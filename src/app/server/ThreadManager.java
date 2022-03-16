package app.server;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.lang.reflect.Array;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;

import app.response.ClientResponse;
import app.room.ChatRoom;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;


public class ThreadManager implements Runnable {

    private final BufferedReader bufferedReader;
    private final PrintWriter writer;
    public CopyOnWriteArrayList<ChatRoom> chatRoomsList;
    private String clientId;
    private String roomId;
    private final ArrayList<ThreadManager> clients;

    public ThreadManager(Socket clientSocket, CopyOnWriteArrayList<ChatRoom> chatRoomsList, ArrayList<ThreadManager> clients) throws IOException {
        bufferedReader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
        writer = new PrintWriter(clientSocket.getOutputStream(), true);
        this.chatRoomsList = chatRoomsList;
        this.clients = clients;
        roomId = chatRoomsList.get(0).getRoomId();
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
                    ChatRoom mainHall = chatRoomsList.get(0);
                    mainHall.addMember(clientId);
                    writer.println("{\"type\" : \"newidentity\", \"approved\" : \"true\"}");

//                creating rooms
                } else if (client_obj.get("type").equals("createroom")) {
                    String[] roomIdsArray  = createChatRoom(client_obj);
                    boolean isRoomCreateSuccess = !Objects.equals(roomIdsArray[0], roomIdsArray[1]);
                    JSONObject createRoomResJsonObj = ClientResponse.createChatRoomResponse(isRoomCreateSuccess, (String) client_obj.get("roomid"));
                    writer.println(createRoomResJsonObj);
                    if (isRoomCreateSuccess) {
                        JSONObject roomChangeResJsonObj = ClientResponse.changeChatRoomResponse(clientId, chatRoomsList.get(0).getRoomId(), (String) client_obj.get("roomid"));
                        ChatRoom formerChatRoom = chatRoomsList.stream().filter(chatRoom -> chatRoom.getRoomId().equals(roomIdsArray[0])).toList().get(0);
                        List<String> notifyingClients = formerChatRoom.getMembers();
                        writer.println(roomChangeResJsonObj);
                        clients.forEach(client -> notifyingClients.forEach(notifyingClient -> {
                            if (client.clientId.equals(notifyingClient)) client.writer.println(roomChangeResJsonObj);
                        }));
                    }

//                    listing all rooms
                } else if (client_obj.get("type").equals("list")) {
                    JSONObject listRoomsResJsonObj = ClientResponse.listChatRoomsResponse(chatRoomsList);
                    writer.println(listRoomsResJsonObj);

                } else if (client_obj.get("type").equals("joinroom")) {
                    String[] roomIdsArray = joiningRoomId(client_obj);
                    JSONObject listRoomsResJsonObj = ClientResponse.joinChatRoomResponse(clientId, roomIdsArray[0], roomIdsArray[1]);

                    if (roomIdsArray[0] != roomIdsArray[1]) {
                        ChatRoom formerChatRoom = chatRoomsList.stream().filter(chatRoom -> chatRoom.getRoomId().equals(roomIdsArray[0])).toList().get(0);
                        ChatRoom newChatRoom = chatRoomsList.stream().filter(chatRoom -> chatRoom.getRoomId().equals(roomIdsArray[1])).toList().get(0);
                        List<String> notifyingClients = formerChatRoom.getMembers();
                        notifyingClients.addAll(newChatRoom.getMembers());
                        clients.forEach(client -> notifyingClients.forEach(notifyingClient -> {
                            if (client.clientId.equals(notifyingClient)) client.writer.println(listRoomsResJsonObj);
                        }));
                    }else{
                        writer.println(listRoomsResJsonObj);
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
            for (ChatRoom chatRoom : chatRoomsList) {
                if (chatRoom.getRoomId().equals(newRoomId) || chatRoom.getOwner().equals(clientId)) {
                    return roomIdsArray;
                }
            }
            ChatRoom newChatRoom = new ChatRoom(newRoomId, clientId);
            chatRoomsList.add(newChatRoom);
            ChatRoom formerChatRoom = chatRoomsList.stream().filter(chatRoom -> chatRoom.getRoomId().equals(roomId)).toList().get(0);
            formerChatRoom.removeMember(clientId);
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
        for (ChatRoom chatRoom : chatRoomsList) {
            if (chatRoom.getRoomId().equals(joiningRoomId)) isJoiningRoomIdExist = true;
            if (chatRoom.getOwner().equals(clientId)) return roomIdsArray;
        }
        if (isJoiningRoomIdExist) {
            ChatRoom formerChatRoom = chatRoomsList.stream().filter(chatRoom -> chatRoom.getRoomId().equals(roomId)).toList().get(0);
            formerChatRoom.removeMember(clientId);
            ChatRoom newChatRoom = chatRoomsList.stream().filter(chatRoom -> chatRoom.getRoomId().equals(joiningRoomId)).toList().get(0);
            newChatRoom.addMember(clientId);
            roomIdsArray[1] = joiningRoomId;
            roomId = joiningRoomId;
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
