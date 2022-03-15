package app.room;
import java.util.concurrent.CopyOnWriteArrayList;

public class ChatRoom {
    private String serverid;
    private String roomid;
    private String owner;
    private CopyOnWriteArrayList<String> members = new CopyOnWriteArrayList<String>();

    public ChatRoom(String serverid, String roomid, String owner) {
        this.serverid = serverid;
        this.roomid = roomid;
        this.owner = owner;
        System.out.println("Create chatroom "+ roomid);
    }

    void add_members(String client){
        members.add(client);
    }

    public String getServerid() {
        return serverid;
    }

    public String getRoomid() {
        return roomid;
    }

    public String getOwner() {
        return owner;
    }

    public CopyOnWriteArrayList<String> getMembers() {
        return members;
    }
}
