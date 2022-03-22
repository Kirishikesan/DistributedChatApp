package app.server;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

import app.leaderState.LeaderState;
import app.response.ClientResponse;
import app.response.ServerResponse;
import app.room.ChatRoom;
import app.serversState.ServersState;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;


public class ClientHandlerThread implements Runnable {

    private BufferedReader bufferedReader;
    private PrintWriter writer;
    public String clientId;
    private String roomId;
    private Socket clientSocket;
    private Long clientThreadId;
    private int approvedCreateRoom = -1;
    
    final Object lock=new Object();

    public ClientHandlerThread() {
    }

    public ClientHandlerThread(Socket clientSocket) throws IOException {
        this.clientSocket = clientSocket;
        bufferedReader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
        writer = new PrintWriter(clientSocket.getOutputStream(), true);
        roomId = ServersState.getInstance().getChatRoomsMap().get(ServersState.getInstance().getChatRoomsMap().keySet().toArray()[0]).getRoomId();

    }

    public void setClientThreadId(Long clientThreadId) {
        this.clientThreadId = clientThreadId;
    }

    public String getClientId() {
        return clientId;
    }

    public void setRoomId(String roomId) {
        this.roomId = roomId;
    }

    @Override
    public void run(){
        System.out.println("clientSocket thread started");
        String msg = null;
        JSONObject client_obj = null;
        JSONParser parser = new JSONParser();

//        listen for incoming socket messages
        while (true) {
            try {
                msg = bufferedReader.readLine();
                client_obj = (JSONObject) parser.parse(msg);

//                adding users
                if (client_obj.get("type").equals("newidentity")) {
                    clientId = (String) client_obj.get("identity");
                    ChatRoom mainHall = ServersState.getInstance().getChatRoomsMap().get(ServersState.getInstance().getChatRoomsMap().keySet().toArray()[0]);
                    mainHall.addMember(this);
                    writer.println("{\"type\" : \"newidentity\", \"approved\" : \"true\"}");

//                creating rooms
                } else if (client_obj.get("type").equals("createroom")) {
                    String[] roomIdsArray = createChatRoom(client_obj);
                    boolean isRoomCreateSuccess = !Objects.equals(roomIdsArray[0], roomIdsArray[1]);
                    JSONObject createRoomResJsonObj = ClientResponse.createChatRoomResponse(isRoomCreateSuccess, (String) client_obj.get("roomid"));
                    writer.println(createRoomResJsonObj);
                    if (isRoomCreateSuccess) {
                        ChatRoom formerChatRoom = null;
                        for (String key : ServersState.getInstance().getChatRoomsMap().keySet()) {
                            if (ServersState.getInstance().getChatRoomsMap().get(key).getRoomId().equals(roomIdsArray[0])) {
                                formerChatRoom = ServersState.getInstance().getChatRoomsMap().get(key);
                                break;
                            }
                        }
                        JSONObject roomChangeResJsonObj = ClientResponse.changeChatRoomResponse(clientId, formerChatRoom.getRoomId(), (String) client_obj.get("roomid"));
                        ConcurrentHashMap<String, ClientHandlerThread> notifyingClients = formerChatRoom.getMembers();
                        writer.println(roomChangeResJsonObj);
                        notifyingClients.forEach((key, clientHandlerThread) -> {
                            if (!key.equals("default")) clientHandlerThread.writer.println(roomChangeResJsonObj);
                        });
                    }

//                    listing all rooms
                } else if (client_obj.get("type").equals("list")) {
                    JSONObject listRoomsResJsonObj = ClientResponse.listChatRoomsResponse(ServersState.getInstance().getChatRoomsMap());
                    writer.println(listRoomsResJsonObj);

                } else if (client_obj.get("type").equals("joinroom")) {
                    String[] roomIdsArray = joiningRoomId(client_obj);
                    JSONObject listRoomsResJsonObj = ClientResponse.joinChatRoomResponse(clientId, roomIdsArray[0], roomIdsArray[1]);

                    if (!Objects.equals(roomIdsArray[0], roomIdsArray[1])) {
                        for (String key : ServersState.getInstance().getChatRoomsMap().keySet()) {
                            if (ServersState.getInstance().getChatRoomsMap().get(key).getRoomId().equals(roomIdsArray[0])) {
                                ServersState.getInstance().getChatRoomsMap().get(key).getMembers().forEach((former_key, clientHandlerThread) -> {
                                    if (!former_key.equals("default")) clientHandlerThread.writer.println(listRoomsResJsonObj);
                                });
                                break;
                            }
                        }

                        for (String key : ServersState.getInstance().getChatRoomsMap().keySet()) {
                            if (ServersState.getInstance().getChatRoomsMap().get(key).getRoomId().equals(roomIdsArray[1])) {
                                ServersState.getInstance().getChatRoomsMap().get(key).getMembers().forEach((new_key, clientHandlerThread) -> {
                                    if (!new_key.equals("default")) clientHandlerThread.writer.println(listRoomsResJsonObj);
                                });
                                break;
                            }
                        }

                    } else {
                        writer.println(listRoomsResJsonObj);
                    }

                }else if (client_obj.get("type").equals("deleteroom")) {
                    String[] roomIdsArray = deleteRoomId(client_obj);

                    boolean isRoomDeleteSuccess = !Objects.equals(roomIdsArray[0], roomIdsArray[1]);
                    JSONObject listRoomsResJsonObj = ClientResponse.deleteChatRoomResponse(roomIdsArray[0], String.valueOf(isRoomDeleteSuccess));
                    this.writer.println(listRoomsResJsonObj);

                    // TO Do - notify servers

                } else if (client_obj.get("type").equals("message")) {
                    JSONObject messageChatRoomsJsonObj = ClientResponse.messageChatRoom(clientId, (String) client_obj.get("content"));

                    for (String key : ServersState.getInstance().getChatRoomsMap().keySet()) {
                        if (ServersState.getInstance().getChatRoomsMap().get(key).getRoomId().equals(roomId)) {
                            ServersState.getInstance().getChatRoomsMap().get(key).getMembers().forEach((new_key, clientHandlerThread) -> {
                                if (!new_key.equals("default") && !clientHandlerThread.clientId.equals(clientId)) clientHandlerThread.writer.println(messageChatRoomsJsonObj);
                            });
                            break;
                        }
                    }
                }else if (client_obj.get("type").equals("quit")) {
                    String[] roomIdsArray = quit();
                    boolean isQuitSuccess = !Objects.equals(roomIdsArray[0], roomIdsArray[1]);

                    if(isQuitSuccess){

                        //close connection
                        clientSocket.close();
                    }

                }

            } catch (Exception e) {
                System.out.println("client - " + e);
            }
        }
    } 

    private String[] createChatRoom(JSONObject client_obj) throws IOException{
        String newRoomId = (String) client_obj.get("roomid");
        String[] roomIdsArray = {roomId, roomId};
        if (createChatRoomValidation(newRoomId)) {
        	if(LeaderState.getInstance().getLeaderId()==ServersState.getInstance().getSelfServerId()) {
	            for (JSONObject activeChatRoom : LeaderState.getInstance().getActiveChatRooms()) {
	                if (activeChatRoom.get("chatRoomId").equals(newRoomId) || activeChatRoom.get("ownerId").equals(clientId)) {
	                    return roomIdsArray;
	                }
	            }
            }else{
            	ServerMessage.sendToLeader(ServerResponse.createRoom(String.valueOf(ServersState.getInstance().getSelfServerId()), newRoomId));
            	synchronized(lock) {
            		if(approvedCreateRoom==-1) {
            			try {
							lock.wait(10000);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
            		}
            	}
            }
            ChatRoom newChatRoom = new ChatRoom(newRoomId, this);
            ServersState.getInstance().getChatRoomsMap().put(newRoomId, newChatRoom);

            for (String key : ServersState.getInstance().getChatRoomsMap().keySet()) {
                if (ServersState.getInstance().getChatRoomsMap().get(key).getRoomId().equals(roomId)) {
                    ServersState.getInstance().getChatRoomsMap().get(key).removeMember(this);
                    break;
                }
            }
            roomIdsArray[1] = newRoomId;
            roomId = newRoomId;
            return roomIdsArray;
        }
        return roomIdsArray;
    }
    
    public void approveCreateRoom(int approvedCreateRoom) {
    	this.approvedCreateRoom = approvedCreateRoom;
    }

    private String[] joiningRoomId(JSONObject client_obj) {
        String joiningRoomId = (String) client_obj.get("roomid");
        String[] roomIdsArray = {roomId, roomId};
        boolean isJoiningRoomIdExist = false;
        for (String key : ServersState.getInstance().getChatRoomsMap().keySet()) {
            if (ServersState.getInstance().getChatRoomsMap().get(key).getRoomId().equals(joiningRoomId)) isJoiningRoomIdExist = true;
            if (ServersState.getInstance().getChatRoomsMap().get(key).getOwner().equals(clientId)) return roomIdsArray;
        }
        if (isJoiningRoomIdExist) {

            for (String key : ServersState.getInstance().getChatRoomsMap().keySet()) {
                if (ServersState.getInstance().getChatRoomsMap().get(key).getRoomId().equals(roomId)) {
                    ServersState.getInstance().getChatRoomsMap().get(key).removeMember(this);
                    break;
                }
            }

            for (String key : ServersState.getInstance().getChatRoomsMap().keySet()) {
                if (ServersState.getInstance().getChatRoomsMap().get(key).getRoomId().equals(joiningRoomId)) {
                    ServersState.getInstance().getChatRoomsMap().get(key).addMember(this);
                    break;
                }
            }
            roomIdsArray[1] = joiningRoomId;
            roomId = joiningRoomId;
//            ServersState.getInstance().getChatRoomsMap().forEach((key, value) -> {
//                System.out.println(key);
//                value.getMembers().forEach((key1,value1)->{
//                    System.out.println(value1.clientId);
//                });
//            });
            return roomIdsArray;
        }
        return roomIdsArray;
    }

    private boolean createChatRoomValidation(String roomId) {
        if (roomId.length() >= 3 && roomId.length() <= 16 && Character.isLetter(roomId.charAt(0))) {
            char[] roomIdChars = roomId.toCharArray();
            for (char character : roomIdChars) {
                if (!Character.isLetterOrDigit(character)) return false;
            }
            return true;
        }
        return false;
    }

    private String[] deleteRoomId(JSONObject client_obj) {
        String deleteRoomId = (String) client_obj.get("roomid");

        String[] roomIdsArray = {roomId, roomId};
        boolean isDeleteRoomIdExist = false;
        boolean isDeleteRoomOwnerExist = false;
        for (String key : ServersState.getInstance().getChatRoomsMap().keySet()) {
            if (ServersState.getInstance().getChatRoomsMap().get(key).getRoomId().equals(deleteRoomId)) isDeleteRoomIdExist = true;
            if (ServersState.getInstance().getChatRoomsMap().get(key).getOwner().equals(clientId)) isDeleteRoomOwnerExist = true;
        }
        if (isDeleteRoomIdExist && isDeleteRoomOwnerExist) {

            //move all members to main hall
            ChatRoom formerChatRoom = ServersState.getInstance().getChatRoomsMap().get(deleteRoomId);
            String mainHallRoomId = (String) ServersState.getInstance().getChatRoomsMap().keySet().toArray()[0];
            ChatRoom mainHall = ServersState.getInstance().getChatRoomsMap().get(mainHallRoomId);

            ConcurrentHashMap<String, ClientHandlerThread> notifyingClients = formerChatRoom.getMembers();

            String[] moveRoomIdsArray = moveAll(formerChatRoom, mainHall);
            boolean isClientsMoveSuccess = !Objects.equals(moveRoomIdsArray[0], moveRoomIdsArray[1]);

            if (isClientsMoveSuccess) {
                //notify all about move
                notifyingClients.forEach((notifyClient_key, notifyClientHandlerThread) -> {
                    notifyingClients.forEach((formerClient_key, formerClientHandlerThread) -> {
                        if (!formerClient_key.equals("default")){
                            JSONObject listRoomsResJsonObj = ClientResponse.joinChatRoomResponse(formerClient_key, moveRoomIdsArray[0], moveRoomIdsArray[1]);
                            if (!notifyClient_key.equals("default")) notifyClientHandlerThread.writer.println(listRoomsResJsonObj);
                        }
                    });
                });

                //delete chat room
                ServersState.getInstance().getChatRoomsMap().remove(deleteRoomId);

                //notify all about delete
                roomIdsArray[1] = mainHall.getRoomId();
                roomId = mainHall.getRoomId();
            }

//            ServersState.getInstance().getChatRoomsMap().forEach((key, value) -> {
//                System.out.println(key);
//                value.getMembers().forEach((key1,value1)->{
//                    System.out.println(value1.clientId);
//                });
//            });
            return roomIdsArray;
        }
        return roomIdsArray;
    }

    private String[] moveAll(ChatRoom formerChatRoom, ChatRoom mainHall) {
        String[] roomIdsArray = {roomId, roomId};

        ConcurrentHashMap<String, ClientHandlerThread> formerChatRoomClients = formerChatRoom.getMembers();
        mainHall.addMembers(formerChatRoomClients);

        roomIdsArray[1] = mainHall.getRoomId();
        roomId = mainHall.getRoomId();

        formerChatRoomClients.forEach((formerClient_key, formerClient) -> {
            if (!formerClient_key.equals("default")){
                formerClient.setRoomId(mainHall.getRoomId());
            }
        });

        return roomIdsArray;
    }

    private String[] forcedeleteRoomId(String roomId) {
        JSONObject client_obj = new JSONObject();
        client_obj.put("type", "deleteroom");
        client_obj.put("roomid", roomId);

        return deleteRoomId(client_obj);
    }

    private String[] quit(){

        String[] quitRoomIdsArray = {roomId, roomId};

        boolean isQuitRoomIdExist = false;
        boolean isQuitRoomOwnerExist = false;

        for (String key : ServersState.getInstance().getChatRoomsMap().keySet()) {
            if (ServersState.getInstance().getChatRoomsMap().get(key).getRoomId().equals(roomId)) isQuitRoomIdExist = true;
            if (ServersState.getInstance().getChatRoomsMap().get(key).getOwner().equals(clientId)) isQuitRoomOwnerExist = true;
        }

//        System.out.println("cl - " + this.clientId + " " + roomId);

        if (isQuitRoomIdExist) {

            if (isQuitRoomOwnerExist) {
                String[] deleteRoomIdsArray = forcedeleteRoomId(quitRoomIdsArray[0]);

                boolean isRoomDeleteSuccess = !Objects.equals(deleteRoomIdsArray[0], deleteRoomIdsArray[1]);
                JSONObject listRoomsResJsonObj = ClientResponse.deleteChatRoomResponse(deleteRoomIdsArray[0], String.valueOf(isRoomDeleteSuccess));
                this.writer.println(listRoomsResJsonObj);

                // TO Do - notify servers

                quitRoomIdsArray[0] = deleteRoomIdsArray[1];
                quitRoomIdsArray[1] = deleteRoomIdsArray[1];
            }


            // notify former room
            JSONObject listRoomsResJsonObj = ClientResponse.joinChatRoomResponse(clientId, quitRoomIdsArray[0], "");
            ConcurrentHashMap<String, ClientHandlerThread> notifyClients = ServersState.getInstance().getChatRoomsMap().get(quitRoomIdsArray[0]).getMembers();

            notifyClients.forEach((former_key, clientHandlerThread) -> {
                if (!former_key.equals("default")) clientHandlerThread.writer.println(listRoomsResJsonObj);
            });

            //update local server
            ChatRoom quitChatRoom = ServersState.getInstance().getChatRoomsMap().get(roomId);
            quitChatRoom.removeMember(this);
            Server.removeClientSocket(this.clientThreadId);

            // TO Do - update global server


            quitRoomIdsArray[1] = "";
            roomId = "";
        }

        return quitRoomIdsArray;
    }
}
