package app.server;

import app.room.ChatRoom;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;

public class Server implements Runnable{

    private final int serverId;
    private String server_address;
    private final int clients_port;
    private final int coordination_port;


//    private static ConcurrentHashMap<String, ChatRoom> chatRoomsMap = new ConcurrentHashMap<>();
    public static Socket clientSocket;
    public static ServerSocket serverClientSocket;
    public static ServerSocket serverCoordinationSocket;

    //    public static ConcurrentHashMap<Long, Client> client_threads = new ConcurrentHashMap<>();
    public static ConcurrentHashMap<Long, Thread> client_threads = new ConcurrentHashMap<>();
    public static HashMap<Long, ClientHandlerThread> clientHandlerThreadsMap = new HashMap<>();

    public Server(int serverId, String server_address, int clients_port, int coordination_port) {
        this.serverId = serverId;
        this.server_address = server_address;
        this.clients_port = clients_port;
        this.coordination_port = coordination_port;
    }

    public int getserverId() {
        return serverId;
    }

    public String getServerAddress() {
        return server_address;
    }

    public int getCoordinationPort() {
        return coordination_port;
    }

    public String getClients_port() {
        return String.valueOf(clients_port);
    }

    public void run() {

        try {
            InetAddress ip = InetAddress.getLocalHost();
            String internal_address = ip.getHostAddress();

            System.out.println("My external IP address is: " + internal_address + " - " + server_address);

            serverClientSocket = new ServerSocket();
            serverClientSocket.bind(new InetSocketAddress(internal_address, clients_port));
            serverCoordinationSocket = new ServerSocket();
            serverCoordinationSocket.bind(new InetSocketAddress(internal_address, coordination_port));

        } catch (IOException e) {
            e.printStackTrace();
        }
        ServerHandlerThread serverHandlerThread = new ServerHandlerThread(serverCoordinationSocket);
        Thread serverCoordinationThread = new Thread(serverHandlerThread);
        serverCoordinationThread.start();

        System.out.println("Server-" + serverId + " Listening!");

        while (true) {
            try {
                clientSocket = serverClientSocket.accept();
                System.out.println("Connection Established!");

                ClientHandlerThread clientHandlerThread = new ClientHandlerThread(clientSocket);
                Thread client_thread = new Thread(clientHandlerThread, "thread-" + serverId);
                client_thread.start();

                clientHandlerThread.setClientThreadId(client_thread.getId());
                client_threads.put(client_thread.getId(), client_thread);
                clientHandlerThreadsMap.put(client_thread.getId(), clientHandlerThread);
            } catch (Exception e) {
                System.out.println("server" + e);
            }
        }
    }

    public static ClientHandlerThread getClientHandlerThread(long clientThreadId){
        return clientHandlerThreadsMap.get(clientThreadId);
    }

    public ChatRoom create_default_chat_room(String roomId) {
        return new ChatRoom(roomId, null);
    }

    public static void removeClientThread(Long clientThreadId) {
        System.out.println("clientSocket thread stop");

        Thread client_thread = client_threads.get(clientThreadId);
        client_threads.remove(clientThreadId);
        client_thread.stop();

    }

}
