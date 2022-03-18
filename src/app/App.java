package app;

import app.server.Server;
import app.serversState.ServersState;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class App {

    public static void main(String[] args) throws Exception {

        System.out.println("Input server : " + args[0]);
        ServersState.getInstance().initializeServer(args[0], args[1]);

    }
}
