package app.response;

import app.room.ChatRoom;
import org.json.simple.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

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
    public static JSONObject roomChangeResponse(String clientId, String formerRoomId, String newRoomId) {
        JSONObject responseObj = new JSONObject();
        responseObj.put("type", "roomchange");
        responseObj.put("identity", clientId);
        responseObj.put("former", formerRoomId);
        responseObj.put("roomid", newRoomId);

        return responseObj;
    }

    @SuppressWarnings("unchecked")
    public static JSONObject listRoomsResponse(CopyOnWriteArrayList<ChatRoom> chatRoomList) {
        List<String> chatRoomsList = new ArrayList<>();
        chatRoomList.forEach(chatRoom -> chatRoomsList.add(chatRoom.getRoomId()));
        JSONObject responseObj = new JSONObject();
        responseObj.put("type", "roomlist");
        responseObj.put("rooms", chatRoomsList);

        return responseObj;
    }
}
