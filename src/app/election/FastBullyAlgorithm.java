package app.election;

import app.response.ClientResponse;
import app.response.ServerResponse;
import app.server.Server;
import app.server.ServerMessage;
import app.serversState.ServersState;
import org.json.simple.JSONObject;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;


public class FastBullyAlgorithm implements Runnable{

    String operation;

    final int t1 = 7000; // threashold for respond from coordinator

    public FastBullyAlgorithm(String operation) {
        this.operation = operation;
    }

    public static void initializeLeader(){
        // start initial election
        System.out.println("start FastBullyAlgorithm");
        Runnable sender = new FastBullyAlgorithm("election");
        new Thread(sender).start();
    }


    @Override
    public void run() {
        switch (operation){
            case "heartbeat":
                //
            case "timer":

                break;

            case "election":
                try {
                    election();
                } catch( Exception e ) {
                    System.out.println( "WARN : fail to send election request" );
                }
                break;

            case "answer":
                try {
                    answer();
                } catch( Exception e ) {
                    System.out.println( "WARN : fail to send ok message" );
                }
                break;

            case "coordination":
                try {
                    coordination();
                } catch( Exception e ) {
                    System.out.println( "WARN : fail to send coordination message" );
                }
                break;
        }
    }

    public static void election(){
        System.out.println("INFO : start election");

        AtomicInteger failedRequests = new AtomicInteger();
        int selfServerId = ServersState.getInstance().getSelfServerId();
        ConcurrentHashMap<Integer, Server> serversMap = ServersState.getInstance().getServersMap();

        serversMap.forEach((serverKey, destServer) -> {
            if(serverKey > selfServerId){
                try {
                    JSONObject createElectionReqObj = ServerResponse.createElectionRequest(selfServerId);
                    ServerMessage.sendServer(createElectionReqObj, destServer);

                }catch(Exception e){
                    System.out.println("WARN : Server s"+destServer.getserverId() +
                            " has failed, cannot send election request");
                    failedRequests.getAndIncrement();
                }
            }
        });

    }

    public static void answer(){

    }

    public static void coordination(){

    }

}
