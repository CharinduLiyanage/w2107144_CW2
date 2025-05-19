package lk.ac.iit.ds.charindu.client;

import java.io.IOException;
import static lk.ac.iit.ds.charindu.client.util.CLIShapes.*;
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

public class EventCoordinatorClient extends Client {

    private Scanner scanner = new Scanner(System.in);

    public EventCoordinatorClient() throws IOException, InterruptedException {
        super();
    }

    @Override
    protected void processUserRequests() {
        System.out.println("=== Event Coordinator Client ===");
        boolean running = true;
        while (running) {
            try {
                System.out.println("\nAvailable Commands:");
                numberMenu(new String[]{
                        "Get Available Events",
                        "Get Event Ticket Tiers",
                        "Bulk Reserve Tickets",
                        "Quit"
                });

                int command = getValidSelection("Enter command number:", 1, 4);

                switch (command) {
                    case 1:
                        getAvailableEvents();
                        break;
                    case 2:
                        getEventTicketTiers();
                        break;
                    case 3:
                        bulkReserveTickets();
                        break;
                    case 4:
                        running = !askYesNo("Are you sure you want to quit? (Y/N)", scanner);
                        break;
                }
            } catch (IOException | InterruptedException e) {
                System.out.println("Error: " + e.getMessage());
            }
        }
    }

    private void getAvailableEvents() throws IOException, InterruptedException {
        GetEventsResponse response = eventQueryServiceBlockingStub.getEvents(
                GetEventsRequest.newBuilder().build()
        );

        List<Event> availableEvents = response.getEventsList().stream()
                .filter(e -> e.getEventTicketsAvailable() > 0)
                .collect(Collectors.toList());

        if (availableEvents.isEmpty()) {
            System.out.println("No events with available tickets found.");
            return;
        }

        LinkedHashMap<String, Function<Event, ?>> columns = new LinkedHashMap<>();
        columns.put("ID", Event::getId);
        columns.put("Name", Event::getName);
        columns.put("Date", Event::getDate);
        columns.put("Available", Event::getEventTicketsAvailable);
        columns.put("After Party", Event::getAfterPartyTicketsAvailable);

        System.out.println("\nAvailable Events:");
        printTable(availableEvents, columns);
    }

    private void getEventTicketTiers() throws IOException, InterruptedException {
        String eventId = getNonEmptyInput("Enter Event ID:");
        Event event = getAndVerifyEvent(eventId);
        if (event == null) return;

        Map<String, TicketTier> tiers = event.getTicketTiersMap();
        if (tiers.isEmpty()) {
            System.out.println("No ticket tiers found for this event.");
            return;
        }

        LinkedHashMap<String, Function<TicketTier, ?>> columns = new LinkedHashMap<>();
        columns.put("Tier ID", TicketTier::getId);
        columns.put("Price", TicketTier::getPrice);
        columns.put("Total", TicketTier::getTicketsTotal);
        columns.put("Sold", TicketTier::getTicketsSold);
        columns.put("Available", TicketTier::getTicketsAvailable);

        System.out.println("\nTicket Tiers for " + event.getName() + ":");
        printTable(new ArrayList<>(tiers.values()), columns);
    }

//    private void bulkReserveTickets() throws IOException, InterruptedException {
//        System.out.println("\n=== Bulk Ticket Reservation ===");
//
//        // Get and validate event
//        String eventId = getNonEmptyInput("Enter Event ID:");
//        Event event = getAndVerifyEvent(eventId);
//        if (event == null) return;
//
//        // Select ticket tier
//        Map<String, TicketTier> tiers = event.getTicketTiersMap();
//        String tierId = selectTicketTier(tiers);
//        if (tierId == null) return;
//
//        // Validate ticket count
//        int maxTickets = tiers.get(tierId).getTicketsAvailable();
//        int count = getPositiveInteger("Enter number of tickets to reserve (Max " + maxTickets + "):", maxTickets);
//
//        boolean afterParty = askYesNo("Include after-party tickets? (Y/N)", scanner);
//
//        BulkReserveRequest request = BulkReserveRequest.newBuilder()
//                .setEventId(eventId)
//                .setTier(tierId)
//                .setCount(count)
//                .setAfterParty(afterParty)
//                .build();
//
//        ReservationResponse response = eventCommandServiceBlockingStub.bulkReserve(request);
//
//        System.out.println(response.getSuccess()
//                ? "Successfully reserved " + count
//                : "Reservation failed: " + response.getMessage());
//    }

    private void bulkReserveTickets() throws IOException, InterruptedException {
        System.out.println("\n=== Bulk Ticket Reservation ===");

        // Get and validate event
        String eventId = getNonEmptyInput("Enter Event ID:");
        Event event = getAndVerifyEvent(eventId);
        if (event == null) return;

        // Select ticket tier
        Map<String, TicketTier> tiers = event.getTicketTiersMap();
        String tierId = selectTicketTier(tiers);
        if (tierId == null) return;

        // Initial ticket count validation
        int maxRegular = tiers.get(tierId).getTicketsAvailable();
        int count = getPositiveInteger("Enter number of tickets to reserve (Max " + maxRegular + "):", maxRegular);

        // After-party ticket handling
        boolean wantsAfterParty = askYesNo("Include after-party tickets?", scanner);
        int maxAfterParty = event.getAfterPartyTicketsAvailable();

        if (wantsAfterParty) {
            if (maxAfterParty <= 0) {
                System.out.println("No after-party tickets available!");
                wantsAfterParty = false;
            } else {
                while (true) {
                    if (count > maxAfterParty) {
                        System.out.println("Only " + maxAfterParty + " after-party tickets available.");
                        System.out.println("Your current reservation: " + count + " tickets");

                        if (!askYesNo("Would you like to adjust your reservation quantity?", scanner)) {
                            wantsAfterParty = false;
                            break;
                        }

                        int newMax = Math.min(maxRegular, maxAfterParty);
                        count = getPositiveInteger("Enter new quantity (Max " + newMax + "):", newMax);

                        // Re-check after-party availability
                        maxAfterParty = event.getAfterPartyTicketsAvailable();
                        if (count <= maxAfterParty) break;
                    } else {
                        break;
                    }
                }
            }
        }

        // Final confirmation
        System.out.println("\nReservation Summary:");
        System.out.println("Regular Tickets: " + count);
        if (wantsAfterParty) {
            System.out.println("After-Party Tickets: " + count);
        }

        if (!askYesNo("Confirm reservation? (Y/N)", scanner)) {
            System.out.println("Reservation cancelled.");
            return;
        }

        // Build and send request
        BulkReserveRequest request = BulkReserveRequest.newBuilder()
                .setEventId(eventId)
                .setTier(tierId)
                .setCount(count)
                .setAfterParty(wantsAfterParty)
//                .setGroupId(getNonEmptyInput("Enter group/organization ID:"))
                .build();

        ReservationResponse response = eventCommandServiceBlockingStub.bulkReserve(request);

        System.out.println(response.getSuccess()
                ? "Successfully reserved " + count + " tickets" + (wantsAfterParty ? " with after-party access" : "")
                : "Reservation failed: " + response.getMessage());
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
        if (tierIds.isEmpty()) {
            System.out.println("No ticket tiers available!");
            return null;
        }

        System.out.println("Available Ticket Tiers:");
        for (int i = 0; i < tierIds.size(); i++) {
            System.out.printf("%d. %s%n", i+1, tierIds.get(i));
        }

        int selection = getValidSelection("Select ticket tier:", 1, tierIds.size());
        return tierIds.get(selection-1);
    }

    private String getNonEmptyInput(String prompt) {
        while (true) {
            System.out.print(prompt + " ");
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
                System.out.print(prompt + " ");
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
                System.out.print(prompt + " ");
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
//            System.out.print(prompt + " ");
//            String input = scanner.nextLine().trim().toUpperCase();
//            if (input.equals("Y")) return true;
//            if (input.equals("N")) return false;
//            System.out.println("Please enter Y or N!");
//        }
//    }
}

//public class EventCoordinatorClient extends Client {
//
////    private GetEventServiceGrpc.GetEventServiceBlockingStub getEventServiceBlockingStub;
////    private GetTicketTierServiceGrpc.GetTicketTierServiceBlockingStub getTicketTierServiceBlockingStub;
////    private BuyTicketServiceGrpc.BuyTicketServiceBlockingStub buyTicketServiceBlockingStub;
//
//
//    public EventCoordinatorClient() throws IOException, InterruptedException {
//        super();
//    }
//
//    @Override
//    protected void processUserRequests() {
//
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
////        while (running) {
////            try {
////                promptBox("Welcome to the Event Coordinator Client");
////                numberMenu(new String[]{"Get Events", "Get Ticket Tiers", "Reserve Tickets", "Quit"});
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
////                        // filter for only those with ticketsAvailableToPurchase > 0
////                        List<Event> availableEvents = eventsList.stream()
////                                .filter(event -> event.getTicketsAvailableToPurchase() > 0)
////                                .collect(Collectors.toList());
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
////                        columns.put("Total Tickets ", TicketTier::getTotalTickets);
////                        columns.put("Tickets Sold", TicketTier::getTicketsSold);
////                        columns.put("Tickets Available", TicketTier::getTicketsAvailableToPurchase);
////
////                        printTable(availableTiers, columns);
////                        break;
////                    case 3:
////                        promptBox("Reserve Tickets");
////
////                        System.out.println("Buy Ticket");
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
////                        Ticket ticket = ticketBuilder.build();
////
////                        System.out.println("Enter the quantity to reserve: ");
////                        int quantity = scanner.nextInt();
////                        scanner.nextLine();
////
////                        BuyTicketsRequest buyTicketsRequest = BuyTicketsRequest
////                                .newBuilder()
////                                .setTicket(ticket)
////                                .setQuantity(quantity)
////                                .build();
////                        waitForConnection();
////
////                        BuyTicketsRespond buyTicketsRespond = buyTicketServiceBlockingStub.buyTickets(buyTicketsRequest);
////                        if (buyTicketsRespond.getSuccess()) {
////                            System.out.println("Ticket(s) Reserved");
////                        } else {
////                            System.out.println("Ticket(s) Not Reserved");
////                            System.out.println(buyTicketsRespond.getMessage());
////                        }
////
////                        break;
////                    case 4:
////                        promptBox("Quit");
////                        running = false;
////                        break;
////                    default:
////                        alertBox("Please select a valid option", '!');
////                        break;
////                }
////            } catch (RuntimeException | IOException | InterruptedException e) {
////                System.out.println("Error: " + e.getMessage());
////            }
////        }
////    }
//}
