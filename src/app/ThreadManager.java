package app;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;

public class ThreadManager implements Runnable {

    private final BufferedReader bufferedReader;


    public ThreadManager(Socket clientSocket) throws IOException {
        bufferedReader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
    }

    @Override
    public void run() {
        while (true) {
            System.out.println("Thread started");
            String msg = null;
            try {
                msg = bufferedReader.readLine();
            } catch (IOException e) {
                e.printStackTrace();
            }
            System.out.println(msg);
        }
    }
}
