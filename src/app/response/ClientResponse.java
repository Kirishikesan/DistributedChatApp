package app.response;

import org.json.simple.JSONObject;

public class ClientResponse {

    @SuppressWarnings("unchecked")
    public static JSONObject createRoomResponse(boolean isRoomSuccess, String roomId) {
        JSONObject responseObj = new JSONObject();
        responseObj.put("type", "createroom");
        responseObj.put("roomid", roomId);

        if (isRoomSuccess) responseObj.put("approved", "true");
        else responseObj.put("approved", "false");
        return responseObj;
    }


    @SuppressWarnings("unchecked")
    public static JSONObject roomChangeResponse(String clientId,String formerRoomId, String newRoomId) {
        JSONObject responseObj = new JSONObject();
        responseObj.put("type", "roomchange");
        responseObj.put("identity", clientId);
        responseObj.put("former", formerRoomId);
        responseObj.put("roomid", newRoomId);


        return responseObj;
    }
}
