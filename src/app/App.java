package app;

import app.server.Server;
import app.server.ThreadManager;

import java.util.ArrayList;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class App {

    private static ArrayList<Server> server_threads = new ArrayList<>();
    private static Executor server_threadPool = Executors.newFixedThreadPool(4);

    public static void main(String[] args) throws Exception {

        Server s1 = new Server("s1", 4444);
        server_threads.add(s1);
        server_threadPool.execute(s1);

    }


}
