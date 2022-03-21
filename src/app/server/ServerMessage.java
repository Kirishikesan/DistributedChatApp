package app.server;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;

import app.leaderState.LeaderState;
import app.serversState.ServersState;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class ServerMessage {

    //send message to server
    public static void sendToServer(JSONObject message, Server destination) throws IOException
    {
        Socket serverSocket = new Socket(destination.getServerAddress(), destination.getCoordinationPort());
        PrintWriter writer = new PrintWriter(serverSocket.getOutputStream(), true);
        writer.println(message);
    }

    //send message to leader
    public static void sendToLeader(JSONObject message) throws IOException
    {
        int leaderId = LeaderState.getInstance().getLeaderId();
        ConcurrentHashMap<Integer, Server> serversMap = ServersState.getInstance().getServersMap();

        Server destination = serversMap.get(leaderId);
        Socket serverSocket = new Socket(destination.getServerAddress(), destination.getCoordinationPort());
        PrintWriter writer = new PrintWriter(serverSocket.getOutputStream(), true);
        writer.println(message);
    }

}
