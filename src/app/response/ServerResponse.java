package app.response;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import app.server.ServerMessage;

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
    
    public static JSONObject createRoom(String identity, String serverId, String ownerId) {
    	JSONObject responseObj = new JSONObject();
    	responseObj.put("type","createRoom");
    	responseObj.put("roomId", identity);
    	responseObj.put("serverId",serverId);
    	responseObj.put("ownerId",ownerId);
    	
    	return responseObj;
    }

    public static JSONObject createClient(String identity, long clientThreadId) {
        JSONObject responseObj = new JSONObject();
        responseObj.put("type","newidentity");
        responseObj.put("identity", identity);
        responseObj.put("clientThreadId", clientThreadId);

        return responseObj;
    }

    public static JSONObject getAllRooms() {
        JSONObject responseObj = new JSONObject();
        responseObj.put("type","allRooms");

        return responseObj;
    }

    public static JSONObject sendAllRooms(Set<JSONObject> allRooms) {
        JSONObject responseObj = new JSONObject();
        responseObj.put("allRooms",allRooms);

        return responseObj;
    }

    public static JSONObject deleteRoom(String identity, String serverId) {
    	JSONObject responseObj = new JSONObject();
    	responseObj.put("type","deleteRoom");
    	responseObj.put("identity", identity);
    	responseObj.put("serverId",serverId);
    	
    	return responseObj;
    }
    
    public static JSONObject approveCreateRoom(String identity, String serverId, int status) {
    	JSONObject responseObj = new JSONObject();
    	responseObj.put("type", "approveCreateRoom");
    	responseObj.put("identity", identity);
    	responseObj.put("serverId", serverId);
    	responseObj.put("status", status);

    	return responseObj;
    }

    public static JSONObject newIdentityResp(String isApproved){
        JSONObject responseObj = new JSONObject();
        responseObj.put("type", "newidentity");
        responseObj.put("approved", isApproved);

        return responseObj;
    }

    public static JSONObject addNewClientResp(String isApproved, String clientThreadId){
        JSONObject responseObj = new JSONObject();
        responseObj.put("type", "addnewclient");
        responseObj.put("approved", isApproved);
        responseObj.put("clientThreadId", clientThreadId);

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
