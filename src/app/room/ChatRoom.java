package app.room;

import app.server.Client;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class ChatRoom {
    private final String roomid;
    private final String owner;
    public ConcurrentHashMap<String, Client> members;

    public ChatRoom(String roomId, Client owner) {
        this.roomid = roomId;
        if (Objects.isNull(owner)) {
            this.owner = "default";
            owner = new Client();
        } else {
            this.owner = owner.clientId;
        }
        members = new ConcurrentHashMap<>();
        members.put(this.owner, owner);
    }

    public void addMember(Client client) {
        members.put(client.clientId, client);
    }

    public void addMembers(ConcurrentHashMap<String, Client> clients) {
        members.putAll(clients);
    }

    public void removeMember(Client client) {
        members.remove(client.clientId);
    }

    public String getRoomId() {
        return roomid;
    }

    public String getOwner() {
        return owner;
    }

    public ConcurrentHashMap<String, Client> getMembers() {
        return members;
    }
}
