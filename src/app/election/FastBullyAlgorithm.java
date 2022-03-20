package app.election;

import app.leaderState.LeaderState;
import app.response.ClientResponse;
import app.response.ServerResponse;
import app.server.Server;
import app.server.ServerMessage;
import app.serversState.ServersState;
import org.json.simple.JSONObject;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;


public class FastBullyAlgorithm implements Runnable{

    String operation;

    final int t1 = 15000; // threshold for respond from coordinator
    final int t2 = 20000; // threshold for respond from candidates - answer messages
    final int t3 = 35000; // threshold for respond from win candidate - coordinator messages
    final int t4 = 30000; // threshold for respond from either a nomination or a coordinator

    static int sourceServerId=-1;

    static volatile boolean electionStatus = false;
    static volatile boolean answerStatus = false;
    static volatile boolean nominationStatus = false;
    static volatile boolean coordinatorStatus = false;

    public static volatile boolean leaderUpdateComplete = false;

    static volatile int highestPriorityServerId = -1;

    static volatile ConcurrentHashMap<Integer, Server> answersMap = new ConcurrentHashMap<>();

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
            case "wait_answer":                     //T2
                try {
                    Thread.sleep( t2 );

                    if(answerStatus){

                        //  2.4 If the answer messages are received within T2
                        setUpNominator();

                    }else{
                        //  2.3 If no answer within T2
                        setUpSelfLeader();
                    }


                } catch (InterruptedException e) {
                    System.out.println( "INFO : Exception in wait_answer thread" );
                }

                break;

            case "wait_coordination":    //T3
                try {
                    Thread.sleep( t3 );

                    //  2.6 If no coordinator message within T3
                    if(nominationStatus && !coordinatorStatus){
                        System.out.println( "INFO : no coordinator message within T3" );
                        setUpNominator();
                    }
                } catch (InterruptedException e) {
                    System.out.println( "INFO : Exception in wait_coordination thread" );
                }
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
                    answersMap.clear();
                    sendElection();
                } catch( Exception e ) {
                    System.out.println( "WARN : fail to send election request" );
                }
                break;

            case "answer":
                try {
                    sendAnswer();
                } catch( Exception e ) {
                    System.out.println( "WARN : fail to send ok message" );
                }
                break;

            case "nomination":
                try {
                    sendNomination();
                } catch( Exception e ) {
                    System.out.println( "WARN : fail to send coordination message" );
                }
                break;

            case "coordination":
                try {
                    sendCoordination();
                } catch( Exception e ) {
                    System.out.println( "WARN : fail to send coordination message" );
                }
                break;
        }
    }

    public static void sendElection(){
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
        Runnable procedure = new FastBullyAlgorithm("wait_answer");
        new Thread(procedure).start();

    }

    public static void sendAnswer(){
        try {
            ConcurrentHashMap<Integer, Server> serversMap = ServersState.getInstance().getServersMap();
            Server destinationServer = serversMap.get(sourceServerId);
            int selfServerId = ServersState.getInstance().getSelfServerId();

            JSONObject createElectionReqObj = ServerResponse.createAnswerRequest(selfServerId);
            ServerMessage.sendServer(createElectionReqObj, destinationServer);
            System.out.println("INFO : Server s"+ selfServerId +" has sent answer message to s" + sourceServerId);

        }catch(Exception e){
            System.out.println("INFO : Server s"+ ServersState.getInstance().getSelfServerId() +" has failed. answer message can not be sent to " + sourceServerId);
        }
    }

    public static void sendNomination(){
        nominationStatus = true;
        try {
            ConcurrentHashMap<Integer, Server> serversMap = ServersState.getInstance().getServersMap();
            Server nominatedServer = serversMap.get(highestPriorityServerId);
            int selfServerId = ServersState.getInstance().getSelfServerId();

            JSONObject createNominationReqObj = ServerResponse.createNominationRequest(selfServerId);
            ServerMessage.sendServer(createNominationReqObj, nominatedServer);
            System.out.println("INFO : Server s"+ selfServerId +" has sent nomination message to s" + highestPriorityServerId);

        }catch(Exception e){
            System.out.println("INFO : Server s"+ ServersState.getInstance().getSelfServerId() +" has failed to send nomination message");
        }

        Runnable procedure = new FastBullyAlgorithm("wait_coordination" );
        new Thread( procedure ).start();
    }

    public static void sendCoordination(){

        AtomicInteger failedRequestCount = new AtomicInteger();
        int selfServerId = ServersState.getInstance().getSelfServerId();
        ConcurrentHashMap<Integer, Server> serversMap = ServersState.getInstance().getServersMap();

        //  2.3.1 Pi sends a coordinator message to other processes with lower priority number
        //  3.4.1 Pj sends a coordinator message to all the processes with lower priority numbers
        serversMap.forEach((serverKey, destinationServer) -> {
            if(serverKey < selfServerId){
                try {
                    JSONObject createCoordinationReqObj = ServerResponse.createCoordinationRequest(selfServerId);
                    ServerMessage.sendServer(createCoordinationReqObj, destinationServer);
                    System.out.println("INFO : Server s"+ selfServerId +" has sent coordinator message to s" + destinationServer.getserverId());

                }catch(Exception e){
                    System.out.println("WARN : Server s"+destinationServer.getserverId() + " has failed, cannot send coordinator request");
                    failedRequestCount.getAndIncrement();
                }
            }
        });

//        failedRequestCount
    }



    public static void setUpNominator(){

        if(answersMap.isEmpty()){

            //  2.6.2 If no process left to choose, Pi restarts the election procedure
            if(!electionStatus){
                electionStatus = true;
                answerStatus = false;
                nominationStatus = false;
                coordinatorStatus = false;

                Runnable procedure = new FastBullyAlgorithm("election");
                new Thread(procedure).start();
            }


        }else{

            //  2.4.1 Pi determines the highest priority number of the answering processes
            List<Integer> answerIds = new ArrayList<Integer>(answersMap.keySet());
            System.out.println( "INFO : nomination lists - " + Arrays.toString(answerIds.toArray()) );

            highestPriorityServerId = Collections.max(answerIds);
            answersMap.remove(highestPriorityServerId);

            System.out.println( "INFO : Server s" + highestPriorityServerId + " is selected for nomination " );

            //  2.4.2 Pi sends a nomination message to this process
            Runnable procedure_1 = new FastBullyAlgorithm("nomination" );
            new Thread( procedure_1 ).start();


        }

    }

    public static void setUpSelfLeader(){

        LeaderState.getInstance().setLeaderId( ServersState.getInstance().getSelfServerId() );

        //  2.3.2 Pi stops its election procedure
        //  3.4.2 Pj stops its election procedure - coordinatorStatus->true in leader
        electionStatus = false;
        coordinatorStatus = true;

        System.out.println( "INFO : Server s" + LeaderState.getInstance().getLeaderId() + " is set as leader " );

        LeaderState.getInstance().resetLeader(); // reset leader lists when newly elected

        Runnable procedure = new FastBullyAlgorithm("coordination" );
        new Thread( procedure ).start();
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
                int answerIdentity = Integer.parseInt(requestObject.get( "identity" ).toString());
                System.out.println( "INFO : Received answer from s" + answerIdentity );

                ConcurrentHashMap<Integer, Server> serversMap = ServersState.getInstance().getServersMap();
                Server answerServer = serversMap.get(answerIdentity);
                answersMap.put(answerIdentity, answerServer);

                break;

            case "nomination":

                //  3.4 If a process Pj(i<j) receives the nomination message from Pi
                setUpSelfLeader();
                break;


            case "coordination":
                coordinatorStatus = true;
                System.out.println( "INFO : Receive coordination message & Server s" + Integer.parseInt(requestObject.get( "identity" ).toString()) + " is Admit as leader " );
                break;

        }
    }

}
