package app.response;

import org.json.simple.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class ServerResponse {

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

}
