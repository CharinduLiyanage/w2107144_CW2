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
        System.out.println("=== Welcome to Box Office Clerk Client ===");
        boolean running = true;
        while (running) {
            try {
                System.out.println("\nAvailable Commands:");
                numberMenu(new String[]{
                        "Get Events",
                        "Get Ticket Tiers",
                        "Add Tickets",
                        "Add After Party Tickets",
                        "Update Ticket Tier Price",
                        "Quit"
                });

                int command = getValidSelection("Enter command number:", 1, 6);

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
                        running = !askYesNo("Are you sure you want to quit? (Y/N)", scanner);
                        break;
                }
            } catch (IOException | InterruptedException e) {
                System.out.println("Error: " + e.getMessage());
            }
        }
    }

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

    private void getTicketTiers() throws IOException, InterruptedException {
        System.out.println("\n=== Get Ticket Tiers ===");

        // Get and validate event ID
        String eventId = getNonEmptyInput("Enter Event ID:");

        // Fetch and verify event
        Event event = getAndVerifyEvent(eventId);
        if (event == null) {
            System.out.println("Event not found!");
            return;
        }

        // Get ticket tiers
        Map<String, TicketTier> ticketTiers = event.getTicketTiersMap();
        if (ticketTiers.isEmpty()) {
            System.out.println("No ticket tiers available for this event.");
            return;
        }

        // Prepare data for table
        List<TicketTier> tierList = new ArrayList<>(ticketTiers.values());

        // Define table columns
        LinkedHashMap<String, Function<TicketTier, ?>> columns = new LinkedHashMap<>();
        columns.put("Tier ID", TicketTier::getId);
        columns.put("Price", TicketTier::getPrice);
        columns.put("Total Tickets", TicketTier::getTicketsTotal);
        columns.put("Tickets Sold", TicketTier::getTicketsSold);
        columns.put("Available", TicketTier::getTicketsAvailable);

        // Print table
        System.out.println("\nTicket Tiers for Event: " + event.getName());
        printTable(tierList, columns);
    }

//    private void addTickets() throws IOException, InterruptedException {
//        String eventId = getNonEmptyInput("Enter Event ID:");
//        Event event = getAndVerifyEvent(eventId);
//        if (event == null) return;
//
//        Map<String, TicketTier> tiers = event.getTicketTiersMap();
//        if (tiers.isEmpty()) {
//            System.out.println("No ticket tiers available.");
//            return;
//        }
//
//        String tierId = selectTicketTier(tiers);
//        if (tierId == null) return;
//        TicketTier selectedTier = tiers.get(tierId);
//
//        int maxAddable = selectedTier.getTicketsTotal() -
//                selectedTier.getTicketsSold() -
//                selectedTier.getTicketsAvailable();
//
//        int amount = getPositiveInteger("Enter tickets to add (Max " + maxAddable + "):", maxAddable);
//
//        EventResponse response = eventCommandServiceBlockingStub.addTicketStock(
//                AddTicketStockRequest.newBuilder()
//                        .setEventId(eventId)
//                        .setTier(tierId)
//                        .setCount(amount)
//                        .build()
//        );
//
//        System.out.println(response.getSuccess()
//                ? "Tickets added successfully"
//                : "Error: " + response.getMessage());
//    }

    private void addTickets() throws IOException, InterruptedException {
        String eventId = getNonEmptyInput("Enter Event ID:");
        Event event = getAndVerifyEvent(eventId);
        if (event == null) return;

        Map<String, TicketTier> tiers = event.getTicketTiersMap();
        if (tiers.isEmpty()) {
            System.out.println("No ticket tiers available.");
            return;
        }

        String tierId = selectTicketTier(tiers);
        if (tierId == null) return;
        TicketTier selectedTier = tiers.get(tierId);

        int maxAddable = selectedTier.getTicketsTotal() -
                selectedTier.getTicketsSold() -
                selectedTier.getTicketsAvailable();

        // Handle zero available capacity first
        if (maxAddable <= 0) {
            System.out.println("Cannot add tickets to this tier - maximum capacity reached!");
            return;
        }

        // Get amount with continuous validation
        int amount;
        while (true) {
            amount = getPositiveInteger("Enter tickets to add (Max " + maxAddable + "):");

            if (amount <= maxAddable) {
                break;
            }
            System.out.println("Cannot add " + amount + " tickets. Maximum allowed: " + maxAddable);
        }

        EventResponse response = eventCommandServiceBlockingStub.addTicketStock(
                AddTicketStockRequest.newBuilder()
                        .setEventId(eventId)
                        .setTier(tierId)
                        .setCount(amount)
                        .build()
        );

        System.out.println(response.getSuccess()
                ? "Added " + amount + " tickets successfully"
                : "Error: " + response.getMessage());
    }

    private void addAfterPartTickets() throws IOException, InterruptedException {
        String eventId = getNonEmptyInput("Enter Event ID:");
        Event event = getAndVerifyEvent(eventId);
        if (event == null) return;

        int maxAddable = event.getAfterPartyTicketsTotal() -
                event.getAfterPartyTicketsSold() -
                event.getAfterPartyTicketsAvailable();

        int amount = getPositiveInteger("Enter after-party tickets to add (Max " + maxAddable + "):", maxAddable);

        EventResponse response = eventCommandServiceBlockingStub.addTicketStock(
                AddTicketStockRequest.newBuilder()
                        .setEventId(eventId)
                        .setAfterParty(true)
                        .setCount(amount)
                        .build()
        );

        System.out.println(response.getSuccess()
                ? "After-party tickets added successfully"
                : "Error: " + response.getMessage());
    }

    private void updateTicketTierPrice() throws IOException, InterruptedException {
        String eventId = getNonEmptyInput("Enter Event ID:");
        Event event = getAndVerifyEvent(eventId);
        if (event == null) return;

        String tierId = selectTicketTier(event.getTicketTiersMap());
        if (tierId == null) return;

        int newPrice = getPositiveInteger("Enter new ticket price:");

        Event updatedEvent = Event.newBuilder(event)
                .putTicketTiers(tierId, TicketTier.newBuilder(event.getTicketTiersMap().get(tierId))
                        .setPrice(newPrice)
                        .build())
                .build();

        EventResponse response = eventCommandServiceBlockingStub.updateEvent(
                UpdateEventRequest.newBuilder().setEvent(updatedEvent).build()
        );

        System.out.println(response.getSuccess()
                ? "Price updated successfully"
                : "Error: " + response.getMessage());
    }

    // Helper methods
    private Event getAndVerifyEvent(String eventId) throws IOException, InterruptedException {
        GetEventResponse response = eventQueryServiceBlockingStub.getEvent(
                GetEventRequest.newBuilder().setEventId(eventId).build()
        );
        if (!response.hasEvent()) {
            System.out.println("Event not found!");
            return null;
        }
        return response.getEvent();
    }

    private String selectTicketTier(Map<String, TicketTier> tiers) {
        List<String> tierIds = new ArrayList<>(tiers.keySet());
        System.out.println("Available Ticket Tiers:");
        for (int i = 0; i < tierIds.size(); i++) {
            System.out.printf("%d. %s%n", i+1, tierIds.get(i));
        }
        int selection = getValidSelection("Select tier:", 1, tierIds.size());
        return tierIds.get(selection-1);
    }

    private String getNonEmptyInput(String prompt) {
        while (true) {
            System.out.println(prompt);
            String input = scanner.nextLine().trim();
            if (!input.isEmpty()) return input;
            System.out.println("Input cannot be empty!");
        }
    }

    private int getPositiveInteger(String prompt) {
        return getPositiveInteger(prompt, Integer.MAX_VALUE);
    }

    private int getPositiveInteger(String prompt, int maxValue) {
        while (true) {
            try {
                System.out.println(prompt);
                int value = Integer.parseInt(scanner.nextLine().trim());
                if (value > 0 && value <= maxValue) return value;
                System.out.println("Please enter a positive number" +
                        (maxValue < Integer.MAX_VALUE ? " up to " + maxValue : ""));
            } catch (NumberFormatException e) {
                System.out.println("Invalid number format!");
            }
        }
    }

    private int getValidSelection(String prompt, int min, int max) {
        while (true) {
            try {
                System.out.println(prompt);
                int selection = Integer.parseInt(scanner.nextLine().trim());
                if (selection >= min && selection <= max) return selection;
                System.out.println("Please enter a number between " + min + " and " + max);
            } catch (NumberFormatException e) {
                System.out.println("Invalid number format!");
            }
        }
    }

//    private boolean askYesNo(String prompt) {
//        while (true) {
//            System.out.println(prompt + " (Y/N)");
//            String input = scanner.nextLine().trim().toUpperCase();
//            if (input.equals("Y")) return true;
//            if (input.equals("N")) return false;
//            System.out.println("Please enter Y or N!");
//        }
//    }
}


//public class BoxOfficeClerksClient extends Client {
//
//    private Scanner scanner = new Scanner(System.in);
//
//    public BoxOfficeClerksClient() throws IOException, InterruptedException {
//        super();
//    }
//
//    @Override
//    protected void processUserRequests() {
//        promptBox("Welcome to the Box Office Coordinator Client");
//        boolean running = true;
//        while (running) {
//            try {
//                System.out.println();
//                System.out.println("Select Command: ");
//                numberMenu(new String[]{
//                        "Get Events",
//                        "Get Ticket Tiers",
//                        "Add Tickets",
//                        "Add After Party Tickets",
//                        "Update Ticket Tier Price",
//                        "Quit"
//                });
//                int command = scanner.nextInt();
//                scanner.nextLine(); // Consume the next line.
//
//                switch (command) {
//                    case 1:
//                        getEvents();
//                        break;
//                    case 2:
//                        getTicketTiers();
//                        break;
//                    case 3:
//                        addTickets();
//                        break;
//                    case 4:
//                        addAfterPartTickets();
//                        break;
//                    case 5:
//                        updateTicketTierPrice();
//                        break;
//                    case 6:
//                        System.out.println("Quitting...");
//                        running = false;
//                        break;
//                    default:
//                        System.out.println("Invalid command");
//                        break;
//                }
//            } catch (IOException | InterruptedException e) {
//                System.err.println("Error: " + e.getMessage());
//            } catch (InputMismatchException e) {
//                scanner.nextLine();
//                System.out.println("Invalid Input.");
//            }
//        }
//    }
//
//    //    @Override
////    protected void initializeConnection() {
////        super.initializeConnection();
////        getEventServiceBlockingStub = GetEventServiceGrpc.newBlockingStub(channel);
////        getTicketTierServiceBlockingStub = GetTicketTierServiceGrpc.newBlockingStub(channel);
////        ticketServiceBlockingStub = TicketServiceGrpc.newBlockingStub(channel);
////        ticketTierServiceBlockingStub = TicketTierServiceGrpc.newBlockingStub(channel);
////        channel.getState(true);
////    }
////
////    @Override
////    protected void processUserRequests() {
////        Scanner scanner = new Scanner(System.in);
////        boolean running = true;
////        while (running) {
////            try {
////                promptBox("Welcome to the Box Office Coordinator Client");
////                numberMenu(new String[]{
////                        "Get Events", "Get Ticket Tiers",
////                        "Add Tickets", "Add After Party Tickets",
////                        "Update Ticket Tier Price",
////                        "Quit"
////                });
////                int mainMenuChoice = scanner.nextInt();
////                scanner.nextLine();
////                switch (mainMenuChoice) {
////                    case 1:
////                        promptBox("Get Events");
////
////                        GetEventsRequest getEventsRequest = GetEventsRequest
////                                .newBuilder()
////                                .build();
////                        waitForConnection();
////
////                        GetEventsResponse getEventsResponse = getEventServiceBlockingStub.getEvents(getEventsRequest);
////                        List<Event> eventsList = getEventsResponse.getEventsList();
////
////                        LinkedHashMap<String, Function<Event, ?>> columnsEvent = new LinkedHashMap<>();
////                        columnsEvent.put("Event Id", Event::getEventId);
////                        columnsEvent.put("Event Name", Event::getEventId);
////                        columnsEvent.put("Total Tickets", Event::getTotalTickets);
////                        columnsEvent.put("Tickets Sold", Event::getTicketsSold);
////                        columnsEvent.put("Tickets Available", Event::getTicketsAvailableToPurchase);
////                        columnsEvent.put("Total After Party Tickets", Event::getTotalAfterPartyTickets);
////                        columnsEvent.put("After Party Tickets Sold", Event::getTotalAfterPartyTicketsSold);
////                        columnsEvent.put("After Party Tickets Available", Event::getTotalAfterPartTicketsAvailableToPurchase);
////
////                        printTable(eventsList, columnsEvent);
////                        break;
////                    case 2:
////                        promptBox("Get Ticket Tiers");
////
////                        GetTicketTiersRequest getTicketTiersRequest = GetTicketTiersRequest
////                                .newBuilder()
////                                .build();
////                        waitForConnection();
////
////                        GetTicketTiersResponse getTicketTiersResponse = getTicketTierServiceBlockingStub.getTicketTiers(getTicketTiersRequest);
////                        List<TicketTier> ticketTierList = getTicketTiersResponse.getTicketTierList();
////
////                        // filter for only those with ticketsAvailableToPurchase > 0
////                        List<TicketTier> availableTiers = ticketTierList.stream()
////                                .filter(tier -> tier.getTicketsAvailableToPurchase() > 0)
////                                .collect(Collectors.toList());
////
////                        LinkedHashMap<String, Function<TicketTier, ?>> columns = new LinkedHashMap<>();
////                        columns.put("Ticket Tier Id", TicketTier::getTicketTierId);
////                        columns.put("Ticket Tier Name", TicketTier::getTicketTierName);
////                        columns.put("Ticket Price", TicketTier::getTicketTierPrice);
////                        columns.put("Total Tickets ", TicketTier::getTotalTickets);
////                        columns.put("Tickets Sold", TicketTier::getTicketsSold);
////                        columns.put("Tickets Available", TicketTier::getTicketsAvailableToPurchase);
////
////                        printTable(availableTiers, columns);
////                        break;
////                    case 3:
////                        promptBox("Add Tickets");
////
////                        System.out.println("Enter Event Id: ");
////                        int eventId = scanner.nextInt();
////                        scanner.nextLine();
////
////                        System.out.println("Enter Ticket Tier Id: ");
////                        int ticketTierId = scanner.nextInt();
////                        scanner.nextLine();
////
////                        System.out.println("Enter No of Tickets to Add: ");
////                        int ticketsToAdd = scanner.nextInt();
////                        scanner.nextLine();
////
////                        AddTicketsRequest addTicketsRequest = AddTicketsRequest
////                                .newBuilder()
////                                .setEventId(eventId)
////                                .setTicketTierId(ticketTierId)
////                                .setNoOfTicketsToAdd(ticketsToAdd)
////                                .build();
////                        waitForConnection();
////
////                        AddTicketsResponse addTicketsResponse = ticketServiceBlockingStub.addTickets(addTicketsRequest);
////                        if (addTicketsResponse.getSuccess()) {
////                            System.out.println("Tickets Added");
////                        } else {
////                            System.out.println("Tickets Not Added");
////                            System.out.println(addTicketsResponse.getMessage());
////                        }
////                        break;
////                    case 4:
////                        promptBox("Add After Party Tickets");
////
////                        System.out.println("Enter Event Id: ");
////                        int eventId1 = scanner.nextInt();
////                        scanner.nextLine();
////
////                        System.out.println("Enter  No of Tickets to Add: ");
////                        int ticketsToAdd1 = scanner.nextInt();
////                        scanner.nextLine();
////
////                        AddAfterPartyTicketsRequest addAfterPartyTicketsRequest = AddAfterPartyTicketsRequest
////                                .newBuilder()
////                                .setEventId(eventId1)
////                                .setNoOfTicketsToAdd(ticketsToAdd1)
////                                .build();
////                        waitForConnection();
////
////                        AddAfterPartyTicketsResponse addAfterPartyTicketsResponse = ticketServiceBlockingStub.addAfterPartyTickets(addAfterPartyTicketsRequest);
////                        if (addAfterPartyTicketsResponse.getSuccess()) {
////                            System.out.println("Tickets Added");
////                        } else {
////                            System.out.println("Tickets Not Added");
////                            System.out.println(addAfterPartyTicketsResponse.getMessage());
////                        }
////                        break;
////                    case 5:
////                        promptBox("Update Ticket Tier Price");
////
////                        System.out.println("Enter Event Id: ");
////                        int eventIdUpdate = scanner.nextInt();
////                        scanner.nextLine();
////
////                        System.out.println("Enter Ticket Tier Id: ");
////                        int ticketTierIdUpdate = scanner.nextInt();
////                        scanner.nextLine();
////
////                        GetTicketTierRequest getTicketTierRequest = GetTicketTierRequest
////                                .newBuilder()
////                                .setEventId(eventIdUpdate)
////                                .setTicketTierId(ticketTierIdUpdate)
////                                .build();
////                        waitForConnection();
////
////                        GetTicketTierResponse getTicketTierResponse = getTicketTierServiceBlockingStub.getTicketTier(getTicketTierRequest);
////                        TicketTier currentTicketTier = getTicketTierResponse.getTicketTier();
////
////                        System.out.println("Enter New Ticket Tier Price: ");
////                        int newTicketTierPrice = scanner.nextInt();
////                        scanner.nextLine();
////
////                        UpdateTicketTierRequest updateTicketTierRequest = UpdateTicketTierRequest
////                                .newBuilder()
////                                .setEventId(eventIdUpdate)
////                                .setTicketTierId(ticketTierIdUpdate)
////                                .setTicketTierPrice(newTicketTierPrice)
////                                .setTicketTierName(currentTicketTier.getTicketTierName())
////                                .setTotalTickets(currentTicketTier.getTotalTickets())
////                                .build();
////                        waitForConnection();
////
////                        UpdateTicketTierResponse updateTicketTierResponse = ticketTierServiceBlockingStub.updateTicketTier(updateTicketTierRequest);
////                        if (updateTicketTierResponse.getSuccess()) {
////                            System.out.println("Ticket Tier Updated");
////                        } else {
////                            System.out.println("Ticket Tier Not Updated");
////                            System.out.println(updateTicketTierResponse.getMessage());
////                        }
////                        break;
////                    case 6:
////                        promptBox("Quit");
////                        running = false;
////                        break;
////                    default:
////                        alertBox("Invalid choice", '!');
////                        break;
////                }
////            } catch (RuntimeException | IOException | InterruptedException e) {
////                System.out.println("Error: " + e.getMessage());
////            }
////        }
////    }
//
//
//    private void getEvents() throws IOException, InterruptedException {
//        promptBox("Get Events");
//
//        GetEventsRequest getEventsRequest = GetEventsRequest
//                .newBuilder()
//                .build();
//        waitForConnection();
//
//        GetEventsResponse getEventsResponse = eventQueryServiceBlockingStub.getEvents(getEventsRequest);
//        List<Event> eventsList = getEventsResponse.getEventsList();
//
//        if (eventsList.isEmpty()) {
//            System.err.println("No events found.");
//            return;
//        }
//
//        LinkedHashMap<String, Function<Event, ?>> columnsEvents = new LinkedHashMap<>();
//        columnsEvents.put("Id", Event::getId);
//        columnsEvents.put("Name", Event::getName);
//        columnsEvents.put("Date", Event::getDate);
//        columnsEvents.put("Total Tickets", Event::getEventTicketsTotal);
//        columnsEvents.put("Tickets Sold", Event::getEventTicketsSold);
//        columnsEvents.put("Tickets Available", Event::getEventTicketsAvailable);
//        columnsEvents.put("Total After Party Tickets", Event::getAfterPartyTicketsTotal);
//        columnsEvents.put("After Party Tickets Sold", Event::getAfterPartyTicketsSold);
//        columnsEvents.put("After Party Tickets Available", Event::getAfterPartyTicketsAvailable);
//
//        printTable(eventsList, columnsEvents);
//    }
//
//    private Event getEvent(String eventId) throws IOException, InterruptedException {
//        GetEventRequest getEventRequest = GetEventRequest
//                .newBuilder()
//                .setEventId(eventId)
//                .build();
//        waitForConnection();
//        GetEventResponse getEventResponse = eventQueryServiceBlockingStub.getEvent(getEventRequest);
//        return getEventResponse.getEvent();
//    }
//
//    private void getTicketTiers() throws IOException, InterruptedException {
//        promptBox("Get Ticket Tiers of an Event");
//
//        System.out.println("Enter Event Id: ");
//        String eventId = scanner.nextLine();
//
//        Event currentEvent = getEvent(eventId);
//
//        if (!currentEvent.getId().equals(eventId)) {
//            alertBox("Event not found", '!');
//            return;
//        }
//
//        Map<String, TicketTier> ticketTiersMap = currentEvent.getTicketTiersMap();
//        ArrayList<TicketTier> ticketTiersList = new ArrayList<>(ticketTiersMap.values());
//
//        if (ticketTiersList.isEmpty()) {
//            System.err.println("There are no Ticket Tiers in this Event");
//            return;
//        }
//
//        LinkedHashMap<String, Function<TicketTier, ?>> columnsTicketTier = new LinkedHashMap<>();
//        columnsTicketTier.put("Id", TicketTier::getId);
//        columnsTicketTier.put("Price", TicketTier::getPrice);
//        columnsTicketTier.put("Tickets Total", TicketTier::getTicketsTotal);
//        columnsTicketTier.put("Tickets Sold", TicketTier::getTicketsSold);
//        columnsTicketTier.put("Tickets Available", TicketTier::getTicketsAvailable);
//
//        printTable(ticketTiersList, columnsTicketTier);
//    }
//
//    private void addTickets() throws IOException, InterruptedException {
//        promptBox("Add Tickets");
//
//        System.out.println("Enter Event Id: ");
//        String eventId = scanner.nextLine();
//
//        Event currentEvent = getEvent(eventId);
//
//        if (!currentEvent.getId().equals(eventId)) {
//            alertBox("Event not found", '!');
//            return;
//        }
//
//        Map<String, TicketTier> ticketTiersMap = currentEvent.getTicketTiersMap();
//        ArrayList<String> ticketTiersIdList = new ArrayList<>(ticketTiersMap.keySet());
//
//        if (ticketTiersIdList.isEmpty()) {
//            System.err.println("There are no Ticket Tiers in this Event");
//            return;
//        }
//
//        String[] ticketTiersIdArray = ticketTiersIdList.toArray(new String[0]);
//
//        System.out.println("Select Ticket Tier to Add Tickets:");
//        numberMenu(ticketTiersIdArray);
//        int ticketTierSelection = scanner.nextInt();
//        scanner.nextLine();
//
//        String ticketTierId = ticketTiersIdArray[ticketTierSelection-1];
//        TicketTier ticketTier = ticketTiersMap.get(ticketTierId);
//        int ticketsRemaining = ticketTier.getTicketsTotal() - ticketTier.getTicketsSold() - ticketTier.getTicketsAvailable();
//
//        System.out.println("Enter amount of tickets to add[" + ticketsRemaining + "]:");
//        int amount = scanner.nextInt();
//        scanner.nextLine();
//
//        if (amount > ticketsRemaining) {
//            System.err.println("Ticket amount is greater than tickets available");
//            return;
//        }
//
//        AddTicketStockRequest request = AddTicketStockRequest
//                .newBuilder()
//                .setEventId(eventId)
//                .setTier(ticketTierId)
//                .setAfterParty(false)
//                .setCount(amount)
//                .build();
//        waitForConnection();
//
//        EventResponse addTicketStockRespond = eventCommandServiceBlockingStub.addTicketStock(request);
//        if (addTicketStockRespond.getSuccess()) {
//            System.out.println("Added Ticket Stock");
//        } else {
//            System.err.println("Failed to add Ticket Stock");
//            System.err.println(addTicketStockRespond.getMessage());
//        }
//    }
//
//    private void addAfterPartTickets() throws IOException, InterruptedException {
//        promptBox("Add After Party Tickets");
//
//        System.out.println("Enter Event Id: ");
//        String eventId = scanner.nextLine();
//
//        Event currentEvent = getEvent(eventId);
//
//        if (!currentEvent.getId().equals(eventId)) {
//            alertBox("Event not found", '!');
//            return;
//        }
//
//        int ticketsRemaining = currentEvent.getAfterPartyTicketsTotal() - currentEvent.getAfterPartyTicketsSold() - currentEvent.getAfterPartyTicketsAvailable();
//
//        System.out.println("Enter amount of tickets to add[" + ticketsRemaining + "]:");
//        int amount = scanner.nextInt();
//        scanner.nextLine();
//
//
//        if (amount > ticketsRemaining) {
//            System.err.println("Ticket amount is greater than current ticket amount available");
//            return;
//        }
//
//        AddTicketStockRequest request = AddTicketStockRequest
//                .newBuilder()
//                .setEventId(eventId)
//                .setAfterParty(true)
//                .setCount(amount)
//                .build();
//        waitForConnection();
//
//        EventResponse addTicketStockRespond = eventCommandServiceBlockingStub.addTicketStock(request);
//        if (addTicketStockRespond.getSuccess()) {
//            System.out.println("Added Ticket Stock");
//        } else {
//            System.err.println("Failed to add Ticket Stock");
//            System.err.println(addTicketStockRespond.getMessage());
//        }
//    }
//
//    private void updateTicketTierPrice() throws IOException, InterruptedException {
//        promptBox("Update Ticket Tier Price");
//
//        System.out.println("Enter Event Id: ");
//        String eventId = scanner.nextLine();
//
//        Event currentEvent = getEvent(eventId);
//
//        if (!currentEvent.getId().equals(eventId)) {
//            alertBox("Event not found", '!');
//            return;
//        }
//
//        Map<String, TicketTier> ticketTiersMap = currentEvent.getTicketTiersMap();
//        ArrayList<String> ticketTiersIdList = new ArrayList<>(ticketTiersMap.keySet());
//
//        String[] ticketTiersIdArray = ticketTiersIdList.toArray(new String[0]);
//
//        System.out.println("Select Ticket Tier to Update");
//        numberMenu(ticketTiersIdArray);
//        int ticketTierSelection = scanner.nextInt();
//        scanner.nextLine();
//
//        TicketTier currentTicketTier = ticketTiersMap.get(ticketTiersIdArray[ticketTierSelection-1]);
//
//        TicketTier.Builder newTicketTierBuilder = TicketTier
//                .newBuilder()
//                .mergeFrom(currentTicketTier);
//
//
//        System.out.println("Enter New Ticket Tier Price: ");
//        int newTicketTierPrice = scanner.nextInt();
//        scanner.nextLine();
//        newTicketTierBuilder.setPrice(newTicketTierPrice);
//
//        TicketTier newTicketTier = newTicketTierBuilder.build();
//
//        Event newEvent = Event
//                .newBuilder()
//                .mergeFrom(currentEvent)
//                .putTicketTiers(newTicketTier.getId(), newTicketTier)
//                .build();
//
//        UpdateEventRequest updateEventRequest = UpdateEventRequest
//                .newBuilder()
//                .setEvent(newEvent)
//                .build();
//        waitForConnection();
//
//        EventResponse updateEventResponse = eventCommandServiceBlockingStub.updateEvent(updateEventRequest);
//        if (updateEventResponse.getSuccess()) {
//            System.out.println("Ticket Tier Updated Successfully");
//        } else {
//            System.out.println("Ticket Tier Not Updated");
//            System.out.println(updateEventResponse.getMessage());
//        }
//    }
//}
