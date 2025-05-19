package lk.ac.iit.ds.charindu.client;

import lk.ac.iit.ds.charindu.grpc.generated.*;

import java.io.IOException;
import java.util.*;
import java.util.function.Function;

import static lk.ac.iit.ds.charindu.client.util.CLIShapes.*;

public class BoxOfficeClerksClient extends Client {

    private Scanner scanner = new Scanner(System.in);

    public BoxOfficeClerksClient() throws IOException, InterruptedException {
        super();
    }

    @Override
    protected void processUserRequests() {
        promptBox("Welcome to the Box Office Coordinator Client");
        boolean running = true;
        while (running) {
            try {
                System.out.println();
                System.out.println("Select Command: ");
                numberMenu(new String[]{
                        "Get Events",
                        "Get Ticket Tiers",
                        "Add Tickets",
                        "Add After Party Tickets",
                        "Update Ticket Tier Price",
                        "Quit"
                });
                int command = scanner.nextInt();
                scanner.nextLine(); // Consume the next line.

                switch (command) {
                    case 1:
                        getEvents();
                        break;
                    case 2:
                        getTicketTiers();
                        break;
                    case 3:
                        addTickets();
                        break;
                    case 4:
                        addAfterPartTickets();
                        break;
                    case 5:
                        updateTicketTierPrice();
                        break;
                    case 6:
                        System.out.println("Quitting...");
                        running = false;
                        break;
                    default:
                        System.out.println("Invalid command");
                        break;
                }
            } catch (IOException | InterruptedException e) {
                System.err.println("Error: " + e.getMessage());
            } catch (InputMismatchException e) {
                scanner.nextLine();
                System.out.println("Invalid Input.");
            }
        }
    }

    //    @Override
//    protected void initializeConnection() {
//        super.initializeConnection();
//        getEventServiceBlockingStub = GetEventServiceGrpc.newBlockingStub(channel);
//        getTicketTierServiceBlockingStub = GetTicketTierServiceGrpc.newBlockingStub(channel);
//        ticketServiceBlockingStub = TicketServiceGrpc.newBlockingStub(channel);
//        ticketTierServiceBlockingStub = TicketTierServiceGrpc.newBlockingStub(channel);
//        channel.getState(true);
//    }
//
//    @Override
//    protected void processUserRequests() {
//        Scanner scanner = new Scanner(System.in);
//        boolean running = true;
//        while (running) {
//            try {
//                promptBox("Welcome to the Box Office Coordinator Client");
//                numberMenu(new String[]{
//                        "Get Events", "Get Ticket Tiers",
//                        "Add Tickets", "Add After Party Tickets",
//                        "Update Ticket Tier Price",
//                        "Quit"
//                });
//                int mainMenuChoice = scanner.nextInt();
//                scanner.nextLine();
//                switch (mainMenuChoice) {
//                    case 1:
//                        promptBox("Get Events");
//
//                        GetEventsRequest getEventsRequest = GetEventsRequest
//                                .newBuilder()
//                                .build();
//                        waitForConnection();
//
//                        GetEventsResponse getEventsResponse = getEventServiceBlockingStub.getEvents(getEventsRequest);
//                        List<Event> eventsList = getEventsResponse.getEventsList();
//
//                        LinkedHashMap<String, Function<Event, ?>> columnsEvent = new LinkedHashMap<>();
//                        columnsEvent.put("Event Id", Event::getEventId);
//                        columnsEvent.put("Event Name", Event::getEventId);
//                        columnsEvent.put("Total Tickets", Event::getTotalTickets);
//                        columnsEvent.put("Tickets Sold", Event::getTicketsSold);
//                        columnsEvent.put("Tickets Available", Event::getTicketsAvailableToPurchase);
//                        columnsEvent.put("Total After Party Tickets", Event::getTotalAfterPartyTickets);
//                        columnsEvent.put("After Party Tickets Sold", Event::getTotalAfterPartyTicketsSold);
//                        columnsEvent.put("After Party Tickets Available", Event::getTotalAfterPartTicketsAvailableToPurchase);
//
//                        printTable(eventsList, columnsEvent);
//                        break;
//                    case 2:
//                        promptBox("Get Ticket Tiers");
//
//                        GetTicketTiersRequest getTicketTiersRequest = GetTicketTiersRequest
//                                .newBuilder()
//                                .build();
//                        waitForConnection();
//
//                        GetTicketTiersResponse getTicketTiersResponse = getTicketTierServiceBlockingStub.getTicketTiers(getTicketTiersRequest);
//                        List<TicketTier> ticketTierList = getTicketTiersResponse.getTicketTierList();
//
//                        // filter for only those with ticketsAvailableToPurchase > 0
//                        List<TicketTier> availableTiers = ticketTierList.stream()
//                                .filter(tier -> tier.getTicketsAvailableToPurchase() > 0)
//                                .collect(Collectors.toList());
//
//                        LinkedHashMap<String, Function<TicketTier, ?>> columns = new LinkedHashMap<>();
//                        columns.put("Ticket Tier Id", TicketTier::getTicketTierId);
//                        columns.put("Ticket Tier Name", TicketTier::getTicketTierName);
//                        columns.put("Ticket Price", TicketTier::getTicketTierPrice);
//                        columns.put("Total Tickets ", TicketTier::getTotalTickets);
//                        columns.put("Tickets Sold", TicketTier::getTicketsSold);
//                        columns.put("Tickets Available", TicketTier::getTicketsAvailableToPurchase);
//
//                        printTable(availableTiers, columns);
//                        break;
//                    case 3:
//                        promptBox("Add Tickets");
//
//                        System.out.println("Enter Event Id: ");
//                        int eventId = scanner.nextInt();
//                        scanner.nextLine();
//
//                        System.out.println("Enter Ticket Tier Id: ");
//                        int ticketTierId = scanner.nextInt();
//                        scanner.nextLine();
//
//                        System.out.println("Enter No of Tickets to Add: ");
//                        int ticketsToAdd = scanner.nextInt();
//                        scanner.nextLine();
//
//                        AddTicketsRequest addTicketsRequest = AddTicketsRequest
//                                .newBuilder()
//                                .setEventId(eventId)
//                                .setTicketTierId(ticketTierId)
//                                .setNoOfTicketsToAdd(ticketsToAdd)
//                                .build();
//                        waitForConnection();
//
//                        AddTicketsResponse addTicketsResponse = ticketServiceBlockingStub.addTickets(addTicketsRequest);
//                        if (addTicketsResponse.getSuccess()) {
//                            System.out.println("Tickets Added");
//                        } else {
//                            System.out.println("Tickets Not Added");
//                            System.out.println(addTicketsResponse.getMessage());
//                        }
//                        break;
//                    case 4:
//                        promptBox("Add After Party Tickets");
//
//                        System.out.println("Enter Event Id: ");
//                        int eventId1 = scanner.nextInt();
//                        scanner.nextLine();
//
//                        System.out.println("Enter  No of Tickets to Add: ");
//                        int ticketsToAdd1 = scanner.nextInt();
//                        scanner.nextLine();
//
//                        AddAfterPartyTicketsRequest addAfterPartyTicketsRequest = AddAfterPartyTicketsRequest
//                                .newBuilder()
//                                .setEventId(eventId1)
//                                .setNoOfTicketsToAdd(ticketsToAdd1)
//                                .build();
//                        waitForConnection();
//
//                        AddAfterPartyTicketsResponse addAfterPartyTicketsResponse = ticketServiceBlockingStub.addAfterPartyTickets(addAfterPartyTicketsRequest);
//                        if (addAfterPartyTicketsResponse.getSuccess()) {
//                            System.out.println("Tickets Added");
//                        } else {
//                            System.out.println("Tickets Not Added");
//                            System.out.println(addAfterPartyTicketsResponse.getMessage());
//                        }
//                        break;
//                    case 5:
//                        promptBox("Update Ticket Tier Price");
//
//                        System.out.println("Enter Event Id: ");
//                        int eventIdUpdate = scanner.nextInt();
//                        scanner.nextLine();
//
//                        System.out.println("Enter Ticket Tier Id: ");
//                        int ticketTierIdUpdate = scanner.nextInt();
//                        scanner.nextLine();
//
//                        GetTicketTierRequest getTicketTierRequest = GetTicketTierRequest
//                                .newBuilder()
//                                .setEventId(eventIdUpdate)
//                                .setTicketTierId(ticketTierIdUpdate)
//                                .build();
//                        waitForConnection();
//
//                        GetTicketTierResponse getTicketTierResponse = getTicketTierServiceBlockingStub.getTicketTier(getTicketTierRequest);
//                        TicketTier currentTicketTier = getTicketTierResponse.getTicketTier();
//
//                        System.out.println("Enter New Ticket Tier Price: ");
//                        int newTicketTierPrice = scanner.nextInt();
//                        scanner.nextLine();
//
//                        UpdateTicketTierRequest updateTicketTierRequest = UpdateTicketTierRequest
//                                .newBuilder()
//                                .setEventId(eventIdUpdate)
//                                .setTicketTierId(ticketTierIdUpdate)
//                                .setTicketTierPrice(newTicketTierPrice)
//                                .setTicketTierName(currentTicketTier.getTicketTierName())
//                                .setTotalTickets(currentTicketTier.getTotalTickets())
//                                .build();
//                        waitForConnection();
//
//                        UpdateTicketTierResponse updateTicketTierResponse = ticketTierServiceBlockingStub.updateTicketTier(updateTicketTierRequest);
//                        if (updateTicketTierResponse.getSuccess()) {
//                            System.out.println("Ticket Tier Updated");
//                        } else {
//                            System.out.println("Ticket Tier Not Updated");
//                            System.out.println(updateTicketTierResponse.getMessage());
//                        }
//                        break;
//                    case 6:
//                        promptBox("Quit");
//                        running = false;
//                        break;
//                    default:
//                        alertBox("Invalid choice", '!');
//                        break;
//                }
//            } catch (RuntimeException | IOException | InterruptedException e) {
//                System.out.println("Error: " + e.getMessage());
//            }
//        }
//    }


    private void getEvents() throws IOException, InterruptedException {
        promptBox("Get Events");

        GetEventsRequest getEventsRequest = GetEventsRequest
                .newBuilder()
                .build();
        waitForConnection();

        GetEventsResponse getEventsResponse = eventQueryServiceBlockingStub.getEvents(getEventsRequest);
        List<Event> eventsList = getEventsResponse.getEventsList();

        if (eventsList.isEmpty()) {
            System.err.println("No events found.");
            return;
        }

        LinkedHashMap<String, Function<Event, ?>> columnsEvents = new LinkedHashMap<>();
        columnsEvents.put("Id", Event::getId);
        columnsEvents.put("Name", Event::getName);
        columnsEvents.put("Date", Event::getDate);
        columnsEvents.put("Total Tickets", Event::getEventTicketsTotal);
        columnsEvents.put("Tickets Sold", Event::getEventTicketsSold);
        columnsEvents.put("Tickets Available", Event::getEventTicketsAvailable);
        columnsEvents.put("Total After Party Tickets", Event::getAfterPartyTicketsTotal);
        columnsEvents.put("After Party Tickets Sold", Event::getAfterPartyTicketsSold);
        columnsEvents.put("After Party Tickets Available", Event::getAfterPartyTicketsAvailable);

        printTable(eventsList, columnsEvents);
    }

    private Event getEvent(String eventId) throws IOException, InterruptedException {
        GetEventRequest getEventRequest = GetEventRequest
                .newBuilder()
                .setEventId(eventId)
                .build();
        waitForConnection();
        GetEventResponse getEventResponse = eventQueryServiceBlockingStub.getEvent(getEventRequest);
        return getEventResponse.getEvent();
    }

    private void getTicketTiers() throws IOException, InterruptedException {
        promptBox("Get Ticket Tiers of an Event");

        System.out.println("Enter Event Id: ");
        String eventId = scanner.nextLine();

        Event currentEvent = getEvent(eventId);

        if (!currentEvent.getId().equals(eventId)) {
            alertBox("Event not found", '!');
            return;
        }

        Map<String, TicketTier> ticketTiersMap = currentEvent.getTicketTiersMap();
        ArrayList<TicketTier> ticketTiersList = new ArrayList<>(ticketTiersMap.values());

        if (ticketTiersList.isEmpty()) {
            System.err.println("There are no Ticket Tiers in this Event");
            return;
        }

        LinkedHashMap<String, Function<TicketTier, ?>> columnsTicketTier = new LinkedHashMap<>();
        columnsTicketTier.put("Id", TicketTier::getId);
        columnsTicketTier.put("Price", TicketTier::getPrice);
        columnsTicketTier.put("Tickets Total", TicketTier::getTicketsTotal);
        columnsTicketTier.put("Tickets Sold", TicketTier::getTicketsSold);
        columnsTicketTier.put("Tickets Available", TicketTier::getTicketsAvailable);

        printTable(ticketTiersList, columnsTicketTier);
    }

    private void addTickets() throws IOException, InterruptedException {
        promptBox("Add Tickets");

        System.out.println("Enter Event Id: ");
        String eventId = scanner.nextLine();

        Event currentEvent = getEvent(eventId);

        if (!currentEvent.getId().equals(eventId)) {
            alertBox("Event not found", '!');
            return;
        }

        Map<String, TicketTier> ticketTiersMap = currentEvent.getTicketTiersMap();
        ArrayList<String> ticketTiersIdList = new ArrayList<>(ticketTiersMap.keySet());

        if (ticketTiersIdList.isEmpty()) {
            System.err.println("There are no Ticket Tiers in this Event");
            return;
        }

        String[] ticketTiersIdArray = ticketTiersIdList.toArray(new String[0]);

        System.out.println("Select Ticket Tier to Add Tickets:");
        numberMenu(ticketTiersIdArray);
        int ticketTierSelection = scanner.nextInt();
        scanner.nextLine();

        String ticketTierId = ticketTiersIdArray[ticketTierSelection-1];
        TicketTier ticketTier = ticketTiersMap.get(ticketTierId);
        int ticketsRemaining = ticketTier.getTicketsTotal() - ticketTier.getTicketsSold() - ticketTier.getTicketsAvailable();

        System.out.println("Enter amount of tickets to add[" + ticketsRemaining + "]:");
        int amount = scanner.nextInt();
        scanner.nextLine();

        if (amount > ticketsRemaining) {
            System.err.println("Ticket amount is greater than tickets available");
            return;
        }

        AddTicketStockRequest request = AddTicketStockRequest
                .newBuilder()
                .setEventId(eventId)
                .setTier(ticketTierId)
                .setAfterParty(false)
                .setCount(amount)
                .build();
        waitForConnection();

        EventResponse addTicketStockRespond = eventCommandServiceBlockingStub.addTicketStock(request);
        if (addTicketStockRespond.getSuccess()) {
            System.out.println("Added Ticket Stock");
        } else {
            System.err.println("Failed to add Ticket Stock");
            System.err.println(addTicketStockRespond.getMessage());
        }
    }

    private void addAfterPartTickets() throws IOException, InterruptedException {
        promptBox("Add After Party Tickets");

        System.out.println("Enter Event Id: ");
        String eventId = scanner.nextLine();

        Event currentEvent = getEvent(eventId);

        if (!currentEvent.getId().equals(eventId)) {
            alertBox("Event not found", '!');
            return;
        }

        int ticketsRemaining = currentEvent.getAfterPartyTicketsTotal() - currentEvent.getAfterPartyTicketsSold() - currentEvent.getAfterPartyTicketsAvailable();

        System.out.println("Enter amount of tickets to add[" + ticketsRemaining + "]:");
        int amount = scanner.nextInt();
        scanner.nextLine();


        if (amount > ticketsRemaining) {
            System.err.println("Ticket amount is greater than current ticket amount available");
            return;
        }

        AddTicketStockRequest request = AddTicketStockRequest
                .newBuilder()
                .setEventId(eventId)
                .setAfterParty(true)
                .setCount(amount)
                .build();
        waitForConnection();

        EventResponse addTicketStockRespond = eventCommandServiceBlockingStub.addTicketStock(request);
        if (addTicketStockRespond.getSuccess()) {
            System.out.println("Added Ticket Stock");
        } else {
            System.err.println("Failed to add Ticket Stock");
            System.err.println(addTicketStockRespond.getMessage());
        }
    }

    private void updateTicketTierPrice() throws IOException, InterruptedException {
        promptBox("Update Ticket Tier Price");

        System.out.println("Enter Event Id: ");
        String eventId = scanner.nextLine();

        Event currentEvent = getEvent(eventId);

        if (!currentEvent.getId().equals(eventId)) {
            alertBox("Event not found", '!');
            return;
        }

        Map<String, TicketTier> ticketTiersMap = currentEvent.getTicketTiersMap();
        ArrayList<String> ticketTiersIdList = new ArrayList<>(ticketTiersMap.keySet());

        String[] ticketTiersIdArray = ticketTiersIdList.toArray(new String[0]);

        System.out.println("Select Ticket Tier to Update");
        numberMenu(ticketTiersIdArray);
        int ticketTierSelection = scanner.nextInt();
        scanner.nextLine();

        TicketTier currentTicketTier = ticketTiersMap.get(ticketTiersIdArray[ticketTierSelection-1]);

        TicketTier.Builder newTicketTierBuilder = TicketTier
                .newBuilder()
                .mergeFrom(currentTicketTier);


        System.out.println("Enter New Ticket Tier Price: ");
        int newTicketTierPrice = scanner.nextInt();
        scanner.nextLine();
        newTicketTierBuilder.setPrice(newTicketTierPrice);

        TicketTier newTicketTier = newTicketTierBuilder.build();

        Event newEvent = Event
                .newBuilder()
                .mergeFrom(currentEvent)
                .putTicketTiers(newTicketTier.getId(), newTicketTier)
                .build();

        UpdateEventRequest updateEventRequest = UpdateEventRequest
                .newBuilder()
                .setEvent(newEvent)
                .build();
        waitForConnection();

        EventResponse updateEventResponse = eventCommandServiceBlockingStub.updateEvent(updateEventRequest);
        if (updateEventResponse.getSuccess()) {
            System.out.println("Ticket Tier Updated Successfully");
        } else {
            System.out.println("Ticket Tier Not Updated");
            System.out.println(updateEventResponse.getMessage());
        }
    }
}
