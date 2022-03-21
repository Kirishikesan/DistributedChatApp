package app.leaderState;

import app.election.FastBullyAlgorithm;
import app.room.ChatRoom;
import app.server.ClientHandlerThread;
import app.serversState.ServersState;
import org.json.simple.JSONObject;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class LeaderState {
    private static LeaderState leaderStateInstance;

    private int leaderId;

    private final Set<Integer> activeViews = Collections.synchronizedSet(new HashSet<>());

    private final Set<String> activeClientsList = Collections.synchronizedSet(new HashSet<>());
    private final Set<JSONObject> activeChatRooms = Collections.synchronizedSet(new HashSet<>());


    private LeaderState() {
    }


    public static LeaderState getLeaderStateInstance() {
        return leaderStateInstance;
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

    public void setActiveViews(int serverId) {
        activeViews.add(serverId);
    }

    public void addClients(List<String> clients) {
        activeClientsList.addAll(clients);
    }

    public void addChatRooms(List<JSONObject> chatRoom) {
        activeChatRooms.addAll(chatRoom);
    }

    public Set<Integer> getActiveViews() {
        return activeViews;
    }

    public Set<String> getActiveClientsList() {
        return activeClientsList;
    }

    public Set<JSONObject> getActiveChatRooms() {
        return activeChatRooms;
    }

    public void resetLeader() {
        activeViews.clear();
        activeClientsList.clear();
        activeChatRooms.clear();
    }

    public boolean isLeader() {
        return ServersState.getInstance().getSelfServerId() == LeaderState.getInstance().getLeaderId();
    }

    public boolean isElectedLeader() {
//        System.out.println(FastBullyAlgorithm.isLeader + " " + isLeader());
        return FastBullyAlgorithm.isLeader && isLeader();
    }


}
