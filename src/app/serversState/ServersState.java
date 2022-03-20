package app.serversState;

import app.room.ChatRoom;
import app.server.Server;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.ConcurrentHashMap;

public class ServersState {
    private static ServersState serversStateInstance;

    private int selfServerId;
    private final ConcurrentHashMap<Integer, Server> serversMap = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, ChatRoom> chatRoomsMap = new ConcurrentHashMap<>();


    private ServersState() {
    }

    public int getSelfServerId() {
        return selfServerId;
    }

    public ConcurrentHashMap<Integer, Server> getServersMap() {
        return serversMap;
    }

    public static ServersState getInstance() {
        if (serversStateInstance == null) {
            synchronized (ServersState.class) {
                if (serversStateInstance == null) {
                    serversStateInstance = new ServersState();
                }
            }
        }
        return serversStateInstance;
    }

    public void initializeServer(String serverId, String server_config_path) {
        try {
            File file = new File(server_config_path);
            Scanner myReader = new Scanner(file);
            while (myReader.hasNextLine()) {
                String data = myReader.nextLine();
                String[] server_config = data.split(" ");

                Server server = new Server(Integer.parseInt(server_config[0].substring(1, 2)), server_config[1], Integer.parseInt(server_config[2]), Integer.parseInt(server_config[3]));
                serversMap.put(Integer.parseInt(server_config[0].substring(1, 2)), server);

                if (server_config[0].equals(serverId)) {
                    ChatRoom default_chatRoom = server.create_default_chat_room("MainHall-" + serverId);
                    ServersState.getInstance().getChatRoomsMap().put("MainHall-" + serverId, default_chatRoom);

                    selfServerId = Integer.parseInt(server_config[0].substring(1, 2));
                    Thread serverThread = new Thread(server);
                    serverThread.start();
                }
                System.out.println(Arrays.toString(chatRoomsMap.keySet().toArray()));

            }

        } catch (FileNotFoundException e) {
            System.out.println("An error occurred.");
            e.printStackTrace();
        }
    }

    public ConcurrentHashMap<String, ChatRoom> getChatRoomsMap() {
        return chatRoomsMap;
    }


    public JSONArray getClientList() {
        ArrayList<String> clientIdList = new ArrayList<>();

        chatRoomsMap.forEach((chatRoomId, chatRoom) -> {
            clientIdList.addAll(chatRoom.getMembers().keySet());
        });

        JSONArray clientsArray = new JSONArray();
        clientsArray.addAll(clientIdList);

        return clientsArray;
    }

    public JSONArray getChatRoomList() {
        JSONArray chatRoomsArray = new JSONArray();

        chatRoomsMap.forEach((chatRoomId, chatRoom) -> {
            JSONObject chatRoomObject = new JSONObject();
            chatRoomObject.put( "serverId", getSelfServerId() );
            chatRoomObject.put( "chatRoomId", chatRoom.getRoomId() );
            chatRoomObject.put( "ownerId", chatRoom.getOwner() );

            chatRoomsArray.add(chatRoomObject);
        });
        return chatRoomsArray;
    }




}
