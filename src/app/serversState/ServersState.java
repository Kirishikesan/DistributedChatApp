package app.serversState;

import app.election.FastBullyAlgorithm;
import app.server.Server;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;
import java.util.concurrent.ConcurrentHashMap;

public class ServersState {
    private static ServersState serversStateInstance;

    private int selfServerId;
    private final ConcurrentHashMap<Integer, Server> serversMap = new ConcurrentHashMap<>();

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

                Server server = new Server(Integer.parseInt(server_config[0].substring(1, 2)), server_config[1], Integer.parseInt(server_config[2]), Integer.parseInt(server_config[3]));
                serversMap.put(Integer.parseInt(server_config[0].substring(1, 2)), server);

                if (server_config[0].equals(serverId)) {
                    selfServerId = Integer.parseInt(server_config[0].substring(1, 2));
                    Thread serverThread = new Thread(server);
                    serverThread.start();
                }

            }

        } catch (FileNotFoundException e) {
            System.out.println("An error occurred.");
            e.printStackTrace();
        }

    }
}
