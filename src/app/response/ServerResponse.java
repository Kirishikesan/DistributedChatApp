package app.response;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.util.ArrayList;
import java.util.List;
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

    public static JSONObject getLocalUpdatesRequest(int identity, JSONArray clients, JSONArray chatRooms) {
        JSONObject responseObj = new JSONObject();
        responseObj.put("type", "leaderstateupdate");
        responseObj.put("identity", identity);
        responseObj.put("clients", clients);
        responseObj.put("chatrooms", chatRooms);


        return responseObj;
    }

}
