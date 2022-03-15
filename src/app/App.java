package app;

import app.server.Server;

public class App {


    public static void main(String[] args) throws Exception {
        Server s1 = new Server("s1", 4444);
        s1.init_server();

    }


}
