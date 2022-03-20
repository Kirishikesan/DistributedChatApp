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

    final int t1 = 7000; // threshold for respond from coordinator
    final int t2 = 10000; // threshold for respond from candidates - answer messages
    final int t4 = 15000; // threshold for respond from either a nomination or a coordinator

    static int sourceServerId=-1;

    static volatile boolean electionStatus = false;
    static volatile boolean answerStatus = false;
    static volatile boolean nominationStatus = false;
    static volatile boolean coordinatorStatus = false;

    public FastBullyAlgorithm(String operation) {
        this.operation = operation;
    }

    public static void initializeLeader(){
        // start initial election
        System.out.println("start FastBullyAlgorithm");
        Runnable procedure = new FastBullyAlgorithm("election");
        new Thread(procedure).start();
    }


    @Override
    public void run() {
        switch (operation){
            case "heartbeat":
                //
            case "wait_answer":

                break;

            case "wait_coordination_nomination":    //T4
                try {
                    Thread.sleep( t4 );

                    //  3.3 If no coordinator message or nomination message within T4
                    if( !nominationStatus && !coordinatorStatus){
                        System.out.println( "INFO : no coordinator message or nomination message -> restarts the procedure" );

                        //  3.3.1 Pj restarts the procedure
                        electionStatus = false;
                        answerStatus = false;

                        Runnable procedure = new FastBullyAlgorithm("election");
                        new Thread(procedure).start();

                    }

                } catch (InterruptedException e) {
                    System.out.println( "INFO : Exception in wait_coordination_nomination thread" );
                }
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

        AtomicInteger failedRequestCount = new AtomicInteger();
        int selfServerId = ServersState.getInstance().getSelfServerId();
        ConcurrentHashMap<Integer, Server> serversMap = ServersState.getInstance().getServersMap();

        // 2.1 Pi sends an election message to every process with higher priority number
        serversMap.forEach((serverKey, destinationServer) -> {
            if(serverKey > selfServerId){
                try {
                    JSONObject createElectionReqObj = ServerResponse.createElectionRequest(selfServerId);
                    ServerMessage.sendServer(createElectionReqObj, destinationServer);

                }catch(Exception e){
                    System.out.println("WARN : Server s"+destinationServer.getserverId() + " has failed, cannot send election request");
                    failedRequestCount.getAndIncrement();
                }
            }
        });

        //  2.2 Pi waits for answer messages for the interval T2
        int  priorityServerCount = serversMap.size() - selfServerId;
        if(priorityServerCount == failedRequestCount.intValue()){
            if(!electionStatus){

                electionStatus = true;
                answerStatus = false;

                Runnable procedure = new FastBullyAlgorithm("wait_answer");
                new Thread(procedure).start();
            }
        }

    }

    public static void answer(){
        try {
            ConcurrentHashMap<Integer, Server> serversMap = ServersState.getInstance().getServersMap();
            Server destinationServer = serversMap.get(sourceServerId);
            int selfServerId = ServersState.getInstance().getSelfServerId();

            JSONObject createElectionReqObj = ServerResponse.createAnswerRequest(selfServerId);
            ServerMessage.sendServer(createElectionReqObj, destinationServer);
            System.out.println("INFO : Server s"+ sourceServerId +" has sent answer message");

        }catch(Exception e){
            System.out.println("INFO : Server s"+ sourceServerId +" has failed. answer message can not be sent");
        }
    }

    public static void coordination(){

    }

    public static void handleRequest(JSONObject requestObject){
        String request = (String) requestObject.get( "request" );

        switch (request) {
            case "heartbeat":
                break;

            case "election":    // 3 If a process Pj(i<j) receives an election message from Pi
                
                sourceServerId = Integer.parseInt(requestObject.get( "identity" ).toString());
                System.out.println("INFO : Received election request from s" + sourceServerId );

                //  3.1 Pj sends an answer message to Pi
                int selfServerId = ServersState.getInstance().getSelfServerId();
                if( selfServerId > sourceServerId ) {
                    Runnable procedure = new FastBullyAlgorithm( "answer" );
                    new Thread( procedure ).start();
                }

                //  3.2 Pj waits for either a nomination or a coordinator message for the interval T4
                Runnable procedure = new FastBullyAlgorithm("wait_coordination_nomination");
                new Thread(procedure).start();

                break;

            case "answer":

                answerStatus = true;
                int senderID = Integer.parseInt(requestObject.get( "identity" ).toString());
                System.out.println( "INFO : Received answer from s" + senderID );
                break;

            case "coordination":
                break;


        }
    }

}
