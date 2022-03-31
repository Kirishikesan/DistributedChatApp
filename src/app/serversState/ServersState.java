package app.serversState;

import app.room.ChatRoom;
import app.server.Server;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.Socket;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class ServersState {
    private static ServersState serversStateInstance;

    private int selfServerId;
    private final ConcurrentHashMap<Integer, Server> serversMap = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, ChatRoom> chatRoomsMap = new ConcurrentHashMap<>();
    private final Set<Integer> views = Collections.synchronizedSet(new HashSet<>());

    private final ConcurrentHashMap<Integer, Socket> serverSocketMap = new ConcurrentHashMap<>();

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

//                String server_address = server_config[4];
//                if (server_config[0].equals(serverId)) {
//                    server_address = server_config[1];
//                }
                String server_address = server_config[1];
                Server server = new Server(Integer.parseInt(server_config[0].substring(1, 2)), server_address, Integer.parseInt(server_config[2]), Integer.parseInt(server_config[3]));
                serversMap.put(Integer.parseInt(server_config[0].substring(1, 2)), server);

                if (server_config[0].equals(serverId)) {
                    ChatRoom default_chatRoom = server.create_default_chat_room("MainHall-" + serverId);
                    ServersState.getInstance().getChatRoomsMap().put("MainHall-" + serverId, default_chatRoom);

                    selfServerId = Integer.parseInt(server_config[0].substring(1, 2));
                    Thread serverThread = new Thread(server);
                    serverThread.start();
                    setViews(selfServerId);

                }
//                System.out.println(Arrays.toString(chatRoomsMap.keySet().toArray()));

            }

        } catch (Exception e) {
            System.out.println("An error occurred - " + e.getMessage());
        }
    }


    public Socket getServerSocket(Server server) {
        int serverId = server.getserverId();
        if(serverSocketMap.containsKey(serverId)){
            return serverSocketMap.get(serverId);
        }else{
            return addServerSocket(server);
        }
    }

    public Socket addServerSocket(Server server) {
        Socket serverSocket = null;
        try {
            serverSocket = new Socket(server.getServerAddress(), server.getCoordinationPort());
            serverSocketMap.put(server.getserverId(), serverSocket);
        } catch (IOException e) {
            System.out.println("socket doesn't exit s" + server.getserverId() + " - " + e.getMessage());
        }

        return serverSocket;
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

    public Set<Integer> getViews() {
        return views;
    }

    public void setViews(int serverId) {
        views.add(serverId);
    }

    public void setViewsList(Set<Integer> serverIdList) {
        views.addAll(serverIdList);
    }

    public void resetViews() {
        views.clear();
        setViews(selfServerId);
    }
}
