package app;

import app.server.Server;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class App {

    private static ArrayList<Server> server_threads = new ArrayList<>();
    private static Executor server_threadPool = Executors.newFixedThreadPool(4);

    public static void main(String[] args) throws Exception {

        System.out.println("Input server : " + args[0]);
        initializeServer(args[0], args[1]);

    }

    private static void initializeServer(String serverId, String server_config_path){
        try {
            File file = new File(server_config_path);
            Scanner myReader = new Scanner(file);
            while (myReader.hasNextLine()) {
                String data = myReader.nextLine();
                String[] server_config = data.split(" ");
                if (server_config[0].equals(serverId)) {
                    Server server = new Server("s1", "localhost", 4444, 5555);
                    server_threads.add(server);
                    server_threadPool.execute(server);
                }
            }
        } catch (FileNotFoundException e) {
            System.out.println("An error occurred.");
            e.printStackTrace();
        }
    }


}
