//package lk.ac.iit.ds.charindu.client;
//
//import java.io.IOException;
//
//public class CustomerClient extends Client {
//
////    GetEventServiceGrpc.GetEventServiceBlockingStub getEventServiceBlockingStub;
////    GetTicketTierServiceGrpc.GetTicketTierServiceBlockingStub getTicketTierServiceBlockingStub;
////    BuyTicketServiceGrpc.BuyTicketServiceBlockingStub buyTicketServiceBlockingStub;
//
//    public CustomerClient() throws IOException, InterruptedException {
//        super();
//    }
//
//    @Override
//    protected void processUserRequests() {
//        promptBox("Welcome to Customer Client");
//        boolean running = true;
//        while (running) {
//            try {
//                System.out.println();
//                System.out.println("Select Command: ");
//                numberMenu(new String[]{
//                        "Get Events",
//                        "Get Ticket Tiers",
//                        "Buy Ticket",
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
//                        addEvent();
//                        break;
//                    case 4:
//                        addTicketTier();
//                        break;
//                    case 5:
//                        updateEvent();
//                        break;
//                    case 6:
//                        updateTicketTier();
//                        break;
//                    case 7:
//                        removeEvent();
//                        break;
//                    case 8:
//                        removeTicketTier();
//                        break;
//                    case 9:
//                        System.out.println("Quitting...");
//                        running = false;
//                        break;
//                    default:
//                        alertBox("Invalid command", '!');
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
////        buyTicketServiceBlockingStub = BuyTicketServiceGrpc.newBlockingStub(channel);
////        channel.getState(true);
////    }
////
////    @Override
////    protected void processUserRequests() {
////        Scanner scanner = new Scanner(System.in);
////        boolean running = true;
////
////        while (running) {
////            try {
////                promptBox("Welcome to the Customer Client");
////                numberMenu(new String[]{
////                        "Get Events",
////                        "Get Ticket Tiers",
////                        "Buy Ticket",
////                        "Quit"
////                });
////                int mainMenuSelection = scanner.nextInt();
////                scanner.nextLine();
////
////                switch (mainMenuSelection) {
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
////                        // filter for only those with ticketsAvailableToPurchase > 0
////                        List<Event> availableEvents = eventsList.stream()
////                                .filter(event -> event.getTicketsAvailableToPurchase() > 0)
////                                .collect(Collectors.toList());
////
////                        LinkedHashMap<String, Function<Event, ?>> columnsEvent = new LinkedHashMap<>();
////                        columnsEvent.put("Event Id", Event::getEventId);
////                        columnsEvent.put("Event Name", Event::getEventId);
////                        columnsEvent.put("No of Tickets Available", Event::getTicketsAvailableToPurchase);
////                        columnsEvent.put("No of After Party Tickets Available", Event::getTotalAfterPartTicketsAvailableToPurchase);
////
////                        printTable(availableEvents, columnsEvent);
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
////                        columns.put("No of Tickets Available", TicketTier::getTicketsAvailableToPurchase);
////
////                        printTable(availableTiers, columns);
////
////                        break;
////                    case 3:
////                        promptBox("Buy Ticket");
////
////                        Ticket.Builder ticketBuilder = Ticket.newBuilder();
////                        System.out.println("Enter your Event Id:");
////                        int eventId = scanner.nextInt();
////                        scanner.nextLine();
////                        ticketBuilder.setEventId(eventId);
////
////                        System.out.println("Enter your Ticket Tier Id:");
////                        int ticketTierId = scanner.nextInt();
////                        scanner.nextLine();
////                        ticketBuilder.setTicketTierId(ticketTierId);
////
////                        boolean selectingAfters = true;
////                        while (selectingAfters) {
////                            System.out.println("Do you want to buy an After Party Ticket? (Yes/No)");
////                            String yesOrNo = scanner.next();
////                            if (yesOrNo.equalsIgnoreCase("yes")) {
////                                ticketBuilder.setAfterParty(true);
////                                selectingAfters = false;
////                            } else if (yesOrNo.equalsIgnoreCase("no")) {
////                                ticketBuilder.setAfterParty(false);
////                                selectingAfters = false;
////                            } else {
////                                System.out.println("Enter Yes or No");
////                            }
////                        }
////
////                        System.out.println("Select payment: ");
////                        boolean paying = true;
////                        while (paying) {
////                            System.out.println("1: Visa");
////                            System.out.println("2: Mastercard");
////                            int paymentSelection = scanner.nextInt();
////                            scanner.nextLine();
////                            switch (paymentSelection) {
////                                case 1:
////                                    System.out.println("Visa");
////                                    ticketBuilder.setPaymentType(PaymentType.VISA);
////                                    paying = false;
////                                    break;
////                                case 2:
////                                    System.out.println("Mastercard");
////                                    ticketBuilder.setPaymentType(PaymentType.MASTERCARD);
////                                    paying = false;
////                                    break;
////                                default:
////                                    System.out.println("Invalid selection");
////                                    break;
////                            }
////                        }
////
////                        Ticket ticket = ticketBuilder.build();
////
////                        BuyTicketRequest buyTicketRequest = BuyTicketRequest
////                                .newBuilder()
////                                .setTicket(ticket)
////                                .build();
////                        waitForConnection();
////
////                        BuyTicketRespond buyTicketRespond = buyTicketServiceBlockingStub.buyTicket(buyTicketRequest);
////                        if (buyTicketRespond.getSuccess()) {
////                            System.out.println("Ticket purchase successfully");
////                        } else {
////                            System.out.println("Ticket purchase failed");
////                            System.out.println(buyTicketRespond.getMessage());
////                        }
////
////                        break;
////                    case 4:
////                        promptBox("Quit");
////                        running = false;
////                        break;
////                    default:
////                        System.out.println("Invalid menu selection");
////                        break;
////                }
////            } catch (Exception e) {
////                System.out.println("Error: " + e.getMessage());
////            }
////        }
////    }
//}



package lk.ac.iit.ds.charindu.client;

import lk.ac.iit.ds.charindu.grpc.generated.*;

import java.io.IOException;
import java.util.List;
import java.util.Scanner;
import java.util.function.Function;
import java.util.stream.Collectors;
import static lk.ac.iit.ds.charindu.client.util.CLIShapes.*;

import lk.ac.iit.ds.charindu.grpc.generated.*;

import java.io.IOException;
import java.util.*;
import java.util.function.Function;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;

import static java.lang.System.currentTimeMillis;
////import static lk.ac.iit.ds.charindu.client.util.CLIShapes.*;
//
//public class CustomerClient extends Client {
//
//    private Scanner scanner = new Scanner(System.in);
//
//    public CustomerClient() throws IOException, InterruptedException {
//        super();
//    }
//
//    @Override
//    protected void processUserRequests() {
//        promptBox("Welcome to Customer Client");
//        boolean running = true;
//        while (running) {
//            try {
//                System.out.println("\nSelect Command: ");
//                numberMenu(new String[]{
//                        "Get Events",
//                        "Get Ticket Tiers",
//                        "Buy Ticket",
//                        "Quit"
//                });
//
//                int command = scanner.nextInt();
//                scanner.nextLine(); // Consume newline
//
//                switch (command) {
//                    case 1:
//                        getAvailableEvents();
//                        break;
//                    case 2:
//                        getEventTicketTiers();
//                        break;
//                    case 3:
//                        reserveTickets();
//                        break;
//                    case 4:
//                        System.out.println("Quitting...");
//                        running = false;
//                        break;
//                    default:
//                        alertBox("Invalid command", '!');
//                        break;
//                }
//            } catch (IOException | InterruptedException e) {
//                System.err.println("Error: " + e.getMessage());
//            } catch (InputMismatchException e) {
//                scanner.nextLine();
//                alertBox("Invalid input format", '!');
//            }
//        }
//    }
//
//    private void getAvailableEvents() throws IOException, InterruptedException {
//        promptBox("Available Events");
//
//        GetEventsRequest request = GetEventsRequest.newBuilder().build();
//        waitForConnection();
//
//        GetEventsResponse response = eventQueryServiceBlockingStub.getEvents(request);
//        List<Event> availableEvents = response.getEventsList().stream()
//                .filter(e -> e.getEventTicketsAvailable() > 0 || e.getAfterPartyTicketsAvailable() > 0)
//                .collect(Collectors.toList());
//
//        if (availableEvents.isEmpty()) {
//            alertBox("No events with available tickets", 'i');
//            return;
//        }
//
//        LinkedHashMap<String, Function<Event, ?>> columns = new LinkedHashMap<>();
//        columns.put("Event ID", Event::getId);
//        columns.put("Name", Event::getName);
//        columns.put("Date", Event::getDate);
//        columns.put("Available Tickets", Event::getEventTicketsAvailable);
//        columns.put("After Party Tickets", Event::getAfterPartyTicketsAvailable);
//
//        printTable(availableEvents, columns);
//    }
//
//    private void getEventTicketTiers() throws IOException, InterruptedException {
//        promptBox("Event Ticket Tiers");
//
//        System.out.println("Enter Event ID:");
//        String eventId = scanner.nextLine();
//
//        GetEventRequest request = GetEventRequest.newBuilder()
//                .setEventId(eventId)
//                .build();
//        waitForConnection();
//
//        Event event = eventQueryServiceBlockingStub.getEvent(request).getEvent();
//
//        List<TicketTier> availableTiers = event.getTicketTiersMap().values().stream()
//                .filter(t -> t.getTicketsAvailable() > 0)
//                .collect(Collectors.toList());
//
//        if (availableTiers.isEmpty()) {
//            alertBox("No available ticket tiers for this event", 'i');
//            return;
//        }
//
//        LinkedHashMap<String, Function<TicketTier, ?>> columns = new LinkedHashMap<>();
//        columns.put("Tier ID", TicketTier::getId);
//        columns.put("Price", TicketTier::getPrice);
//        columns.put("Available", TicketTier::getTicketsAvailable);
//
//        printTable(availableTiers, columns);
//    }
//
//    private void reserveTickets() throws IOException, InterruptedException {
//        promptBox("Reserve Tickets");
//
//        System.out.println("Enter Event ID:");
//        String eventId = scanner.nextLine();
//
//        System.out.println("Enter Ticket Tier ID:");
//        String tierId = scanner.nextLine();
//
//        System.out.println("Number of Tickets:");
//        int count = scanner.nextInt();
//        scanner.nextLine(); // Consume newline
//
//        System.out.println("Include After Party? (yes/no):");
//        boolean afterParty = scanner.nextLine().equalsIgnoreCase("yes");
//
//        System.out.println("Your Customer ID:");
//        String customerId = scanner.nextLine();
//
//        ReserveTicketsRequest request = ReserveTicketsRequest.newBuilder()
//                .setEventId(eventId)
//                .setTier(tierId)
//                .setCount(count)
//                .setAfterParty(afterParty)
//                .setCustomerId(customerId)
//                .build();
//        waitForConnection();
//
//        ReservationResponse response = eventCommandServiceBlockingStub.reserveTickets(request);
//
//        if (response.getSuccess()) {
//            System.out.println("Ticket purchase successfully");
//        } else {
//            System.out.println("Ticket purchase failed");
//        }
//    }
//}

public class CustomerClient extends Client {

    private Scanner scanner = new Scanner(System.in);

    public CustomerClient() throws IOException, InterruptedException {
        super();
    }

    @Override
    protected void processUserRequests() {
        promptBox("Welcome to Customer Client");
        boolean running = true;
        while (running) {
            try {
                System.out.println("\nSelect Command: ");
                numberMenu(new String[]{
                        "Get Events",
                        "Get Ticket Tiers",
                        "Buy Ticket",
                        "Quit"
                });

                int command = scanner.nextInt();
                scanner.nextLine(); // Consume newline

                switch (command) {
                    case 1:
                        getAvailableEvents();
                        break;
                    case 2:
                        getEventTicketTiers();
                        break;
                    case 3:
                        reserveTickets();
                        break;
                    case 4:
                        running = !askYesNo("Are you sure you want to quit?", scanner);
                        break;
                    default:
                        System.out.println("Invalid command!");
                        break;
                }
            } catch (IOException | InterruptedException e) {
                System.err.println("Error: " + e.getMessage());
            } catch (InputMismatchException e) {
                scanner.nextLine();
                System.out.println("Invalid input format!");
            }
        }
    }

    private void getAvailableEvents() throws IOException, InterruptedException {
        promptBox("Available Events");

        GetEventsRequest request = GetEventsRequest.newBuilder().build();
        waitForConnection();

        GetEventsResponse response = eventQueryServiceBlockingStub.getEvents(request);
        List<Event> availableEvents = response.getEventsList().stream()
                .filter(e -> e.getEventTicketsAvailable() > 0 || e.getAfterPartyTicketsAvailable() > 0)
                .collect(Collectors.toList());

        if (availableEvents.isEmpty()) {
            System.out.println("No events with available tickets");
            return;
        }

        LinkedHashMap<String, Function<Event, ?>> columns = new LinkedHashMap<>();
        columns.put("Event ID", Event::getId);
        columns.put("Name", Event::getName);
        columns.put("Date", Event::getDate);
        columns.put("Available Tickets", Event::getEventTicketsAvailable);
        columns.put("After Party Tickets", Event::getAfterPartyTicketsAvailable);

        printTable(availableEvents, columns);
    }

    private void getEventTicketTiers() throws IOException, InterruptedException {
        promptBox("Event Ticket Tiers");

        String eventId = getNonEmptyInput("Enter Event ID:", "Event ID cannot be empty");

        GetEventRequest request = GetEventRequest.newBuilder()
                .setEventId(eventId)
                .build();
        waitForConnection();

        Event event = eventQueryServiceBlockingStub.getEvent(request).getEvent();

        if (event.getId().isEmpty() || !event.getId().equals(eventId)) {
            System.out.println("Event not found");
            return;
        }

        List<TicketTier> availableTiers = event.getTicketTiersMap().values().stream()
                .filter(t -> t.getTicketsAvailable() > 0)
                .collect(Collectors.toList());

        if (availableTiers.isEmpty()) {
            System.out.println("No available ticket tiers for this event");
            return;
        }

        LinkedHashMap<String, Function<TicketTier, ?>> columns = new LinkedHashMap<>();
        columns.put("Tier ID", TicketTier::getId);
        columns.put("Price", TicketTier::getPrice);
        columns.put("Available", TicketTier::getTicketsAvailable);

        printTable(availableTiers, columns);
    }

//    private void reserveTickets() throws IOException, InterruptedException {
//        promptBox("Reserve Tickets");
//
//        String eventId = getNonEmptyInput("Enter Event ID:", "Event ID cannot be empty");
//
//        String tierId = getNonEmptyInput("Enter Ticket Tier ID:", "Tier ID cannot be empty");
//
//        boolean afterParty = askYesNo("Include After Party tickets?", scanner);
//
//        ReserveTicketsRequest request = ReserveTicketsRequest.newBuilder()
//                .setEventId(eventId)
//                .setTier(tierId)
//                .setAfterParty(afterParty)
//                .build();
//
//        waitForConnection();
//        ReservationResponse response = eventCommandServiceBlockingStub.reserveTickets(request);
//
//        if (response.getSuccess()) {
//            System.out.println("Reservation Successful!\nReservation ID: " + response.getReservationId());
//        } else {
//            System.out.println("Reservation Failed: " + response.getMessage());
//        }
//    }

//    private void reserveTickets() throws IOException, InterruptedException {
//        promptBox("Reserve Tickets");
//
//        // Get and verify event
//        String eventId = getNonEmptyInput("Enter Event ID:", "Event ID cannot be empty");
//        Event event = getAndVerifyEvent(eventId);
//        if (event == null) return;
//
//        // Get and verify ticket tiers
//        Map<String, TicketTier> ticketTiersMap = event.getTicketTiersMap();
//        if (ticketTiersMap.isEmpty()) {
//            alertBox("No ticket tiers available for this event", '!');
//            return;
//        }
//
//        // Select ticket tier
//        TicketTier selectedTier = selectTicketTier(ticketTiersMap);
//        if (selectedTier == null) return;
//
//
//        // Get other inputs
//        boolean afterParty = askYesNo("Include After Party tickets?", scanner);
//
//        // Build and send request
//        ReserveTicketsRequest request = ReserveTicketsRequest.newBuilder()
//                .setEventId(eventId)
//                .setTier(selectedTier.getId())
//                .setAfterParty(afterParty)
//                .build();
//
//        waitForConnection();
//        ReservationResponse response = eventCommandServiceBlockingStub.reserveTickets(request);
//
//        if (response.getSuccess()) {
//            System.out.println("Reservation Successfull!");
//        } else {
//            System.out.println("Reservation Failed: " + response.getMessage());
//        }
//    }

    private void reserveTickets() throws IOException, InterruptedException {
        System.out.println("\n=== Single Ticket Reservation ===");

        // Get and verify event
        String eventId = getNonEmptyInput("Enter Event ID:", "Event ID cannot be empty");
        Event event = getAndVerifyEvent(eventId);
        if (event == null) return;

        // Verify ticket tier availability
        Map<String, TicketTier> tiers = event.getTicketTiersMap();
        if (tiers.isEmpty()) {
            System.out.println("No available ticket tiers for this event.");
            return;
        }

        // Select valid ticket tier
        TicketTier selectedTier = selectTicketTier(tiers);
        if (selectedTier == null) return;

//        TicketTier selectedTier = tiers.get(tierId);
//        if (selectedTier.getTicketsAvailable() < 1) {
//            System.out.println("No tickets available in selected tier!");
//            return;
//        }

        // After-party ticket handling
        boolean wantsAfterParty = askYesNo("Include after-party ticket?", scanner);
        if (wantsAfterParty) {
            if (event.getAfterPartyTicketsAvailable() < 1) {
                System.out.println("No after-party tickets available!");
                boolean proceed = askYesNo("Would you like to reserve without after-party?", scanner);
                if (!proceed) {
                    System.out.println("Reservation cancelled.");
                    return;
                }
                wantsAfterParty = false;
            }
        }

        // Build and send request
        ReserveTicketsRequest request = ReserveTicketsRequest.newBuilder()
                .setEventId(eventId)
                .setTier(selectedTier.getId())
                .setAfterParty(wantsAfterParty)
                .build();

        ReservationResponse response = eventCommandServiceBlockingStub.reserveTickets(request);

        // Handle response
        if (response.getSuccess()) {
            System.out.println("Reservation successful!");
        } else {
            System.out.println("Reservation failed: " + response.getMessage());
        }
    }

    private Event getAndVerifyEvent(String eventId) throws IOException, InterruptedException {
        GetEventRequest request = GetEventRequest.newBuilder()
                .setEventId(eventId)
                .build();
        waitForConnection();

        GetEventResponse response = eventQueryServiceBlockingStub.getEvent(request);
        if (!response.hasEvent() || response.getEvent().getId().isEmpty()) {
            System.out.println("Event not found");
            return null;
        }
        return response.getEvent();
    }

    private TicketTier selectTicketTier(Map<String, TicketTier> ticketTiersMap) {
        List<TicketTier> availableTiers = ticketTiersMap.values().stream()
                .filter(t -> t.getTicketsAvailable() > 0)
                .collect(Collectors.toList());

        if (availableTiers.isEmpty()) {
            System.out.println("No available ticket tiers with inventory");
            return null;
        }

        System.out.println("Available Ticket Tiers:");
        LinkedHashMap<String, Function<TicketTier, ?>> columns = new LinkedHashMap<>();
        columns.put("#", t -> availableTiers.indexOf(t) + 1);
        columns.put("Tier ID", TicketTier::getId);
        columns.put("Price", TicketTier::getPrice);
        columns.put("Available", TicketTier::getTicketsAvailable);
        printTable(availableTiers, columns);

        int selection = getValidSelection("Select ticket tier:", 1, availableTiers.size());
        return availableTiers.get(selection - 1);
    }


    private int getValidSelection(String prompt, int min, int max) {
        while (true) {
            System.out.println(prompt);
            try {
                int selection = Integer.parseInt(scanner.nextLine().trim());
                if (selection >= min && selection <= max) {
                    return selection;
                }
                System.out.println("Please enter a number between " + min + " and " + max);
            } catch (NumberFormatException e) {
                System.out.println("Invalid number format");
            }
        }
    }

    private String getNonEmptyInput(String prompt, String errorMessage) {
        while (true) {
            System.out.println(prompt);
            String input = scanner.nextLine().trim();
            if (!input.isEmpty()) return input;
            System.out.println(errorMessage);
        }
    }

    private int getPositiveInteger(String prompt) {
        while (true) {
            System.out.println(prompt);
            try {
                int value = Integer.parseInt(scanner.nextLine().trim());
                if (value > 0) return value;
                System.out.println("Please enter a positive number");
            } catch (NumberFormatException e) {
                System.out.println("Invalid number format");
            }
        }
    }
}