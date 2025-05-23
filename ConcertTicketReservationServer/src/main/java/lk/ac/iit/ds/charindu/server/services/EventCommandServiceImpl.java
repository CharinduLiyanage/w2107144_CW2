package lk.ac.iit.ds.charindu.server.services;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.stub.StreamObserver;
import javafx.util.Pair;
import lk.ac.iit.ds.charindu.grpc.generated.*;
import lk.ac.iit.ds.charindu.server.TicketReservationServer;
import lk.ac.iit.ds.charindu.transaction.DistributedTxListener;
import org.apache.zookeeper.KeeperException;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class EventCommandServiceImpl extends EventCommandServiceGrpc.EventCommandServiceImplBase implements DistributedTxListener {
    private TicketReservationServer server;
    private Pair<String, Object> tempDataHolder;
    private ManagedChannel channel = null;
    private EventCommandServiceGrpc.EventCommandServiceBlockingStub clientStub = null;
    private boolean isTxnStarted;

    private boolean transactionStatus = false;


    public EventCommandServiceImpl(TicketReservationServer server) {
        this.server = server;
    }

    private void startDistributesTx(String operationId, Object data) {
        try {
            server.getTransaction().start(operationId, String.valueOf(UUID.randomUUID()));
            tempDataHolder = new Pair<>(operationId, data);
            isTxnStarted = true;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void updateSecondaryServer(String operationId, Object data) throws InterruptedException, KeeperException {
        System.out.println("Updating Secondary Servers");
        List<String[]> othersData = server.getOthersData();
        System.out.println("Secondary Servers: " + othersData.size());
        for (String[] serverData : othersData) {
            if (serverData.length < 2) {
                System.err.println("Invalid secondary server data: " + Arrays.toString(serverData));
                continue;
            }
            String host = serverData[0];
            int port = Integer.parseInt(serverData[1]);

            System.out.println("Secondary Server: " + host + ":" + port);

            try {
                channel = ManagedChannelBuilder
                        .forAddress(host, port)
                        .usePlaintext()
                        .build();
                clientStub = EventCommandServiceGrpc.newBlockingStub(channel);

                // Create a request based on the operation type.
                if (operationId.startsWith("add_event_")) {
                    System.out.println("Adding event");
                    AddEventRequest originalRequest = (AddEventRequest) data;
                    AddEventRequest request = AddEventRequest
                            .newBuilder()
                            .setEvent(originalRequest.getEvent())
                            .setIsSentByPrimary(true)
                            .build();
                    clientStub.addEvent(request);
                } else if (operationId.startsWith("update_event_")) {
                    UpdateEventRequest originalRequest = (UpdateEventRequest) data;
                    UpdateEventRequest request = UpdateEventRequest
                            .newBuilder()
                            .setEvent(originalRequest.getEvent())
                            .setIsSentByPrimary(true)
                            .build();
                    clientStub.updateEvent(request);
                } else if (operationId.startsWith("cancel_event_")) {
                    CancelEventRequest originalRequest = (CancelEventRequest) data;
                    CancelEventRequest request = CancelEventRequest
                            .newBuilder()
                            .setEventId(originalRequest.getEventId())
                            .setIsSentByPrimary(true)
                            .build();
                    clientStub.cancelEvent(request);
                } else if (operationId.startsWith("add_ticket_stock_")) {
                    AddTicketStockRequest originalRequest = (AddTicketStockRequest) data;
                    AddTicketStockRequest request = AddTicketStockRequest
                            .newBuilder()
                            .setEventId(originalRequest.getEventId())
                            .setTier(originalRequest.getTier())
                            .setCount(originalRequest.getCount())
                            .setAfterParty(originalRequest.getAfterParty())
                            .setPrice(originalRequest.getPrice())
                            .setIsSentByPrimary(true)
                            .build();
                    clientStub.addTicketStock(request);
                } else if (operationId.startsWith("update_ticket_price_")) {
                    UpdateTicketPriceRequest originalRequest = (UpdateTicketPriceRequest) data;
                    UpdateTicketPriceRequest request = UpdateTicketPriceRequest
                            .newBuilder()
                            .setEventId(originalRequest.getEventId())
                            .setTier(originalRequest.getTier())
                            .setPrice(originalRequest.getPrice())
                            .setIsSentByPrimary(true)
                            .build();
                    clientStub.updateTicketPrice(request);
                } else if (operationId.startsWith("reserve_tickets_")) {
                    ReserveTicketsRequest originalRequest = (ReserveTicketsRequest) data;
                    ReserveTicketsRequest request = ReserveTicketsRequest.newBuilder()
                            .setEventId(originalRequest.getEventId())
                            .setTier(originalRequest.getTier())
                            .setAfterParty(originalRequest.getAfterParty())
                            .setIsSentByPrimary(true)
                            .build();
                    clientStub.reserveTickets(request);
                } else if (operationId.startsWith("bulk_reserve_tickets_")) {
                    BulkReserveRequest originalRequest = (BulkReserveRequest) data;
                    BulkReserveRequest request = BulkReserveRequest.newBuilder()
                            .setEventId(originalRequest.getEventId())
                            .setTier(originalRequest.getTier())
                            .setCount(originalRequest.getCount())
                            .setAfterParty(originalRequest.getAfterParty())
                            .setIsSentByPrimary(true)
                            .build();
                    clientStub.bulkReserve(request);
                }
            } catch (Exception e) {
                System.out.println("Error communicating with secondary server " + host + ":" + port + ": " + e.getMessage());
            } finally {
                if (channel != null) {
                    channel.shutdown();
                }
            }
        }
    }

    private EventResponse callPrimary(AddEventRequest request) {
        return callPrimaryServer(request, EventResponse.class);
    }

    private EventResponse callPrimary(UpdateEventRequest request) {
        return callPrimaryServer(request, EventResponse.class);
    }

    private EventResponse callPrimary(CancelEventRequest request) {
        return callPrimaryServer(request, EventResponse.class);
    }

    private EventResponse callPrimary(AddTicketStockRequest request) {
        return callPrimaryServer(request, EventResponse.class);
    }

    private EventResponse callPrimary(UpdateTicketPriceRequest request) {
        return callPrimaryServer(request, EventResponse.class);
    }

    private EventResponse callPrimary(ReserveTicketsRequest request) {
        return callPrimaryServer(request, EventResponse.class);
    }

    private EventResponse callPrimary(BulkReserveRequest request) {
        return callPrimaryServer(request, EventResponse.class);
    }

    private <T> T callPrimaryServer(Object request, Class<T> responseType) {
        System.out.println("Calling Primary Server");
        try {
            String[] currentLeaderData = server.getCurrentLeaderData();


            if (currentLeaderData == null || currentLeaderData.length == 0) {
                System.err.println("No leader data available");
                return getDefaultResponse(responseType);
            }

            String IPAddress = currentLeaderData[0];
            int port = Integer.parseInt(currentLeaderData[1]);  // Use getInt() instead of getString()

            System.out.println("Connecting to leader at " + IPAddress + ":" + port);

            channel = ManagedChannelBuilder.forAddress(IPAddress, port)
                    .usePlaintext()
                    .build();
            clientStub = EventCommandServiceGrpc.newBlockingStub(channel);

            if (request instanceof AddEventRequest) {
                return (T) clientStub.addEvent((AddEventRequest) request);
            } else if (request instanceof UpdateEventRequest) {
                return (T) clientStub.updateEvent((UpdateEventRequest) request);
            } else if (request instanceof CancelEventRequest) {
                return (T) clientStub.cancelEvent((CancelEventRequest) request);
            } else if (request instanceof AddTicketStockRequest) {
                return (T) clientStub.addTicketStock((AddTicketStockRequest) request);
            } else if (request instanceof UpdateTicketPriceRequest) {
                return (T) clientStub.updateTicketPrice((UpdateTicketPriceRequest) request);
            } else if (request instanceof ReserveTicketsRequest) {
                return (T) clientStub.reserveTickets((ReserveTicketsRequest) request);
            } else if (request instanceof BulkReserveRequest) {
                return (T) clientStub.bulkReserve((BulkReserveRequest) request);
            }
        } catch (ArrayIndexOutOfBoundsException e) {
            System.err.println("Leader data format is incorrect: " + e.getMessage());
            return getDefaultResponse(responseType);
        } catch (Exception e) {
            System.err.println("Error communicating with primary server: " + e.getMessage());
        } finally {
            if (channel != null) {
                channel.shutdown();
            }
        }

        // Returning the default response when communication fails.
        return getDefaultResponse(responseType);
    }

    // Helper method to get default response
    private <T> T getDefaultResponse(Class<T> responseType) {
        if (responseType == EventResponse.class) {
            return (T) EventResponse.newBuilder()
                    .setSuccess(false)
                    .setMessage("Failed to communicate with primary server")
                    .build();
        } else {
            return (T) ReservationResponse.newBuilder()
                    .setSuccess(false)
                    .setMessage("Failed to communicate with primary server")
                    .build();
        }
    }


    @Override
    public void addEvent(AddEventRequest request, StreamObserver<EventResponse> responseObserver) {
        System.out.println("[Command] Received add event request: " + request.getEvent().getId());

        if (server.isLeader()) {
            // Act as primary
            try {
                System.out.println("Adding event as primary");
                String operationId = "add_event_" + request.getEvent().getId();
                startDistributesTx(operationId, request);
                updateSecondaryServer(operationId, request);
                server.perform();
                transactionStatus = true;
            } catch (InterruptedException | KeeperException e) {
                System.err.println("Error while adding event: " + e.getMessage());
                if (isTxnStarted) {
                    System.out.println("Initiating Global Abort..");
                    server.sendGlobalAbort();
                }
                transactionStatus = false;
            }
        } else {
            // Act as secondary
            try {
                if (request.getIsSentByPrimary()) {
                    System.out.println("Adding event as secondary, on primary's command");
                    String operation_id = "add_event_" + request.getEvent().getId();
                    startDistributesTx(operation_id, request);
                    server.voteCommit();
                    transactionStatus = true;
                } else {
                    // Forward to primary
                    EventResponse response = callPrimary(request);
                    transactionStatus = response.getSuccess();
                }
            } catch (Exception e) {
                if (isTxnStarted) {
                    System.out.println("Initiating Vote Abort..");
                    server.voteAbort();
                }
                transactionStatus = false;
            }
        }

        EventResponse response = EventResponse
                .newBuilder()
                .setSuccess(transactionStatus)
                .setMessage(transactionStatus ? "Event added successfully" : "Failed to add event")
                .setEvent(request.getEvent())
                .build();
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    @Override
    public void updateEvent(UpdateEventRequest request, StreamObserver<EventResponse> responseObserver) {
        System.out.println("[Command] Received update event request: " + request.getEvent().getId());

        if (server.isLeader()) {
            // Act as Primary
            try {
                System.out.println("Updating event as primary");
                String operation_id = "update_event_" + request.getEvent().getId();
                startDistributesTx(operation_id, request);
                updateSecondaryServer(operation_id, request);
                server.perform();
                transactionStatus = true;
            } catch (KeeperException | InterruptedException e) {
                System.err.println("Error while updating event: " + e.getMessage());
                if (isTxnStarted) {
                    System.out.println("Initiating Global Abort..");
                    server.sendGlobalAbort();
                }
                transactionStatus = false;
            }
        } else {
            // Act as Secondary
            try {
                if (request.getIsSentByPrimary()) {
                    System.out.println("Updating event as secondary, on primary's command");
                    String operation_id = "update_event_" + request.getEvent().getId();
                    startDistributesTx(operation_id, request);
                    server.voteCommit();
                    transactionStatus = true;
                } else {
                    // Forward to primary
                    EventResponse response = callPrimary(request);
                    transactionStatus = response.getSuccess();
                }
            } catch (Exception e) {
                if (isTxnStarted) {
                    System.out.println("Initiating Vote Abort..");
                    server.voteAbort();
                }
                transactionStatus = false;
            }
        }

        EventResponse response = EventResponse
                .newBuilder()
                .setSuccess(transactionStatus)
                .setMessage(transactionStatus ? "Event updated successfully" : "Failed to update event")
                .setEvent(request.getEvent())
                .build();
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    @Override
    public void cancelEvent(CancelEventRequest request, StreamObserver<EventResponse> responseObserver) {
        System.out.println("[Command] Received update event request: " + request.getEventId());

        if (server.isLeader()) {
            // Act as Primary
            try {
                System.out.println("Cancelling event as primary");
                String operation_id = "cancel_event_" + request.getEventId();
                startDistributesTx(operation_id, request);
                updateSecondaryServer(operation_id, request);
                server.perform();
                transactionStatus = true;
            } catch (KeeperException | InterruptedException e) {
                System.err.println("Error while cancelling event: " + e.getMessage());
                if (isTxnStarted) {
                    System.out.println("Initiating Global Abort..");
                    server.sendGlobalAbort();
                }
                transactionStatus = false;
            }
        } else {
            // Act as Secondary
            try {
                if (request.getIsSentByPrimary()) {
                    System.out.println("Cancelling event as secondary, on primary's command");
                    String operation_id = "cancel_event_" + request.getEventId();
                    startDistributesTx(operation_id, request);
                    server.voteCommit();
                    transactionStatus = true;
                } else {
                    // Forward to primary
                    EventResponse response = callPrimary(request);
                    transactionStatus = response.getSuccess();
                }
            } catch (Exception e) {
                if (isTxnStarted) {
                    System.out.println("Initiating Vote Abort..");
                    server.voteAbort();
                }
                transactionStatus = false;
            }
        }

        EventResponse response = EventResponse
                .newBuilder()
                .setSuccess(transactionStatus)
                .setMessage(transactionStatus ? "Event cancelled successfully" : "Failed to cancel event")
                .build();
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    @Override
    public void addTicketStock(AddTicketStockRequest request, StreamObserver<EventResponse> responseObserver) {
        String eventId = request.getEventId();
        System.out.println("[Command] Received add ticket stock request: " + eventId);
        if (server.isLeader()) {
            // Act as Primary
            try {
                System.out.println("Adding ticket stock as primary");
                String operation_id = "add_ticket_stock_" + request.getEventId();
                startDistributesTx(operation_id, request);
                updateSecondaryServer(operation_id, request);
                server.perform();
                transactionStatus = true;
            } catch (InterruptedException | KeeperException e) {
                System.err.println("Error while adding ticket stock: " + e.getMessage());
                if (isTxnStarted) {
                    System.out.println("Initiating Global Abort..");
                    server.sendGlobalAbort();
                }
                transactionStatus = false;
            }
        } else {
            // Act as Secondary
            try {
                if (request.getIsSentByPrimary()) {
                    System.out.println("Adding ticket stock as secondary, on primary's command");
                    String operation_id = "add_ticket_stock_" + request.getEventId();
                    startDistributesTx(operation_id, request);
                    server.voteCommit();
                    transactionStatus = true;
                } else {
                    // Forward to primary
                    EventResponse response = callPrimary(request);
                    transactionStatus = response.getSuccess();
                }
            } catch (Exception e) {
                if (isTxnStarted) {
                    System.out.println("Initiating Vote Abort..");
                    server.voteAbort();
                }
                transactionStatus = false;
            }
        }

        EventResponse response = EventResponse
                .newBuilder()
                .setSuccess(transactionStatus)
                .setMessage(transactionStatus ? "Tickets added successfully" : "Failed to add ticket stock")
                .build();
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    @Override
    public void updateTicketPrice(UpdateTicketPriceRequest request, StreamObserver<EventResponse> responseObserver) {
        System.out.println("[Command] Received update ticket price request: " + request.getEventId());

        if (server.isLeader()) {
            // Act as primary
            try {
                System.out.println("Updating ticket price as primary");
                String operation_id = "update_ticket_price_" + request.getEventId();
                startDistributesTx(operation_id, request);
                updateSecondaryServer(operation_id, request);
                server.perform();
                transactionStatus = true;
            } catch (Exception e) {
                System.err.println("Error while updating ticket price: " + e.getMessage());
                if (isTxnStarted) {
                    System.out.println("Initiating Global Abort..");
                    server.sendGlobalAbort();
                }
                transactionStatus = false;
            }
        } else {
            // Act as secondary
            try {
                if (request.getIsSentByPrimary()) {
                    System.out.println("Updating ticket price as secondary, on primary's command");
                    String operation_id = "update_ticket_price_" + request.getEventId();
                    startDistributesTx(operation_id, request);
                    server.voteCommit();
                    transactionStatus = true;
                } else {
                    // Forward to primary
                    EventResponse response = callPrimary(request);
                    transactionStatus = response.getSuccess();
                }
            } catch (Exception e) {
                if (isTxnStarted) {
                    System.out.println("Initiating Vote Abort..");
                    server.voteAbort();
                }
                transactionStatus = false;
            }
        }

        EventResponse response = EventResponse
                .newBuilder()
                .setSuccess(transactionStatus)
                .setMessage(transactionStatus ? "Tickets updated successfully" : "Failed to update ticket price")
                .build();
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    @Override
    public void reserveTickets(ReserveTicketsRequest request, StreamObserver<ReservationResponse> responseObserver) {
        String eventId = request.getEventId();
        System.out.println("[Command] Received reserve ticket request: " + eventId);
        if (server.isLeader()) {
            // Act as primary
            try {
                System.out.println("Reserving tickets as primary");
                String operation_id = "reserve_tickets_" + request.getEventId();
                startDistributesTx(operation_id, request);
                updateSecondaryServer(operation_id, request);
                server.perform();
                transactionStatus = true;
            } catch (Exception e) {
                System.err.println("Error while reserving tickets: " + e.getMessage());
                if (isTxnStarted) {
                    System.out.println("Initiating Global Abort..");
                    server.sendGlobalAbort();
                }
                transactionStatus = false;
            }
        } else {
            // Act as secondary
            try {
                if (request.getIsSentByPrimary()) {
                    System.out.println("Reserving tickets as secondary, on primary's command");
                    String operation_id = "reserve_tickets_" + request.getEventId();
                    startDistributesTx(operation_id, request);
                    server.voteCommit();
                    transactionStatus = true;
                } else {
                    // Forward to primary
                    EventResponse response = callPrimary(request);
                    transactionStatus = response.getSuccess();
                }
            } catch (Exception e) {
                if (isTxnStarted) {
                    System.out.println("Initiating Vote Abort..");
                    server.voteAbort();
                }
                transactionStatus = false;
            }
        }

        ReservationResponse response = ReservationResponse
                .newBuilder()
                .setSuccess(transactionStatus)
                .setMessage(transactionStatus ? "Tickets reserved successfully" : "Failed to reserve tickets")
                .build();
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    @Override
    public void bulkReserve(BulkReserveRequest request, StreamObserver<ReservationResponse> responseObserver) {
        String eventId = request.getEventId();
        System.out.println("[Command] Received reserve ticket request: " + eventId);

        if (server.isLeader()) {
            // Act as primary
            try {
                System.out.println("Bulk reserving tickets as primary");
                String operation_id = "bulk_reserve_tickets_" + request.getEventId();
                startDistributesTx(operation_id, request);
                updateSecondaryServer(operation_id, request);
                server.perform();
                transactionStatus = true;
            } catch (Exception e) {
                System.err.println("Error while bulk reserving tickets: " + e.getMessage());
                if (isTxnStarted) {
                    System.out.println("Initiating Global Abort..");
                    server.sendGlobalAbort();
                }
                transactionStatus = false;
            }
        } else {
            // Act as secondary
            try {
                if (request.getIsSentByPrimary()) {
                    System.out.println("Bulk reserving tickets as secondary, on primary's command");
                    String operation_id = "bulk_reserve_tickets_" + request.getEventId();
                    startDistributesTx(operation_id, request);
                    server.voteCommit();
                    transactionStatus = true;
                } else {
                    // Forward to primary
                    EventResponse response = callPrimary(request);
                    transactionStatus = response.getSuccess();
                }
            } catch (Exception e) {
                if (isTxnStarted) {
                    System.out.println("Initiating Global Abort..");
                    server.sendGlobalAbort();
                }
                transactionStatus = false;
            }
        }

        ReservationResponse response = ReservationResponse
                .newBuilder()
                .setSuccess(transactionStatus)
                .setMessage(transactionStatus ? "Tickets reserved successfully" : "Failed to reserve tickets")
                .build();
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }


    @Override
    public void setTxnStarted(boolean txnStarted) {
        this.isTxnStarted = txnStarted;
    }

    @Override
    public void onGlobalCommit() {
        System.out.println("On global commit...");
        if (tempDataHolder != null) {
            String operationId = tempDataHolder.getKey();
            Object data = tempDataHolder.getValue();
            System.out.println("On global commit: " + operationId);

            // Handle the commited data based on the operation type
            if (operationId.startsWith("add_event_")) {
                AddEventRequest eventRequest = (AddEventRequest) data;
                addEvent(eventRequest);
            } else if (operationId.startsWith("update_event_")) {
                UpdateEventRequest eventRequest = (UpdateEventRequest) data;
                updateEvent(eventRequest);
            } else if (operationId.startsWith("cancel_event_")) {
                CancelEventRequest eventRequest = (CancelEventRequest) data;
                cancelEvent(eventRequest);
            } else if (operationId.startsWith("add_ticket_stock_")) {
                AddTicketStockRequest ticketRequest = (AddTicketStockRequest) data;
                addTicketStocks(ticketRequest);
            } else if (operationId.startsWith("update_ticket_price_")) {
                UpdateTicketPriceRequest ticketRequest = (UpdateTicketPriceRequest) data;
                updateTicketPrice(ticketRequest);
            } else if (operationId.startsWith("reserve_tickets_")) {
                ReserveTicketsRequest ticketRequest = (ReserveTicketsRequest) data;
                reserveTickets(ticketRequest);
            } else if (operationId.startsWith("bulk_reserve_tickets_")) {
                BulkReserveRequest bulkRequest = (BulkReserveRequest) data;
                bulkReserve(bulkRequest);
            }

            tempDataHolder = null;
        }
    }

    @Override
    public void onGlobalAbort() {
        tempDataHolder = null;
        System.out.println("Transaction aborted by the coordinator");
    }

    private void addEvent(AddEventRequest request) {
        synchronized (TicketReservationServer.class) {
            System.out.println("Events Added.");
            server.addEvent(request.getEvent());
        }
    }

    private void updateEvent(UpdateEventRequest request) {
        synchronized (TicketReservationServer.class) {
            System.out.println("Events Map updated.");
            server.updateEvent(request.getEvent());
        }
    }

    private void cancelEvent(CancelEventRequest request) {
        synchronized (TicketReservationServer.class) {
            server.cancelEvent(request.getEventId());
        }
    }

    private void addTicketStocks(AddTicketStockRequest request) {
        synchronized (TicketReservationServer.class) {
            String eventId = request.getEventId();
            Event currentEvent = server.getEvent(eventId);

            if (currentEvent != null) {
                // Number of tickets to add
                int ticketsToAdd = request.getCount();
                System.out.println("Adding " + ticketsToAdd + " tickets");

                if (request.getAfterParty()) {
                    // Adding after party tickets

                    // Number of ticket slots available
                    int availableTicketSlots = currentEvent.getAfterPartyTicketsTotal() - currentEvent.getAfterPartyTicketsSold() - currentEvent.getAfterPartyTicketsAvailable();
                    System.out.println("Available Ticket Slots: " + availableTicketSlots);


                    if (ticketsToAdd <= availableTicketSlots) {
                        Event newEvent = Event
                                .newBuilder()
                                .mergeFrom(currentEvent)
                                .setAfterPartyTicketsAvailable(currentEvent.getAfterPartyTicketsAvailable() + ticketsToAdd)
                                .build();
                        server.updateEvent(newEvent);
                    } else {
                        System.err.println("Tickets not enough to be added");
                    }
                } else if (currentEvent.getTicketTiersMap().containsKey(request.getTier())) {
                    // Adding tickets to a ticket tier
                    TicketTier tier = currentEvent.getTicketTiersMap().get(request.getTier());

                    // Number of ticket slots available
                    int availableTicketSlots = tier.getTicketsTotal() - tier.getTicketsSold() - tier.getTicketsAvailable();

                    if (ticketsToAdd <= availableTicketSlots) {
                        TicketTier newTier = TicketTier
                                .newBuilder()
                                .mergeFrom(tier)
                                .setTicketsAvailable(tier.getTicketsAvailable() + ticketsToAdd)
                                .build();

                        Event newEvent = Event
                                .newBuilder()
                                .mergeFrom(currentEvent)
                                .putTicketTiers(newTier.getId(), newTier)
                                .setEventTicketsAvailable(currentEvent.getEventTicketsAvailable() + ticketsToAdd)
                                .build();

                        server.updateEvent(newEvent);
                    } else {
                        System.err.println("Tickets not enough to be added");
                    }
                }
            }
        }
    }

    private void updateTicketPrice(UpdateTicketPriceRequest request) {
        synchronized (TicketReservationServer.class) {
            String eventId = request.getEventId();
            Event currentEvent = server.getEvent(eventId);
            if (currentEvent != null) {
                Map<String, TicketTier> currentEventTicketTiersMap = currentEvent.getTicketTiersMap();
                TicketTier tier = currentEventTicketTiersMap.get(request.getTier());
                if (tier != null) {
                    TicketTier newTier = TicketTier
                            .newBuilder()
                            .mergeFrom(tier)
                            .setPrice(request.getPrice())
                            .build();

                    Event newEvent = Event
                            .newBuilder()
                            .mergeFrom(currentEvent)
                            .putTicketTiers(tier.getId(), newTier)
                            .build();

                    server.updateEvent(newEvent);
                }
            }
        }
    }

    private void reserveTickets(ReserveTicketsRequest request) {
        synchronized (TicketReservationServer.class) {
            String eventId = request.getEventId();
            Event currentEvent = server.getEvent(eventId);
            if (currentEvent != null) {
                Map<String, TicketTier> currentEventTicketTiersMap = currentEvent.getTicketTiersMap();
                TicketTier currentTier = currentEventTicketTiersMap.get(request.getTier());
                if (currentTier != null) {
                    if (currentTier.getTicketsAvailable() > 0) {
                        Event.Builder newEventBuilder = Event
                                .newBuilder()
                                .mergeFrom(currentEvent);
                        TicketTier.Builder newTierBuilder = TicketTier
                                .newBuilder()
                                .mergeFrom(currentTier);
                        if (request.getAfterParty()) {
                            if (currentEvent.getAfterPartyTicketsAvailable() > 0) {
                                newTierBuilder
                                        .setTicketsAvailable(currentTier.getTicketsAvailable() - 1)
                                        .setTicketsSold(currentTier.getTicketsSold() + 1);

                                newEventBuilder
                                        .setEventTicketsAvailable(currentEvent.getEventTicketsAvailable() - 1)
                                        .setAfterPartyTicketsAvailable(currentEvent.getAfterPartyTicketsAvailable() - 1)
                                        .setEventTicketsSold(currentEvent.getEventTicketsSold() + 1)
                                        .setAfterPartyTicketsSold(currentEvent.getAfterPartyTicketsSold() + 1)
                                        .putTicketTiers(currentTier.getId(), newTierBuilder.build());
                            }
                        } else {
                            newTierBuilder
                                    .setTicketsAvailable(currentTier.getTicketsAvailable() - 1)
                                    .setTicketsSold(currentTier.getTicketsSold() + 1);

                            newEventBuilder
                                    .setEventTicketsAvailable(currentEvent.getEventTicketsAvailable() - 1)
                                    .setEventTicketsSold(currentEvent.getEventTicketsSold() - 1)
                                    .putTicketTiers(currentTier.getId(), newTierBuilder.build());
                        }
                        server.updateEvent(newEventBuilder.build());
                    }
                }
            }
        }
    }

    private void bulkReserve(BulkReserveRequest request) {
        synchronized (TicketReservationServer.class) {
            String eventId = request.getEventId();
            Event currentEvent = server.getEvent(eventId);
            if (currentEvent != null) {
                Map<String, TicketTier> currentEventTicketTiersMap = currentEvent.getTicketTiersMap();
                TicketTier currentTier = currentEventTicketTiersMap.get(request.getTier());
                if (currentTier != null) {
                    int ticketsToBuy = request.getCount();
                    if (currentTier.getTicketsAvailable() >= ticketsToBuy) {
                        Event.Builder newEventBuilder = Event
                                .newBuilder()
                                .mergeFrom(currentEvent);
                        TicketTier.Builder newTierBuilder = TicketTier
                                .newBuilder()
                                .mergeFrom(currentTier);
                        if (request.getAfterParty()) {
                            if (currentEvent.getAfterPartyTicketsAvailable() > ticketsToBuy) {
                                newTierBuilder
                                        .setTicketsAvailable(currentTier.getTicketsAvailable() - ticketsToBuy)
                                        .setTicketsSold(currentTier.getTicketsSold() + ticketsToBuy);

                                newEventBuilder
                                        .setEventTicketsAvailable(currentEvent.getEventTicketsAvailable() - ticketsToBuy)
                                        .setAfterPartyTicketsAvailable(currentEvent.getAfterPartyTicketsAvailable() - ticketsToBuy)
                                        .setEventTicketsSold(currentEvent.getEventTicketsSold() + ticketsToBuy)
                                        .setAfterPartyTicketsSold(currentEvent.getAfterPartyTicketsSold() + ticketsToBuy)
                                        .putTicketTiers(currentTier.getId(), newTierBuilder.build());
                            }
                        } else {
                            newTierBuilder
                                    .setTicketsAvailable(currentTier.getTicketsAvailable() - ticketsToBuy)
                                    .setTicketsSold(currentTier.getTicketsSold() + ticketsToBuy);

                            newEventBuilder
                                    .setEventTicketsAvailable(currentEvent.getEventTicketsAvailable() - ticketsToBuy)
                                    .setEventTicketsSold(currentEvent.getEventTicketsSold() - ticketsToBuy)
                                    .putTicketTiers(currentTier.getId(), newTierBuilder.build());
                        }
                        server.updateEvent(newEventBuilder.build());
                    }
                }
            }
        }
    }
}
