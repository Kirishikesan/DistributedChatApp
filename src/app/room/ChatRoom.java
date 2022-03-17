package app.room;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

public class ChatRoom {
    private final String roomid;
    private final String owner;
//    private CopyOnWriteArrayList<String> members = new CopyOnWriteArrayList<String>();
    public ConcurrentHashMap<String, String> members = new ConcurrentHashMap<>();

    public ChatRoom(String roomId, String owner) {
        this.roomid = roomId;
        this.owner = owner;
        members.put(owner, owner);
    }

    public void addMember(String clientId, String roomId) {
        System.out.println("Before add: "+roomId);
        members.forEach((key,value)->{
            System.out.println(value);
        });
        members.put(clientId, clientId);
        System.out.println("After add: "+roomId);
        members.forEach((key,value)->{
            System.out.println(value);
        });
    }

    public void removeMember(String clientId, String roomId) {
        System.out.println("Before Remove: "+roomId);
        members.forEach((key,value)->{
            System.out.println(value);
        });
        members.remove(clientId);
//        List<String> remainingClients= new ArrayList<>();
//        List<Object> iter = Arrays.asList(members.toArray());
//        iter.forEach(member->{
//            if(!(Objects.equals((String) member, clientId)))remainingClients.add((String) member);
//        });
//        members.clear();
//        members.addAll(remainingClients);
        System.out.println("After Remove"+roomId);
        members.forEach((key,value)->{
            System.out.println(value);
        });
    }

    public String getRoomId() {
        return roomid;
    }

    public String getOwner() {
        return owner;
    }

    public ConcurrentHashMap<String, String> getMembers() {
        return members;
    }
}
