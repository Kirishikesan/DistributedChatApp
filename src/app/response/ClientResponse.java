package app.response;

import app.room.ChatRoom;
import org.json.simple.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

public class ClientResponse {

    @SuppressWarnings("unchecked")
    public static JSONObject createChatRoomResponse(boolean isRoomSuccess, String roomId) {
        JSONObject responseObj = new JSONObject();
        responseObj.put("type", "createroom");
        responseObj.put("roomid", roomId);
        responseObj.put("approved", String.valueOf(isRoomSuccess));

        return responseObj;
    }


    @SuppressWarnings("unchecked")
    public static JSONObject changeChatRoomResponse(String clientId, String formerRoomId, String newRoomId) {
        JSONObject responseObj = new JSONObject();
        responseObj.put("type", "roomchange");
        responseObj.put("identity", clientId);
        responseObj.put("former", formerRoomId);
        responseObj.put("roomid", newRoomId);

        return responseObj;
    }

    @SuppressWarnings("unchecked")
    public static JSONObject listChatRoomsResponse(ConcurrentHashMap<String, ChatRoom> m) {
        List<String> chatRoomsList = new ArrayList<>();
        m.forEach((key,chatRoom) -> chatRoomsList.add(chatRoom.getRoomId()));
        JSONObject responseObj = new JSONObject();
        responseObj.put("type", "roomlist");
        responseObj.put("rooms", chatRoomsList);

        return responseObj;
    }

    @SuppressWarnings("unchecked")
    public static JSONObject joinChatRoomResponse(String clientId, String formerRoomId, String joiningRoomId) {
        JSONObject responseObj = new JSONObject();
        responseObj.put("type", "roomchange");
        responseObj.put("identity", clientId);
        responseObj.put("former", formerRoomId);
        responseObj.put("roomid", joiningRoomId);

        return responseObj;
    }

    @SuppressWarnings("unchecked")
    public static JSONObject deleteChatRoomResponse(String joiningRoomId, String approved) {
        JSONObject responseObj = new JSONObject();
        responseObj.put("type", "deleteroom");
        responseObj.put("roomid", joiningRoomId);
        responseObj.put("approved", approved);

        return responseObj;
    }
      
    @SuppressWarnings("unchecked")
    public static JSONObject messageChatRoom(String clientId, String content) {
        JSONObject responseObj = new JSONObject();
        responseObj.put("type", "message");
        responseObj.put("identity", clientId);
        responseObj.put("content", content);


        return responseObj;
    }
}
