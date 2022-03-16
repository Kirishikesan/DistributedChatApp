package app.server;

import app.room.ChatRoom;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class Server implements Runnable {

    private String serverId;
    private int port;
    public CopyOnWriteArrayList<ChatRoom> chatRoomsList = new CopyOnWriteArrayList<ChatRoom>();
    public static Socket clientSocket;
    public static ServerSocket serverSocket;
    private static ArrayList<ThreadManager> client_threads = new ArrayList<>();
    private static Executor client_threadPool = Executors.newFixedThreadPool(4);

    public Server(String serverId, int port) {
        this.serverId = serverId;
        this.port = port;
        ChatRoom default_chatRoom = create_chat_room("MainHall-" + serverId);
        chatRoomsList.add(0, default_chatRoom);
    }

    @Override
    public void run() {

        try {
            serverSocket = new ServerSocket(port);
        } catch (IOException e) {
            e.printStackTrace();
        }

        System.out.println("Server-" + serverId + " Listening!");

        while (true) {
            try {
                clientSocket = serverSocket.accept();
                System.out.println("Connection Established!");
                ThreadManager threadManager = new ThreadManager(clientSocket, chatRoomsList, client_threads);
                client_threads.add(threadManager);
                client_threadPool.execute(threadManager);
            } catch (Exception e) {
                System.out.println(e);
            }
        }

    }

    ChatRoom create_chat_room(String roomId) {
        return new ChatRoom(roomId, "");
    }

    public String getserverId() {
        return serverId;
    }


}
