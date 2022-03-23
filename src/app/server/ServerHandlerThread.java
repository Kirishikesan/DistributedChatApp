package app.server;

import app.database.ServerDatabase;
import app.election.FastBullyAlgorithm;
import app.leaderState.LeaderState;
import app.response.ClientResponse;
import app.response.ServerResponse;
import app.room.ChatRoom;
import app.serversState.ServersState;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class ServerHandlerThread implements Runnable{

    private final ServerSocket serverCoordinationSocket;

    public ServerHandlerThread(ServerSocket serverCoordinationSocket){
        this.serverCoordinationSocket = serverCoordinationSocket;
    }

    @Override
    public void run() {
        while (true){
            try {
                Socket serverSocket = serverCoordinationSocket.accept();
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(serverSocket.getInputStream()));
                PrintWriter writer = new PrintWriter(serverSocket.getOutputStream(), true);
                String msg = bufferedReader.readLine();
                JSONObject server_obj = (JSONObject) new JSONParser().parse(msg);

                // fast bully algorithm - respond to incoming request
                if (server_obj.containsKey("request")) {
                    FastBullyAlgorithm.handleRequest(server_obj);

                } else if (server_obj.containsKey("type")) {

                    // update leader state - if self server is the elected leader
                    if (server_obj.get("type").equals("leaderstateupdate")) {
                        if (LeaderState.getInstance().isElectedLeader()){
                            updateLeaderState(server_obj);
                        }
                    }else if(server_obj.get("type").equals("createRoom")){
                    	writer.println(approveCreateRoom(server_obj));
                    }else if(server_obj.get("type").equals("deleteRoom")){
                    	updateDeleteRoom(server_obj);
                    }
                }

            } catch (IOException e) {
                e.printStackTrace();
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
    }

    private JSONObject approveCreateRoom(JSONObject server_obj) throws ParseException, IOException{
    	String newRoomId=(String)server_obj.get("roomId");
    	String serverId=(String)server_obj.get("serverId");
    	String clientId=(String)server_obj.get("ownerId");
    	Server server = ServersState.getInstance().getServersMap().get(serverId);
    	for (JSONObject activeChatRoom : LeaderState.getInstance().getActiveChatRooms()) {
            if (activeChatRoom.get("chatRoomId").equals(newRoomId)) {
              JSONObject responseObj = ServerResponse.approveCreateRoom((String)server_obj.get("identity"), (String)server_obj.get("serverId"), -1);
//            	ServerMessage.sendToServer(responseObj, server);
              return responseObj;
            }
        }
    	JSONObject responseObj = ServerResponse.approveCreateRoom((String)server_obj.get("identity"), (String)server_obj.get("serverId"), 1);
//    	ServerMessage.sendToServer(responseObj, server);
    	JSONObject chatroom = new JSONObject();
    	chatroom.put("chatRoomId",newRoomId);
    	chatroom.put("serverId",serverId);
    	chatroom.put("ownerId", clientId);
    	List<JSONObject> chatrooms = new ArrayList<JSONObject>();
    	chatrooms.add(chatroom);
    	LeaderState.getInstance().addChatRooms(chatrooms);
    	return responseObj;
    }
    
    private void updateDeleteRoom(JSONObject server_obj) throws ParseException{
    	LeaderState.getInstance().deleteChatRoom((String)server_obj.get("roomId"));
    	System.out.println(LeaderState.getInstance().getActiveChatRooms());
    }

    private void updateLeaderState(JSONObject server_obj) throws ParseException {

        int senderId = Integer.parseInt(server_obj.get( "identity" ).toString());
        LeaderState.getInstance().setActiveViews(senderId);
        ServerDatabase.saveView(LeaderState.getInstance().getActiveViews());

        List<String> clients = new ArrayList<String>(Arrays.asList(server_obj.get( "clients" ).toString()));

        JSONArray chatroomsJSON = (JSONArray) new JSONParser().parse(server_obj.get( "chatrooms" ).toString());
        List<JSONObject> chatrooms = (List<JSONObject>) chatroomsJSON.stream().map(roomObject -> (JSONObject)roomObject).collect(Collectors.toList());

        LeaderState.getInstance().addClients(clients);
        LeaderState.getInstance().addChatRooms(chatrooms);

        System.out.println("Sent local updated from s" + senderId);
        System.out.println( Arrays.toString(LeaderState.getInstance().getActiveClientsList().toArray()) );
        System.out.println( Arrays.toString(LeaderState.getInstance().getActiveChatRooms().toArray()) );
        System.out.println( Arrays.toString(LeaderState.getInstance().getActiveViews().toArray()) );
    }


}
