package app.room;

import app.server.ClientHandlerThread;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class ChatRoom {
    private final String roomid;
    private final String owner;
    public ConcurrentHashMap<String, ClientHandlerThread> members;

    public ChatRoom(String roomId, ClientHandlerThread owner) {
        this.roomid = roomId;
        if (Objects.isNull(owner)) {
            this.owner = "default";
            owner = new ClientHandlerThread();
        } else {
            this.owner = owner.clientId;
        }
        members = new ConcurrentHashMap<>();
        members.put(this.owner, owner);
    }

    public void addMember(ClientHandlerThread clientHandlerThread) {
        members.put(clientHandlerThread.clientId, clientHandlerThread);
    }

    public void addMembers(ConcurrentHashMap<String, ClientHandlerThread> clients) {
        members.putAll(clients);
    }

    public void removeMember(ClientHandlerThread clientHandlerThread) {
        members.remove(clientHandlerThread.clientId);
    }

    public String getRoomId() {
        return roomid;
    }

    public String getOwner() {
        return owner;
    }

    public ConcurrentHashMap<String, ClientHandlerThread> getMembers() {
        return members;
    }
}
