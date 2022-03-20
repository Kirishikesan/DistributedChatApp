package app.leaderState;

import app.election.FastBullyAlgorithm;
import app.room.ChatRoom;
import app.server.ClientHandlerThread;
import app.serversState.ServersState;

import java.util.concurrent.ConcurrentHashMap;

public class LeaderState {
    private static LeaderState leaderStateInstance;

    private int leaderId;
    private final ConcurrentHashMap<String, ClientHandlerThread> activeClientsList = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, ChatRoom> activeChatRooms = new ConcurrentHashMap<>();

    private LeaderState() {
    }

    public static LeaderState getInstance() {
        if (leaderStateInstance == null) {
            synchronized (LeaderState.class) {
                if (leaderStateInstance == null) {
                    leaderStateInstance = new LeaderState();
                }
            }
        }
        return leaderStateInstance;
    }

    public int getLeaderId() {
        return leaderId;
    }

    public void setLeaderId(int leaderId) {
        this.leaderId = leaderId;
    }



    public void resetLeader() {
        activeClientsList.clear();
        activeChatRooms.clear();
    }

    public void addClient(ClientHandlerThread client) {
        activeClientsList.put(client.getClientId(), client);
    }

    public void addRoom(ChatRoom chatRoom) {
        activeChatRooms.put(chatRoom.getRoomId(), chatRoom);
    }

    public boolean isLeader() {
        return ServersState.getInstance().getSelfServerId() == LeaderState.getInstance().getLeaderId();
    }

    public boolean isElectedLeader() {
//        System.out.println(FastBullyAlgorithm.isLeader + " " + isLeader());
        return FastBullyAlgorithm.isLeader && isLeader();
    }


}
