package app.server;

import app.room.ChatRoom;

import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class Server {

    private String serverid;

    private int port;
    private ChatRoom deault_chatRoom;
    public static Socket clientSocket;
    public static ServerSocket serverSocket;
    private static ArrayList<ThreadManager> threads = new ArrayList<>();
    private static Executor threadPool = Executors.newFixedThreadPool(4);

    public Server(String serverid, int port) {
        this.serverid = serverid;
        this.port = port;
        deault_chatRoom = create_chat_room(serverid,"MainHall-" + serverid, "");
    }

    public void init_server() throws Exception {

        serverSocket = new ServerSocket(port);
        System.out.println("Server-" +serverid + " Listening!");

        while (true) {
            try {
                clientSocket = serverSocket.accept();
                System.out.println("Connection Established!");
                ThreadManager threadManager = new ThreadManager(clientSocket);
                threads.add(threadManager);
                threadPool.execute(threadManager);
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
