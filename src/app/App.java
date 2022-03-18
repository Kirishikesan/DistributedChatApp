package app;

import app.serversState.ServersState;


public class App {

    public static void main(String[] args) throws Exception {

        System.out.println("Input server : " + args[0]);
        ServersState.getInstance().initializeServer(args[0], args[1]);

    }
}
