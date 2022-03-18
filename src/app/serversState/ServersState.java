package app.serversState;

import app.server.Server;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;
import java.util.concurrent.ConcurrentHashMap;

public class ServersState {
    private static ServersState serversStateInstance;
    private final ConcurrentHashMap<String, Server> serversMap = new ConcurrentHashMap<>();

    private ServersState() {
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
                if (server_config[0].equals(serverId)) {
                    Server server = new Server(server_config[0], server_config[1], Integer.parseInt(server_config[2]), Integer.parseInt(server_config[3]));
                    server.start();
                    serversMap.put(server_config[0], server);
                }
            }
        } catch (FileNotFoundException e) {
            System.out.println("An error occurred.");
            e.printStackTrace();
        }
    }
}
