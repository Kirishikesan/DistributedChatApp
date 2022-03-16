package app.server;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.concurrent.CopyOnWriteArrayList;

import app.response.ClientResponse;
import app.room.ChatRoom;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;


public class ThreadManager implements Runnable {

    private final BufferedReader bufferedReader;
    private final PrintWriter writer;
    public CopyOnWriteArrayList<ChatRoom> chatRoomList;
    private String clientId;
    private final ArrayList<ThreadManager> clients;

    public ThreadManager(Socket clientSocket, CopyOnWriteArrayList<ChatRoom> chatRoomsList, ArrayList<ThreadManager> clients) throws IOException {
        bufferedReader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
        writer = new PrintWriter(clientSocket.getOutputStream(), true);
        this.chatRoomList = chatRoomsList;
        this.clients = clients;
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
                    ChatRoom mainHall = chatRoomList.get(0);
                    mainHall.addMember(clientId);
                    writer.println("{\"type\" : \"newidentity\", \"approved\" : \"true\"}");

//                creating rooms
                } else if (client_obj.get("type").equals("createroom")) {
                    boolean isRoomCreateSuccess = createChatRoom(client_obj);
                    JSONObject createRoomResJsonObj = ClientResponse.createRoomResponse(isRoomCreateSuccess, (String) client_obj.get("roomid"));
                    writer.println(createRoomResJsonObj);
                    if (isRoomCreateSuccess) {
                        JSONObject roomChangeResJsonObj = ClientResponse.roomChangeResponse(clientId, chatRoomList.get(0).getRoomId(), (String) client_obj.get("roomid"));
                        writer.println(roomChangeResJsonObj);
                        for (ThreadManager client : clients) {
                            if (chatRoomList.get(0).getMembers().contains(client.clientId))
                                client.writer.println(roomChangeResJsonObj);
                        }
                    }

//                    listing all rooms
                } else if (client_obj.get("type").equals("list")) {
                    JSONObject listRoomsResJsonObj = ClientResponse.listRoomsResponse(chatRoomList);
                    writer.println(listRoomsResJsonObj);

                }
            } catch (IOException | ParseException e) {
                e.printStackTrace();
            }
        }
    }

    private boolean createChatRoom(JSONObject client_obj) {
        String newRoomId = (String) client_obj.get("roomid");
        if (createChatRoomValidation(newRoomId)) {
            for (ChatRoom chatRoom : chatRoomList) {
                if (chatRoom.getRoomId().equals(newRoomId) || chatRoom.getOwner().equals(clientId)) {
                    return false;
                }
            }
            ChatRoom newChatRoom = new ChatRoom(newRoomId, clientId);
            chatRoomList.add(newChatRoom);
            chatRoomList.get(0).removeMember(clientId);
            return true;
        }
        return false;
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
