package app.server;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

import app.response.ClientResponse;
import app.room.ChatRoom;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;


public class Client implements Runnable {

    private BufferedReader bufferedReader;
    private PrintWriter writer;
    public String clientId;
    private String roomId;

    public Client() {
    }

    public Client(Socket clientSocket) throws IOException {
        bufferedReader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
        writer = new PrintWriter(clientSocket.getOutputStream(), true);
        roomId = Server.chatRoomsMap.get(Server.chatRoomsMap.keySet().toArray()[0]).getRoomId();
    }

    @Override
    public void run() {
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
                    ChatRoom mainHall = Server.chatRoomsMap.get(Server.chatRoomsMap.keySet().toArray()[0]);
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
                        for (String key : Server.chatRoomsMap.keySet()) {
                            if (Server.chatRoomsMap.get(key).getRoomId().equals(roomIdsArray[0])) {
                                formerChatRoom = Server.chatRoomsMap.get(key);
                                break;
                            }
                        }
                        JSONObject roomChangeResJsonObj = ClientResponse.changeChatRoomResponse(clientId, formerChatRoom.getRoomId(), (String) client_obj.get("roomid"));
                        ConcurrentHashMap<String, Client> notifyingClients = formerChatRoom.getMembers();
                        writer.println(roomChangeResJsonObj);
                        notifyingClients.forEach((key, client) -> {
                            if (!key.equals("default")) client.writer.println(roomChangeResJsonObj);
                        });
                    }

//                    listing all rooms
                } else if (client_obj.get("type").equals("list")) {
                    JSONObject listRoomsResJsonObj = ClientResponse.listChatRoomsResponse(Server.chatRoomsMap);
                    writer.println(listRoomsResJsonObj);

                } else if (client_obj.get("type").equals("joinroom")) {
                    String[] roomIdsArray = joiningRoomId(client_obj);
                    JSONObject listRoomsResJsonObj = ClientResponse.joinChatRoomResponse(clientId, roomIdsArray[0], roomIdsArray[1]);

                    if (!Objects.equals(roomIdsArray[0], roomIdsArray[1])) {
                        for (String key : Server.chatRoomsMap.keySet()) {
                            if (Server.chatRoomsMap.get(key).getRoomId().equals(roomIdsArray[0])) {
                                Server.chatRoomsMap.get(key).getMembers().forEach((former_key, client) -> {
                                    if (!former_key.equals("default")) client.writer.println(listRoomsResJsonObj);
                                });
                                break;
                            }
                        }

                        for (String key : Server.chatRoomsMap.keySet()) {
                            if (Server.chatRoomsMap.get(key).getRoomId().equals(roomIdsArray[1])) {
                                Server.chatRoomsMap.get(key).getMembers().forEach((new_key, client) -> {
                                    if (!new_key.equals("default")) client.writer.println(listRoomsResJsonObj);
                                });
                                break;
                            }
                        }

                    } else {
                        writer.println(listRoomsResJsonObj);
                    }
                } else if (client_obj.get("type").equals("message")) {
                    JSONObject messageChatRoomsJsonObj = ClientResponse.messageChatRoom(clientId, (String) client_obj.get("content"));

                    for (String key : Server.chatRoomsMap.keySet()) {
                        if (Server.chatRoomsMap.get(key).getRoomId().equals(roomId)) {
                            Server.chatRoomsMap.get(key).getMembers().forEach((new_key, client) -> {
                                if (!new_key.equals("default") && !client.clientId.equals(clientId)) client.writer.println(messageChatRoomsJsonObj);
                            });
                            break;
                        }
                    }

                }
            } catch (IOException | ParseException e) {
                e.printStackTrace();
            }
        }
    }

    private String[] createChatRoom(JSONObject client_obj) {
        String newRoomId = (String) client_obj.get("roomid");
        String[] roomIdsArray = {roomId, roomId};
        if (createChatRoomValidation(newRoomId)) {
            for (String key : Server.chatRoomsMap.keySet()) {
                if (Server.chatRoomsMap.get(key).getRoomId().equals(newRoomId) || Server.chatRoomsMap.get(key).getOwner().equals(clientId)) {
                    return roomIdsArray;
                }
            }
            ChatRoom newChatRoom = new ChatRoom(newRoomId, this);
            Server.chatRoomsMap.put(newRoomId, newChatRoom);

            for (String key : Server.chatRoomsMap.keySet()) {
                if (Server.chatRoomsMap.get(key).getRoomId().equals(roomId)) {
                    Server.chatRoomsMap.get(key).removeMember(this);
                    break;
                }
            }
            roomIdsArray[1] = newRoomId;
            roomId = newRoomId;
            return roomIdsArray;
        }
        return roomIdsArray;
    }

    private String[] joiningRoomId(JSONObject client_obj) {
        String joiningRoomId = (String) client_obj.get("roomid");
        String[] roomIdsArray = {roomId, roomId};
        boolean isJoiningRoomIdExist = false;
        for (String key : Server.chatRoomsMap.keySet()) {
            if (Server.chatRoomsMap.get(key).getRoomId().equals(joiningRoomId)) isJoiningRoomIdExist = true;
            if (Server.chatRoomsMap.get(key).getOwner().equals(clientId)) return roomIdsArray;
        }
        if (isJoiningRoomIdExist) {

            for (String key : Server.chatRoomsMap.keySet()) {
                if (Server.chatRoomsMap.get(key).getRoomId().equals(roomId)) {
                    Server.chatRoomsMap.get(key).removeMember(this);
                    break;
                }
            }

            for (String key : Server.chatRoomsMap.keySet()) {
                if (Server.chatRoomsMap.get(key).getRoomId().equals(joiningRoomId)) {
                    Server.chatRoomsMap.get(key).addMember(this);
                    break;
                }
            }
            roomIdsArray[1] = joiningRoomId;
            roomId = joiningRoomId;
//            Server.chatRoomsMap.forEach((key, value) -> {
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
}
