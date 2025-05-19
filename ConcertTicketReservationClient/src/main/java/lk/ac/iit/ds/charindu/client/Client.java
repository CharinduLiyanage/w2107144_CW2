package lk.ac.iit.ds.charindu.client;

import io.grpc.ConnectivityState;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import lk.ac.iit.ds.charindu.grpc.generated.EventCommandServiceGrpc;
import lk.ac.iit.ds.charindu.grpc.generated.EventQueryServiceGrpc;
import lk.ac.iit.ds.charindu.registration.NameServiceClient;

import java.io.IOException;
import java.util.*;

public abstract class Client {
    private static final String NAME_SERVICE_ADDRESS = "http://127.0.0.1:2379";
    private static final String SERVER_NAME = "Concert-Ticket-Reservation-Server";
    private static final String initServerID = "server";
    protected String host;
    protected int port;
    protected ManagedChannel channel = null;
    private static final int initServerPort = 11436;
    private static Map<String, NameServiceClient.ServiceDetails> serverDetailMap = new HashMap<>();;
    private static String regServerID ;


    protected EventQueryServiceGrpc.EventQueryServiceBlockingStub eventQueryServiceBlockingStub;
    protected EventCommandServiceGrpc.EventCommandServiceBlockingStub eventCommandServiceBlockingStub;

    public Client() throws IOException, InterruptedException {
        fetchServerDetails();
//        NameServiceClient.ServiceDetails serviceDetails = selectServer();
//        host = serviceDetails.getIPAddress();
//        port = serviceDetails.getPort();
    }

    private void fetchServerDetails() throws IOException, InterruptedException {
        NameServiceClient client = new NameServiceClient(NAME_SERVICE_ADDRESS);
        NameServiceClient.ServiceDetails serviceDetails = client.findService("Concert-Ticket-Reservation-Server");
        host = serviceDetails.getIPAddress();
        port = serviceDetails.getPort();
        System.out.println("server details: " + host + ":" + port);

//        int serverNo = 0;
//        String serverName ;
//        int port = initServerPort-1;
//        NameServiceClient.ServiceDetails serviceDetails = null;
//        NameServiceClient client;
//        System.out.println(" ~~ Loading Servers....");
//        try {
//            client = new NameServiceClient(NAME_SERVICE_ADDRESS);
//            do {
//                serverNo += 1;
//                serverName = initServerID + serverNo;
//                port += 1;
//                serviceDetails = client.findOnceService(serverName);
//                if (serviceDetails != null && !serverDetailMap.containsKey(serverName)) {
//                    serverDetailMap.put(serverName, serviceDetails);
//                }
//            } while (serviceDetails != null);
//        }catch (Exception e){
//            System.out.println("Server Detail Update Failed! due to: "+e.getMessage());
//            throw new RuntimeException(e.getMessage());
//        }
    }

    protected void waitForConnection() throws IOException, InterruptedException {
        ConnectivityState state = channel.getState(true);
        while (state != ConnectivityState.READY) {
            System.out.println("Waiting for connection...");
            fetchServerDetails();
            initializeConnection();
            Thread.sleep(1000);
            state = channel.getState(true);
        }
    }

    protected void initializeConnection() {
        System.out.println("Initializing Connecting to server at " + host + ":" + port);
        channel = ManagedChannelBuilder.forAddress(host, port).usePlaintext().build();
        eventQueryServiceBlockingStub = EventQueryServiceGrpc.newBlockingStub(channel);
        eventCommandServiceBlockingStub = EventCommandServiceGrpc.newBlockingStub(channel);
        channel.getState(true);

//        System.out.println("Initializing Connecting to server at " + host + ":" + port);
//        channel = ManagedChannelBuilder
//                .forAddress(host, port)
//                .usePlaintext()
//                .build();
//        eventQueryServiceBlockingStub = EventQueryServiceGrpc.newBlockingStub(channel);
//        eventCommandServiceBlockingStub = EventCommandServiceGrpc.newBlockingStub(channel);
    }

    protected void closeConnection() {
        channel.shutdown();
    }

    protected abstract void processUserRequests();

    public void start() {
        initializeConnection();
        processUserRequests();
        closeConnection();
    }

//    private static NameServiceClient.ServiceDetails selectServer(){
//        NameServiceClient.ServiceDetails serviceDetails;
//        List<String> name = new ArrayList<>();
//        int serverNo= 0;
//        Scanner scanner1 = new Scanner(System.in);
//        System.out.println("========== Select Connecting Server ========== ");
//        for(String serverName : serverDetailMap.keySet()){
//            serverNo++;
//            System.out.println("["+serverNo+"] "+serverName);
//            name.add(serverName);
//        }
//        System.out.print(" Select the number for connecting server: ");
//        int number = Integer.parseInt(scanner1.nextLine().trim());
//        System.out.println("================================================");
//        regServerID = name.get(number-1);
//        return serverDetailMap.get(name.get(number-1));
//    }

//    private void initializeConnection() {
//        System.out.println("Initializing Connecting to server at " + serverIP + ":" + serverPort);
//        channel = ManagedChannelBuilder
//                .forAddress(serverIP, serverPort)
//                .usePlaintext()
//                .build();
//        addItemServiceBlockingStub = AddItemServiceGrpc.newBlockingStub(channel);
//        deleteItemServiceBlockingStub = DeleteItemServiceGrpc.newBlockingStub(channel);
//        getMyItemServiceBlockingStub = GetMyItemServiceGrpc.newBlockingStub(channel);
//        updateItemServiceBlockingStub = UpdateItemServiceGrpc.newBlockingStub(channel);
//    }
}
