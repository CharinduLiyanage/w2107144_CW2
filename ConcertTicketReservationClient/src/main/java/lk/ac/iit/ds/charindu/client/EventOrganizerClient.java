package lk.ac.iit.ds.charindu.client;

import lk.ac.iit.ds.charindu.grpc.generated.*;

import java.io.IOException;
import java.util.*;
import java.util.function.Function;

import static java.lang.System.currentTimeMillis;
import static lk.ac.iit.ds.charindu.client.util.CLIShapes.*;


public class EventOrganizerClient extends Client {

    private Scanner scanner = new Scanner(System.in);

    public EventOrganizerClient() throws IOException, InterruptedException {
        super();
    }

    @Override
    protected void processUserRequests() {
        promptBox("Welcome to Concert Organizer Client");
        boolean running = true;
        while (running) {
            try {
                System.out.println();
                System.out.println("Select Command: ");
                numberMenu(new String[]{
                        "Get Events",
                        "Get Ticket Tiers",
                        "Add New Event",
                        "Add New Ticket Tier",
                        "Update Event",
                        "Update Ticket Tiers",
                        "Remove Event",
                        "Remove Ticket Tiers",
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
                        addEvent();
                        break;
                    case 4:
                        addTicketTier();
                        break;
                    case 5:
                        updateEvent();
                        break;
                    case 6:
                        updateTicketTier();
                        break;
                    case 7:
                        removeEvent();
                        break;
                    case 8:
                        removeTicketTier();
                        break;
                    case 9:
                        System.out.println("Quitting...");
                        running = false;
                        break;
                    default:
                        alertBox("Invalid command", '!');
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
//        eventServiceBlockingStub = EventServiceGrpc.newBlockingStub(channel);
//        ticketTierServiceBlockingStub = TicketTierServiceGrpc.newBlockingStub(channel);
//        channel.getState(true);
//    }
//
//    @Override
//    protected void processUserRequests() {
//        promptBox("Welcome to Concert Organizer Client");
//        Scanner scanner = new Scanner(System.in);
//        boolean running = true;
//        while (running) {
//            try {
//                System.out.println("Select Command: ");
//                numberMenu(new String[]{
//                        "Get Events", "Get Ticket Tiers",
//                        "Add Event", "Add Ticket Tiers",
//                        "Update Event", "Update Ticket Tiers",
//                        "Remove Event", "Remove Ticket Tiers",
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
//                        columnsEvent.put("Event Name", Event::getEventName);
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
//                        columns.put("No of Tickets Available", TicketTier::getTicketsAvailableToPurchase);
//
//                        printTable(availableTiers, columns);
//                        break;
//                    case 3:
//                        promptBox("Add Event");
//
//                        System.out.println("Enter Event Name: ");
//                        String eventName = scanner.nextLine();
//
//                        System.out.println("Enter Total Number of Tickets Available: ");
//                        int ticketsTotal = scanner.nextInt();
//                        scanner.nextLine();
//
//                        System.out.println("Enter Total Number of After Party Tickets Available: ");
//                        int afterPartTicketsTotal = scanner.nextInt();
//                        scanner.nextLine();
//
//                        AddEventRequest addEventRequest = AddEventRequest
//                                .newBuilder()
//                                .setEventName(eventName)
//                                .setTotalTickets(ticketsTotal)
//                                .setTotalAfterPartyTickets(afterPartTicketsTotal)
//                                .build();
//                        waitForConnection();
//
//                        AddEventResponse addEventResponse = eventServiceBlockingStub.addEvent(addEventRequest);
//                        if (addEventResponse.getSuccess()) {
//                            System.out.println("Event Added");
//                        } else {
//                            System.out.println("Event Not Added");
//                            System.out.println(addEventResponse.getMessage());
//                        }
//                        break;
//                    case 4:
//                        promptBox("Add Ticket Tiers");
//
//                        System.out.println("Enter Event Id: ");
//                        int eventId = scanner.nextInt();
//                        scanner.nextLine();
//
//                        System.out.println("Enter Ticket Tier Name: ");
//                        String ticketTierName = scanner.nextLine();
//
//                        System.out.println("Enter Ticket Tier Price: ");
//                        int ticketPrice = scanner.nextInt();
//                        scanner.nextLine();
//
//                        System.out.println("Enter Total Number of Tickets Available: ");
//                        int totalTickets = scanner.nextInt();
//                        scanner.nextLine();
//
//                        AddTicketTierRequest addTicketTierRequest = AddTicketTierRequest
//                                .newBuilder()
//                                .setEventId(eventId)
//                                .setTicketTierName(ticketTierName)
//                                .setTicketPrice(ticketPrice)
//                                .setTotalTickets(totalTickets)
//                                .build();
//                        waitForConnection();
//
//                        AddTicketTierResponse addTicketTierResponse = ticketTierServiceBlockingStub.addTicketTier(addTicketTierRequest);
//                        if (addTicketTierResponse.getSuccess()) {
//                            System.out.println("Ticket Tier Added");
//                        } else {
//                            System.out.println("Ticket Tier Not Added");
//                            System.out.println(addTicketTierResponse.getMessage());
//                        }
//                        break;
//                    case 5:
//                        promptBox("Update Event");
//
//                        System.out.println("Enter Event Id: ");
//                        int eventIdUpdate = scanner.nextInt();
//                        scanner.nextLine();
//
//                        GetEventRequest getEventRequest = GetEventRequest
//                                .newBuilder()
//                                .setId(eventIdUpdate)
//                                .build();
//                        waitForConnection();
//
//                        GetEventResponse getEventResponse = getEventServiceBlockingStub.getEvent(getEventRequest);
//                        Event currentEvent = getEventResponse.getEvent();
//
//                        UpdateEventRequest.Builder updateEventRequestBuilder = UpdateEventRequest.newBuilder();
//
//                        System.out.println("Update the Event Name: [" + currentEvent.getEventName() + "] ? (Y/N)");
//                        String isUpdateEventName = scanner.nextLine();
//                        if (isUpdateEventName.equalsIgnoreCase("Y")) {
//                            System.out.println("Enter New Event Name: ");
//                            String newEventName = scanner.nextLine();
//                            updateEventRequestBuilder.setEventName(newEventName);
//                        } else {
//                            updateEventRequestBuilder.setEventName(currentEvent.getEventName());
//                        }
//
//                        System.out.println("Update the total no of tickets: [" + currentEvent.getTotalTickets() + "] ? (Y/N)");
//                        String isUpdateTotalNoOfTickets = scanner.nextLine();
//                        if (isUpdateTotalNoOfTickets.equalsIgnoreCase("Y")) {
//                            System.out.println("Enter New Total Number of Tickets: ");
//                            int newTotalTickets = scanner.nextInt();
//                            scanner.nextLine();
//                            updateEventRequestBuilder.setTotalTickets(newTotalTickets);
//                        } else {
//                            updateEventRequestBuilder.setTotalTickets(currentEvent.getTotalTickets());
//                        }
//
//                        System.out.println("Update the total no of after party tickets: [" + currentEvent.getTotalAfterPartyTickets() + "] (Y/N)");
//                        String isUpdateTotalNoOfAfterPartyTickets = scanner.nextLine();
//                        if (isUpdateTotalNoOfAfterPartyTickets.equalsIgnoreCase("Y")) {
//                            System.out.println("Enter New Total Number of after party Tickets: ");
//                            int newTotalAfterPartyTickets = scanner.nextInt();
//                            scanner.nextLine();
//                            updateEventRequestBuilder.setTotalAfterPartyTickets(newTotalAfterPartyTickets);
//                        } else {
//                            updateEventRequestBuilder.setTotalAfterPartyTickets(currentEvent.getTotalAfterPartyTickets());
//                        }
//
//                        UpdateEventRequest updateEventRequest = updateEventRequestBuilder.build();
//                        waitForConnection();
//
//                        UpdateEventResponse updateEventResponse = eventServiceBlockingStub.updateEvent(updateEventRequest);
//                        if (updateEventResponse.getSuccess()) {
//                            System.out.println("Event Updated");
//                        } else {
//                            System.out.println("Event Not Updated");
//                            System.out.println(updateEventResponse.getMessage());
//                        }
//                        break;
//                    case 6:
//                        promptBox("Update Ticket Tiers");
//
//                        System.out.println("Enter Event Id: ");
//                        int eventIdUpdate2 = scanner.nextInt();
//                        scanner.nextLine();
//
//                        System.out.println("Enter Ticket Tier Id: ");
//                        int ticketTierIdUpdate = scanner.nextInt();
//                        scanner.nextLine();
//
//                        GetTicketTierRequest getTicketTierRequest = GetTicketTierRequest
//                                .newBuilder()
//                                .setEventId(eventIdUpdate2)
//                                .setTicketTierId(ticketTierIdUpdate)
//                                .build();
//                        waitForConnection();
//
//                        GetTicketTierResponse getTicketTierResponse = getTicketTierServiceBlockingStub.getTicketTier(getTicketTierRequest);
//                        TicketTier currentTicketTier = getTicketTierResponse.getTicketTier();
//
//                        UpdateTicketTierRequest.Builder updateTicketTierRequestBuilder = UpdateTicketTierRequest.newBuilder();
//
//                        System.out.println("Update Ticket Tier Name: [" + currentTicketTier.getTicketTierName() + "] ? (Y/N)");
//                        String isUpdateTicketTierName = scanner.nextLine();
//                        if (isUpdateTicketTierName.equalsIgnoreCase("Y")) {
//                            System.out.println("Enter New Ticket Tier Name: ");
//                            String newTicketTierName = scanner.nextLine();
//                            updateTicketTierRequestBuilder.setTicketTierName(newTicketTierName);
//                        } else {
//                            updateTicketTierRequestBuilder.setTicketTierName(currentTicketTier.getTicketTierName());
//                        }
//
//                        System.out.println("Update Ticket Tier Price: [" + currentTicketTier.getTicketTierPrice() + "] (Y/N)");
//                        String isUpdateTicketTierPrice = scanner.nextLine();
//                        if (isUpdateTicketTierPrice.equalsIgnoreCase("Y")) {
//                            System.out.println("Enter New Ticket Tier Price: ");
//                            int newTicketTierPrice = scanner.nextInt();
//                            scanner.nextLine();
//                            updateTicketTierRequestBuilder.setTicketTierPrice(newTicketTierPrice);
//                        } else {
//                            updateTicketTierRequestBuilder.setTicketTierPrice(currentTicketTier.getTicketTierPrice());
//                        }
//
//                        System.out.println("Update Ticket Tier Total Tickets: [" + currentTicketTier.getTotalTickets() + "] (Y/N)");
//                        String isUpdateTicketTierTotalTickets = scanner.nextLine();
//                        if (isUpdateTicketTierTotalTickets.equalsIgnoreCase("Y")) {
//                            System.out.println("Enter New Ticket Tier Total Tickets: ");
//                            int newTicketTierTotalTickets = scanner.nextInt();
//                            scanner.nextLine();
//                            updateTicketTierRequestBuilder.setTotalTickets(newTicketTierTotalTickets);
//                        } else {
//                            updateTicketTierRequestBuilder.setTotalTickets(currentTicketTier.getTotalTickets());
//                        }
//
//                        UpdateTicketTierRequest updateTicketTierRequest = updateTicketTierRequestBuilder.build();
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
//                    case 7:
//                        promptBox("Remove Event");
//
//                        System.out.println("Enter Event Id: ");
//                        int eventIdCancel = scanner.nextInt();
//                        scanner.nextLine();
//
//                        CancelEventRequest cancelEventRequest = CancelEventRequest
//                                .newBuilder()
//                                .setEventId(eventIdCancel)
//                                .build();
//                        waitForConnection();
//
//                        CancelEventResponse cancelEventResponse = eventServiceBlockingStub.cancelEvent(cancelEventRequest);
//                        if (cancelEventResponse.getSuccess()) {
//                            System.out.println("Event Canceled");
//                        } else {
//                            System.out.println("Event Not Canceled");
//                            System.out.println(cancelEventResponse.getMessage());
//                        }
//                        break;
//                    case 8:
//                        promptBox("Remove Ticket Tiers");
//
//                        System.out.println("Enter Event Id: ");
//                        int eventIdRemove = scanner.nextInt();
//                        scanner.nextLine();
//
//                        System.out.println("Enter Ticket Tier Id: ");
//                        int ticketTierIdRemove = scanner.nextInt();
//                        scanner.nextLine();
//
//                        CancelTicketTierRequest cancelTicketTierRequest = CancelTicketTierRequest
//                                .newBuilder()
//                                .setEventId(eventIdRemove)
//                                .setTicketTierId(ticketTierIdRemove)
//                                .build();
//                        waitForConnection();
//
//                        CancelTicketTierResponse cancelTicketTierResponse = ticketTierServiceBlockingStub.cancelTicketTier(cancelTicketTierRequest);
//                        if (cancelTicketTierResponse.getSuccess()) {
//                            System.out.println("Ticket Tier Removed");
//                        } else {
//                            System.out.println("Ticket Tier Not Removed");
//                            System.out.println(cancelTicketTierResponse.getMessage());
//                        }
//                        break;
//                    case 9:
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

    private void addEvent() throws IOException, InterruptedException {
        promptBox("Add New Event");

        System.out.println("Enter Event Name: ");
        String eventName = scanner.nextLine();

        System.out.println("Enter Event Date (YYYY-MM-DD): ");
        String eventDate = scanner.nextLine();
        // todo: validate date

        System.out.println("Enter Total Number of Tickets Available: ");
        int ticketsTotal = scanner.nextInt();
        scanner.nextLine();
        // todo: validate not negative

        System.out.println("Enter Total Number of After Party Tickets Available: ");
        int afterPartTicketsTotal = scanner.nextInt();
        scanner.nextLine();
        // todo: cannot have more afterparty tickets than normal tickets, and not negative

        String eventId = eventName + "-" + eventDate + "-" + currentTimeMillis();

        AddEventRequest addEventRequest = AddEventRequest
                .newBuilder()
                .setEvent(
                        Event.newBuilder()
                                .setId(eventId)
                                .setName(eventName)
                                .setDate(eventDate)
                                .setEventTicketsTotal(ticketsTotal)
                                .setAfterPartyTicketsTotal(afterPartTicketsTotal)
                                .setEventTicketsSold(0)
                                .setEventTicketsAvailable(0)
                                .setAfterPartyTicketsSold(0)
                                .setAfterPartyTicketsAvailable(0)
                                .build()
                )
                .build();
        waitForConnection();

        EventResponse eventResponse = eventCommandServiceBlockingStub.addEvent(addEventRequest);
        if (eventResponse.getSuccess()) {
            System.out.println("Event Added");
        } else {
            System.err.println("Event Not Added: " + eventResponse.getMessage());
        }
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

    private void updateEvent() throws IOException, InterruptedException {
        promptBox("Update Event");

        System.out.println("Enter Event Id: ");
        String eventId = scanner.nextLine();

        Event currentEvent = getEvent(eventId);

        if (!currentEvent.getId().equals(eventId)) {
            alertBox("Event not found", '!');
            return;
        }

        Event.Builder newEvenBuilder = Event
                .newBuilder()
                .mergeFrom(currentEvent);

        System.out.println("Update the Event Name: [" + currentEvent.getName() + "] ? (Y/N)");
        String isUpdateEventName = scanner.nextLine();
        if (isUpdateEventName.equalsIgnoreCase("Y")) {
            System.out.println("Enter New Event Name: ");
            String newEventName = scanner.nextLine();
            newEvenBuilder.setName(newEventName);
        }

        System.out.println("Update the total no of tickets: [" + currentEvent.getEventTicketsTotal() + "] ? (Y/N)");
        String isUpdateTotalNoOfTickets = scanner.nextLine();
        if (isUpdateTotalNoOfTickets.equalsIgnoreCase("Y")) {
            System.out.println("Enter New Total Number of Tickets: ");
            int newTotalTickets = scanner.nextInt();
            scanner.nextLine();
            // TODO: check for the amount that can be reduced, if its reduced. can not be negative
            newEvenBuilder.setEventTicketsTotal(newTotalTickets);
        }

        System.out.println("Update the total no of after party tickets: [" + currentEvent.getAfterPartyTicketsTotal() + "] (Y/N)");
        String isUpdateTotalNoOfAfterPartyTickets = scanner.nextLine();
        if (isUpdateTotalNoOfAfterPartyTickets.equalsIgnoreCase("Y")) {
            System.out.println("Enter New Total Number of after party Tickets: ");
            int newTotalAfterPartyTickets = scanner.nextInt();
            scanner.nextLine();
            // TODO: check for the amount that can be reduced, if its reduced.
            // todo: cannot have more afterparty tickets than normal tickets
            // todo: can not be negative
            newEvenBuilder.setAfterPartyTicketsTotal(newTotalAfterPartyTickets);
        }

        UpdateEventRequest updateEventRequest = UpdateEventRequest
                .newBuilder()
                .setEvent(newEvenBuilder.build())
                .build();
        waitForConnection();

        EventResponse updateEventResponse = eventCommandServiceBlockingStub.updateEvent(updateEventRequest);
        if (updateEventResponse.getSuccess()) {
            System.out.println("Event Updated");
        } else {
            System.out.println("Event Not Updated");
            System.out.println(updateEventResponse.getMessage());
        }
    }

    private void removeEvent() throws IOException, InterruptedException {
        promptBox("Remove Event");

        System.out.println("Enter Event Id: ");
        String eventId = scanner.nextLine();

        Event currentEvent = getEvent(eventId);
        if (!currentEvent.getId().equals(eventId)) {
            alertBox("Event not found", '!');
            return;
        }

        // todo: prompt how many tickets + afterpart tickets ahas been sold

        CancelEventRequest cancelEventRequest = CancelEventRequest
                .newBuilder()
                .setEventId(currentEvent.getId())
                .build();
        waitForConnection();

        EventResponse cancelEventResponse = eventCommandServiceBlockingStub.cancelEvent(cancelEventRequest);
        if (cancelEventResponse.getSuccess()) {
            System.out.println("Event Removed Successfully");
        } else
            System.out.println("Event Not Removed Successfully");
        System.err.println(cancelEventResponse.getMessage());
    }

    private void addTicketTier() throws IOException, InterruptedException {
        promptBox("Add Ticket Tier for an Event");

        System.out.println("Enter Event Id: ");
        String eventId = scanner.nextLine();

        Event currentEvent = getEvent(eventId);

        if (!currentEvent.getId().equals(eventId)) {
            alertBox("Event not found", '!');
            return;
        }

        Map<String, TicketTier> currentEventTicketTiersMap = currentEvent.getTicketTiersMap();

        System.out.println("Enter Ticket Tier Id: ");
        String ticketTierId = scanner.nextLine();

        if (currentEventTicketTiersMap.containsKey(ticketTierId)) {
            System.err.println("Ticket Tier already exists");
            return;
        }

        System.out.println("Enter Ticket Tier Price: ");
        int price = scanner.nextInt();
        scanner.nextLine();
        // todo: can not be negative

        int assignedTickets = 0;
        for (TicketTier tier : currentEventTicketTiersMap.values()) {
            assignedTickets += tier.getTicketsTotal();
        }

        int ticketsSlotsAvailable = currentEvent.getEventTicketsTotal() - assignedTickets;

        System.out.println("Enter Total Number of Tickets[" + ticketsSlotsAvailable + "]: ");
        int ticketsTotal = scanner.nextInt();
        scanner.nextLine();
        // todo: can not be negative

        if (ticketsTotal > ticketsSlotsAvailable) {
            System.err.println("Tickets not enough");
            return;
        }

        TicketTier newTicketTier = TicketTier
                .newBuilder()
                .setId(ticketTierId)
                .setPrice(price)
                .setTicketsTotal(ticketsTotal)
                .build();

        Event updatedEvent = Event
                .newBuilder()
                .mergeFrom(currentEvent)
                .putTicketTiers(ticketTierId, newTicketTier)
                .build();

        UpdateEventRequest updateEventRequest = UpdateEventRequest
                .newBuilder()
                .setEvent(updatedEvent)
                .build();

        EventResponse updateEventResponse = eventCommandServiceBlockingStub.updateEvent(updateEventRequest);
        if (updateEventResponse.getSuccess()) {
            System.out.println("Ticket Tier Added");
        } else {
            System.out.println("Ticket Tier Added");
            System.out.println(updateEventResponse.getMessage());
        }
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

    private void updateTicketTier() throws IOException, InterruptedException {
        promptBox("Update Ticket Tier for an Event");

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
        // todo: validate if there are ticket tiers in the event
        System.out.println("Select Ticket Tier to Update");
        numberMenu(ticketTiersIdArray);
        int ticketTierSelection = scanner.nextInt();
        scanner.nextLine();

        TicketTier currentTicketTier = ticketTiersMap.get(ticketTiersIdArray[ticketTierSelection-1]);

        TicketTier.Builder newTicketTierBuilder = TicketTier
                .newBuilder()
                .mergeFrom(currentTicketTier);

        System.out.println("Update Ticket Tier Id: [" + currentTicketTier.getId() + "] ? (Y/N)");
        String isUpdateTicketTierName = scanner.nextLine();
        if (isUpdateTicketTierName.equalsIgnoreCase("Y")) {
            System.out.println("Enter New Ticket Tier Id: ");
            String newTicketTierName = scanner.nextLine();
            if (ticketTiersIdList.contains(newTicketTierName)) {
                System.out.println("Ticket Tier Id already exists");
                return;
            }
            newTicketTierBuilder.setId(newTicketTierName);
        }

        System.out.println("Update Ticket Tier Price: [" + currentTicketTier.getPrice() + "] (Y/N)");
        String isUpdateTicketTierPrice = scanner.nextLine();
        if (isUpdateTicketTierPrice.equalsIgnoreCase("Y")) {
            System.out.println("Enter New Ticket Tier Price: ");
            int newTicketTierPrice = scanner.nextInt();
            scanner.nextLine();
            newTicketTierBuilder.setPrice(newTicketTierPrice);
        }

        System.out.println("Update Ticket Tier Total Tickets: [" + currentTicketTier.getTicketsTotal() + "] (Y/N)");
        String isUpdateTicketTierTotalTickets = scanner.nextLine();
        if (isUpdateTicketTierTotalTickets.equalsIgnoreCase("Y")) {
            System.out.println("Enter New Ticket Tier Total Tickets: ");
            int newTicketTierTotalTickets = scanner.nextInt();
            scanner.nextLine();
            // TODO: check for the amount that can be reduced, if its reduced.
            newTicketTierBuilder.setTicketsTotal(newTicketTierTotalTickets);
        }

        TicketTier newTicketTier = newTicketTierBuilder.build();

        Event newEvent = Event
                .newBuilder()
                .removeTicketTiers(currentTicketTier.getId())
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

    private void removeTicketTier() throws IOException, InterruptedException {
        promptBox("Remove Ticket Tier for an Event");

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

        System.out.println("Select Ticket Tier to Remove");
        numberMenu(ticketTiersIdArray);
        int ticketTierSelection = scanner.nextInt();
        scanner.nextLine();

        TicketTier currentTicketTier = ticketTiersMap.get(ticketTiersIdArray[ticketTierSelection-1]);

        Event newEvent = Event
                .newBuilder()
                .mergeFrom(currentEvent)
                .removeTicketTiers(currentTicketTier.getId())
                .build();

        UpdateEventRequest updateEventRequest = UpdateEventRequest
                .newBuilder()
                .setEvent(newEvent)
                .build();
        waitForConnection();

        EventResponse updateEventResponse = eventCommandServiceBlockingStub.updateEvent(updateEventRequest);
        if (updateEventResponse.getSuccess()) {
            System.out.println("Ticket Tier Removed Successfully");
        } else {
            System.err.println("Ticket Tier Not Removed");
            System.err.println(updateEventResponse.getMessage());
        }
    }
}

