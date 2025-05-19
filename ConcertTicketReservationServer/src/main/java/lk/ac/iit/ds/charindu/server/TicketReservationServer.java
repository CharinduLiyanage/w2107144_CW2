package lk.ac.iit.ds.charindu.server;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.Server;
import io.grpc.ServerBuilder;
import lk.ac.iit.ds.charindu.grpc.generated.Event;
import lk.ac.iit.ds.charindu.grpc.generated.EventQueryServiceGrpc;
import lk.ac.iit.ds.charindu.grpc.generated.GetEventsRequest;
import lk.ac.iit.ds.charindu.grpc.generated.GetEventsResponse;
import lk.ac.iit.ds.charindu.registration.NameServiceClient;
import lk.ac.iit.ds.charindu.server.services.EventCommandServiceImpl;
import lk.ac.iit.ds.charindu.server.services.EventQueryServiceImpl;
import lk.ac.iit.ds.charindu.server.utility.LeaderCampaignThread;
import lk.ac.iit.ds.charindu.synchronization.DistributedMasterLock;
import lk.ac.iit.ds.charindu.transaction.DistributedTx;
import lk.ac.iit.ds.charindu.transaction.DistributedTxCoordinator;
import lk.ac.iit.ds.charindu.transaction.DistributedTxListener;
import lk.ac.iit.ds.charindu.transaction.DistributedTxParticipant;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.ZooDefs;
import org.apache.zookeeper.ZooKeeper;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

public class TicketReservationServer {

    public static final String ZOOKEEPER_ADDRESS = "127.0.0.1:2181";
    public static final String NAME_SERVICE_ADDRESS = "http://127.0.0.1:2379";
    public static final String SERVER_NAME = "Concert-Ticket-Reservation-Server";
    public static final String LOCK_NAME = "Concert-Ticket-Reservation-Lock";
    private static final String initServerID = "server";
    private static final String initServerIp = "127.0.0.1";
    private static String regServerID;
    private static int regServerPort;
    private final AtomicBoolean serverReady = new AtomicBoolean(false);
    private int serverPort;
    private AtomicBoolean isLeader = new AtomicBoolean(false);
    private DistributedMasterLock leaderLock;
    private byte[] leaderData;
    private DistributedTx transaction;
    private EventQueryServiceImpl eventQueryService;
    private EventCommandServiceImpl eventCommandService;

    private Map<String, Event> db = new HashMap<>();


    public TicketReservationServer(String host, int serverPort) throws IOException, InterruptedException, KeeperException {
        this.serverPort = serverPort;
        leaderLock = new DistributedMasterLock(LOCK_NAME, (host + ":" + serverPort));

        eventQueryService = new EventQueryServiceImpl(this);
        eventCommandService = new EventCommandServiceImpl(this);

        transaction = new DistributedTxParticipant();
        transaction.setTxnListener(eventCommandService);
    }


    public static void main(String[] args) {
        try {
            if (args.length != 1) {
                System.out.println("Usage executable-name <port>");
                return;
            }

            System.out.println("Using port " + args[0]);
            int serverPort = Integer.parseInt(args[0].trim());

            registerToETCD(initServerIp, serverPort); // register to ETCD
            System.out.println("Initiating Reservation Server.....");

            DistributedMasterLock.setZooKeeperUrl(ZOOKEEPER_ADDRESS);
            DistributedTx.setZooKeeperURL(ZOOKEEPER_ADDRESS);

            TicketReservationServer server = new TicketReservationServer(initServerIp, serverPort);
            server.tryToBeLeader();  // Initiate competition to become master
            server.startServer();  //Initiate services and start server
        } catch (Exception e) {
            System.out.println("Internal Sever Failure: " + e.getMessage());
        }
    }

    private static void registerToETCD(String ip, int port) throws IOException, InterruptedException {
        int serverNo = 0;
        String serverName;
        boolean foundSpot = false;
//        int serverPort = port - 1;
        NameServiceClient.ServiceDetails serviceDetails = null;
        List<String> connectedNodeData;

        try {
//            NameServiceClient client = new NameServiceClient(NAME_SERVICE_ADDRESS);
//            connectedNodeData = getZookeeperConnectedNodes();
//            do {
//                serverNo += 1;
//                serverName = initServerID + serverNo;
//                serverPort += 1;
//                serviceDetails = client.findOnceService(serverName);
//                if (serviceDetails != null) {   // Check With zookeeper connected nodes is there any available port
//                    if (!connectedNodeData.contains(String.valueOf(serviceDetails.getPort()))) {
//                        foundSpot = true;
//                    }
//                } else {
//                    foundSpot = true;
//                }
//
//            } while (!foundSpot);
//            regServerID = serverName;
//            regServerPort = serverPort;
//            client.registerService(serverName, ip, serverPort, "tcp");


            regServerID = SERVER_NAME;
            regServerPort = port;
            NameServiceClient client = new NameServiceClient(NAME_SERVICE_ADDRESS);
            client.registerService(regServerID, ip, regServerPort, "tcp");

            System.out.println("~~~~~ Server Registered Under| Sever Name: " + regServerID + " Server Address: " + ip + regServerPort);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static List<String> getZookeeperConnectedNodes()
            throws IOException, InterruptedException, KeeperException {
        String rootPath = "/" + SERVER_NAME;
        ZooKeeper zk = new ZooKeeper(ZOOKEEPER_ADDRESS, 5000, event -> {
        });

        // Check if the root node exists; if not, create it
        if (zk.exists(rootPath, false) == null) {
            zk.create(
                    rootPath,
                    new byte[0],
                    ZooDefs.Ids.OPEN_ACL_UNSAFE,
                    CreateMode.PERSISTENT
            );
        }

        List<String> children = zk.getChildren(rootPath, false);
        List<String> connectedPorts = new ArrayList<>();

        for (String child : children) {
            byte[] data = zk.getData(rootPath + "/" + child, false, null);
            // assuming your data is "ip:port"
            String nodeInfo = new String(data);
            String port = nodeInfo.split(":")[1].trim();
            connectedPorts.add(port);
        }

        return connectedPorts;
    }

    private void startServer() throws IOException, InterruptedException, KeeperException {
        Server server = ServerBuilder
                .forPort(serverPort)
                .addService(eventQueryService)
                .addService(eventCommandService)
                .build();
        server.start();

        syncServer();
        serverReady.set(true);
        System.out.println("Initiating Reservation Server has Succeed! : server running on : " + initServerIp + " : " + serverPort);
        server.awaitTermination();
    }

    private void syncServer() throws InterruptedException, KeeperException {
        ManagedChannel channel;
        EventQueryServiceGrpc.EventQueryServiceBlockingStub eventQueryServiceBlockingStub;
        GetEventsRequest request;
        GetEventsResponse response;

        System.out.println("Starting System Sync..");
        byte[] currentMasterNodeData = leaderLock.getMasterData();
        String[] decodedMasterAddress = (new String(currentMasterNodeData)).split(":");
        String masterServerIP = decodedMasterAddress[0].trim();
        int masterServerPort = Integer.parseInt(decodedMasterAddress[1].trim());
        if (!Arrays.equals(currentMasterNodeData, leaderLock.getServerData())) { //Check if there are other registered servers and Master servers
            System.out.println("Initializing connecting to Master Server to Sync at: " + masterServerIP + " : " + masterServerPort);
            channel = ManagedChannelBuilder
                    .forAddress(masterServerIP, masterServerPort)
                    .usePlaintext()
                    .build();
            eventQueryServiceBlockingStub = EventQueryServiceGrpc.newBlockingStub(channel);

            System.out.println("Connected to the Master Server at: " + masterServerIP + " : " + masterServerPort + " | Start sync data...");

            request = GetEventsRequest
                    .newBuilder()
                    .build();

            response = eventQueryServiceBlockingStub.getEvents(request);

            List<Event> eventsFromMaster = response.getEventsList();

            System.out.println("Updating Local DB...");
            if (!eventsFromMaster.isEmpty()) {
                for (Event event : eventsFromMaster) {
                    db.put(event.getId(), event);
                    System.out.println("DB get synced with Master successfully!");
                }
            } else {
                System.out.println("No Items to update!");
            }
        } else {
            System.out.println("System Sync Skipped! Due to current server is the master");
        }
    }

    public boolean isLeader() {
        return isLeader.get();
    }

    public boolean isServerReady() {
        return serverReady.get();
    }

    private void tryToBeLeader() {
        Thread leaderCampaignThread = new Thread(new LeaderCampaignThread(leaderLock, this));
        leaderCampaignThread.start();
    }

    public void beTheLeader() {
        System.out.println("I got the leader lock. Now acting as primary");
        isLeader.set(true);
        transaction = new DistributedTxCoordinator();
        transaction.setTxnListener(eventCommandService);
    }

    public synchronized String[] getCurrentLeaderData() throws InterruptedException, KeeperException {
        byte[] masterData = leaderLock.getMasterData();
        return (new String(masterData)).split(":");
    }

    public synchronized void setCurrentLeaderData(byte[] leaderData) {
        this.leaderData = leaderData;
    }

//    public void startDistributedTxn(String id, DistributedTxListener listener) {
//        try {
//            transaction.setTxnListener(listener);
//            transaction.start(id, String.valueOf(UUID.randomUUID()));
//            listener.setTxnStarted(true);
//        } catch (Exception e) {
//            listener.setTxnStarted(false);
//            throw new RuntimeException("Starting Distributed Txn Failed! Due to: " + e.getMessage());
//        }
//    }

    public List<String[]> getOthersData() throws KeeperException, InterruptedException {
        List<String[]> result = new ArrayList<>();
        List<byte[]> slaveServerData = leaderLock.getSlaveData();

        if (!slaveServerData.isEmpty()) {
            slaveServerData.forEach(data -> result.add((new String(data)).split(":")));
        }
        return result;
    }

    public DistributedTx getTransaction() {
        return transaction;
    }

    public void perform() throws InterruptedException, KeeperException {
        ((DistributedTxCoordinator) transaction).perform();
    }

    public void sendGlobalAbort()  {
        ((DistributedTxCoordinator) transaction).sendGlobalAbort();
    }

    public void voteCommit() {
        ((DistributedTxParticipant) transaction).voteCommit();
    }

    public void voteAbort() {
        ((DistributedTxParticipant) transaction).voteAbort();
    }

    // Methods for services.

    public Event getEvent(String eventId) {
        return (Event) db.get(eventId);
    }


    public List<Event> getEvents() {
        return new ArrayList<>(db.values());
    }

    public void addEvent(Event event) {
        db.put(event.getId(), event);
    }

    public void updateEvent(Event event) {
        System.out.println(new ArrayList<>(db.values()));
        System.out.println(db.keySet());
        db.remove(event.getId());
        db.put(event.getId(), event);
    }

//    public void cancelEvent(String eventId) {
//        events.remove(eventId);
//    }
//
//    public void addTicketStocks(AddTicketStockRequest request) {
//        String eventId = request.getEventId();
//        Event currentEvent = getEvent(eventId);
//
//        if (currentEvent != null) {
//            // Number of tickets to add
//            int ticketsToAdd = request.getCount();
//            System.out.println("Adding " + ticketsToAdd + " tickets");
//
//            if (request.getAfterParty()) {
//                // Adding after party tickets
//
//                // Number of ticket slots available
//                int availableTicketSlots = currentEvent.getAfterPartyTicketsTotal() - currentEvent.getAfterPartyTicketsSold() - currentEvent.getAfterPartyTicketsAvailable();
//                System.out.println("Available Ticket Slots: " + availableTicketSlots);
//
//
//                if (ticketsToAdd <= availableTicketSlots) {
//                    Event newEvent = Event
//                            .newBuilder()
//                            .mergeFrom(currentEvent)
//                            .setAfterPartyTicketsAvailable(currentEvent.getAfterPartyTicketsAvailable() + ticketsToAdd)
//                            .build();
//                    updateEvent(newEvent);
//                } else {
//                    System.err.println("Tickets not enough to be added");
//                }
//            } else if (currentEvent.getTicketTiersMap().containsKey(request.getTier())) {
//                // Adding tickets to a ticket tier
//                TicketTier tier = currentEvent.getTicketTiersMap().get(request.getTier());
//
//                // Number of ticket slots available
//                int availableTicketSlots = tier.getTicketsTotal() - tier.getTicketsSold() - tier.getTicketsAvailable();
//
//                if (ticketsToAdd <= availableTicketSlots) {
//                    TicketTier newTier = TicketTier
//                            .newBuilder()
//                            .mergeFrom(tier)
//                            .setTicketsAvailable(tier.getTicketsAvailable() + ticketsToAdd)
//                            .build();
//
//                    Event newEvent = Event
//                            .newBuilder()
//                            .mergeFrom(currentEvent)
//                            .putTicketTiers(newTier.getId(), newTier)
//                            .setEventTicketsAvailable(currentEvent.getEventTicketsAvailable() + ticketsToAdd)
//                            .build();
//
//                    updateEvent(newEvent);
//                } else {
//                    System.err.println("Tickets not enough to be added");
//                }
//            }
//        }
//    }

}
