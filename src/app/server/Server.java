package app.server;

import app.room.ChatRoom;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class Server implements Runnable {

    private final String serverId;
    private String server_address;
    private final int clients_port;
    private final int coordination_port;

    public static ConcurrentHashMap<String, ChatRoom> chatRoomsMap = new ConcurrentHashMap<>();
    public static Socket clientSocket;
    public static ServerSocket serverSocket;
    public static ConcurrentHashMap<Long, Client> client_threads = new ConcurrentHashMap<>();

    public Server(String serverId, String server_address, int clients_port, int coordination_port) {
        this.serverId = serverId;
        this.server_address = server_address;
        this.clients_port = clients_port;
        this.coordination_port = coordination_port;
        ChatRoom default_chatRoom = create_chat_room("MainHall-" + serverId);
        chatRoomsMap.put("MainHall-" + serverId,default_chatRoom);
    }

    @Override
    public void run() {

        try {
            serverSocket = new ServerSocket(clients_port);
        } catch (IOException e) {
            e.printStackTrace();
        }

        System.out.println("Server-" + serverId + " Listening!");

        while (true) {
            try {
                clientSocket = serverSocket.accept();
                System.out.println("Connection Established!");

                Client client = new Client(clientSocket);
                Thread client_thread = new Thread(client, "thread-" + serverId);
                client_thread.start();

                client.setClientThreadId(client_thread.getId());
                client_threads.put(client_thread.getId() , client);

            } catch (Exception e) {
                System.out.println("server" + e);
            }
        }

    }

    ChatRoom create_chat_room(String roomId) {
        return new ChatRoom(roomId, null);
    }

    public String getserverId() {
        return serverId;
    }

    public static void removeClientSocket (Long clientThreadId){
        client_threads.remove(clientThreadId);
    }


}
