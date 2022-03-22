package app;

import app.database.DatabaseConnection;
import app.election.FastBullyAlgorithm;
import app.serversState.ServersState;


public class App {

    public static void main(String[] args) {

        System.out.println("Input server : " + args[0]);
        ServersState.getInstance().initializeServer(args[0], args[1]);
        DatabaseConnection.getInstance().initializeDatabaseConnection(args[0], args[1]);

        FastBullyAlgorithm.initializeLeader();

        Runnable heartbeat = new FastBullyAlgorithm("heartbeat");
        new Thread(heartbeat).start();
    }
}
