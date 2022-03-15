package app;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;


public class ThreadManager implements Runnable {

    private final BufferedReader bufferedReader;
    private final PrintWriter writer;

    public ThreadManager(Socket clientSocket) throws IOException {
        bufferedReader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
        writer = new PrintWriter(clientSocket.getOutputStream(), true);
    }

    @Override
    public void run() {
        while (true) {
            String msg = null;
            JSONObject client_obj = null;
            JSONParser parser = new JSONParser();

            System.out.println("Thread started");

            try {
                msg = bufferedReader.readLine();
                client_obj = (JSONObject) parser.parse(msg);
                writer.println(client_obj.toString());
            } catch (IOException | ParseException e) {
                e.printStackTrace();
            }

            System.out.println("client_obj");
            System.out.println(client_obj.toString());
        }
    }
}
