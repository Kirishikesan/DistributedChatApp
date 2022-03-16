package app.room;

import java.util.concurrent.CopyOnWriteArrayList;

public class ChatRoom {
    private final String roomid;
    private final String owner;
    private CopyOnWriteArrayList<String> members = new CopyOnWriteArrayList<String>();

    public ChatRoom(String roomId, String owner) {
        this.roomid = roomId;
        this.owner = owner;
        members.add(owner);
        System.out.println("Create chatroom " + roomId);
    }

    public void addMember(String clientId) {
        members.add(clientId);
    }

    public void removeMember(String clientId) {
        int index = members.indexOf(clientId);
        if (index != -1) members.remove(index);
    }

    public String getRoomId() {
        return roomid;
    }

    public String getOwner() {
        return owner;
    }

    public CopyOnWriteArrayList<String> getMembers() {
        return members;
    }
}
