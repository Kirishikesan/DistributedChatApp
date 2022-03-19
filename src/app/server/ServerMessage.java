package app.server;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class ServerMessage {

    //send message to server
    public static void sendServer( JSONObject message, Server destination) throws IOException
    {
        Socket serverSocket = new Socket(destination.getServerAddress(), destination.getCoordinationPort());
        PrintWriter writer = new PrintWriter(serverSocket.getOutputStream(), true);
        writer.println(message);
    }

}
