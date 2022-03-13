package app;

import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class App {

    public static Socket clientSocket;
    public static ServerSocket serverSocket;
    private static ArrayList<ThreadManager> threads = new ArrayList<>();
    private static Executor threadPool = Executors.newFixedThreadPool(4);

    public static void main(String[] args) throws Exception {

        serverSocket = new ServerSocket(4444);
        System.out.println("Server Listening!");

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

}
