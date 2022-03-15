package app.server;

import app.room.ChatRoom;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class Server implements Runnable{

    private String serverid;

    private int port;
    private ChatRoom deault_chatRoom;
    public static Socket clientSocket;
    public static ServerSocket serverSocket;
    private static ArrayList<ThreadManager> client_threads = new ArrayList<>();
    private static Executor client_threadPool = Executors.newFixedThreadPool(4);

    public Server(String serverid, int port) {
        this.serverid = serverid;
        this.port = port;
        deault_chatRoom = create_chat_room(serverid,"MainHall-" + serverid, "");
    }

    @Override
    public void run() {

        try {
            serverSocket = new ServerSocket(port);
        } catch (IOException e) {
            e.printStackTrace();
        }

        System.out.println("Server-" +serverid + " Listening!");

        while (true) {
            try {
                clientSocket = serverSocket.accept();
                System.out.println("Connection Established!");
                ThreadManager threadManager = new ThreadManager(clientSocket);
                client_threads.add(threadManager);
                client_threadPool.execute(threadManager);
            } catch (Exception e) {
                System.out.println(e);
            }
        }

    }

    ChatRoom create_chat_room(String serverid, String roomid, String owner){
        ChatRoom chatroom = new ChatRoom(serverid, roomid, owner);
        return chatroom;
    }

    public String getServerid() {
        return serverid;
    }


}
