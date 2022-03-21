package app.response;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class ServerResponse {

    public static JSONObject createHeartbeatRequest(int identity) {
        JSONObject responseObj = new JSONObject();
        responseObj.put("request", "heartbeat");
        responseObj.put("identity", identity);

        return responseObj;
    }

    public static JSONObject createElectionRequest(int identity) {
        JSONObject responseObj = new JSONObject();
        responseObj.put("request", "election");
        responseObj.put("identity", identity);

        return responseObj;
    }

    public static JSONObject createAnswerRequest(int identity) {
        JSONObject responseObj = new JSONObject();
        responseObj.put("request", "answer");
        responseObj.put("identity", identity);

        return responseObj;
    }

    public static JSONObject createNominationRequest(int identity) {
        JSONObject responseObj = new JSONObject();
        responseObj.put("request", "nomination");
        responseObj.put("identity", identity);

        return responseObj;
    }

    public static JSONObject createCoordinationRequest(int identity) {
        JSONObject responseObj = new JSONObject();
        responseObj.put("request", "coordination");
        responseObj.put("identity", identity);

        return responseObj;
    }

    public static JSONObject createIamUpRequest(int identity) {
        JSONObject responseObj = new JSONObject();
        responseObj.put("request", "iamup");
        responseObj.put("identity", identity);

        return responseObj;
    }

    public static JSONObject createViewRequest(int identity , JSONArray view) {
        JSONObject responseObj = new JSONObject();
        responseObj.put("request", "view");
        responseObj.put("identity", identity);
        responseObj.put("view", view);
        return responseObj;
    }

    public static JSONObject getLocalUpdatesRequest(int identity, JSONArray clients, JSONArray chatRooms) {
        JSONObject responseObj = new JSONObject();
        responseObj.put("type", "leaderstateupdate");
        responseObj.put("identity", identity);
        responseObj.put("clients", clients);
        responseObj.put("chatrooms", chatRooms);


        return responseObj;
    }
    
    public static JSONObject createChatRoom(int identity, String roomId) {
    	JSONObject responseObj = new JSONObject();
    	responseObj.put("type","createRoom");
    	responseObj.put("identity", identity);
    	responseObj.put("roomId",roomId);
    	
    	return responseObj;
    }

    public static JSONObject informServersNewIdentity(String serverID, String identity, String updatedID){
        JSONObject responseObj = new JSONObject();
        responseObj.put("type", "gossipNewIdentity");
        responseObj.put("serverID", serverID);
        responseObj.put("identity", identity);
        responseObj.put("updatedID", updatedID);

        return responseObj;
    }

    public static JSONObject informServersDeleteIdentity(String serverID, String identity, String updatedID){
        JSONObject responseObj = new JSONObject();
        responseObj.put("type", "gossipDeleteIdentity");
        responseObj.put("serverID", serverID);
        responseObj.put("identity", identity);
        responseObj.put("updatedID", updatedID);

        return responseObj;
    }

    public static JSONObject informServersNewChatroom(String serverID, String roomID, String updatedID){
        JSONObject responseObj = new JSONObject();
        responseObj.put("type", "gossipNewRoomID");
        responseObj.put("serverID", serverID);
        responseObj.put("roomID", roomID);
        responseObj.put("updatedID", updatedID);

        return responseObj;
    }

    public static JSONObject informServersDeleteChatRoom(String serverID, String roomID, String updatedID){
        JSONObject responseObj = new JSONObject();
        responseObj.put("type", "gossipDeleteRoom");
        responseObj.put("serverID", serverID);
        responseObj.put("roomID", roomID);
        responseObj.put("updatedID", updatedID);

        return responseObj;
    }

//    public static JSONObject createClientRequest(){
//
//    }

}
