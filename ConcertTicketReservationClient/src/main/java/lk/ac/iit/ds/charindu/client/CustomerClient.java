package lk.ac.iit.ds.charindu.client;

import java.io.IOException;

public class CustomerClient extends Client {

//    GetEventServiceGrpc.GetEventServiceBlockingStub getEventServiceBlockingStub;
//    GetTicketTierServiceGrpc.GetTicketTierServiceBlockingStub getTicketTierServiceBlockingStub;
//    BuyTicketServiceGrpc.BuyTicketServiceBlockingStub buyTicketServiceBlockingStub;

    public CustomerClient() throws IOException, InterruptedException {
        super();
    }

    @Override
    protected void processUserRequests() {

    }

    //    @Override
//    protected void initializeConnection() {
//        super.initializeConnection();
//        getEventServiceBlockingStub = GetEventServiceGrpc.newBlockingStub(channel);
//        getTicketTierServiceBlockingStub = GetTicketTierServiceGrpc.newBlockingStub(channel);
//        buyTicketServiceBlockingStub = BuyTicketServiceGrpc.newBlockingStub(channel);
//        channel.getState(true);
//    }
//
//    @Override
//    protected void processUserRequests() {
//        Scanner scanner = new Scanner(System.in);
//        boolean running = true;
//
//        while (running) {
//            try {
//                promptBox("Welcome to the Customer Client");
//                numberMenu(new String[]{
//                        "Get Events",
//                        "Get Ticket Tiers",
//                        "Buy Ticket",
//                        "Quit"
//                });
//                int mainMenuSelection = scanner.nextInt();
//                scanner.nextLine();
//
//                switch (mainMenuSelection) {
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
//                        // filter for only those with ticketsAvailableToPurchase > 0
//                        List<Event> availableEvents = eventsList.stream()
//                                .filter(event -> event.getTicketsAvailableToPurchase() > 0)
//                                .collect(Collectors.toList());
//
//                        LinkedHashMap<String, Function<Event, ?>> columnsEvent = new LinkedHashMap<>();
//                        columnsEvent.put("Event Id", Event::getEventId);
//                        columnsEvent.put("Event Name", Event::getEventId);
//                        columnsEvent.put("No of Tickets Available", Event::getTicketsAvailableToPurchase);
//                        columnsEvent.put("No of After Party Tickets Available", Event::getTotalAfterPartTicketsAvailableToPurchase);
//
//                        printTable(availableEvents, columnsEvent);
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
//
//                        break;
//                    case 3:
//                        promptBox("Buy Ticket");
//
//                        Ticket.Builder ticketBuilder = Ticket.newBuilder();
//                        System.out.println("Enter your Event Id:");
//                        int eventId = scanner.nextInt();
//                        scanner.nextLine();
//                        ticketBuilder.setEventId(eventId);
//
//                        System.out.println("Enter your Ticket Tier Id:");
//                        int ticketTierId = scanner.nextInt();
//                        scanner.nextLine();
//                        ticketBuilder.setTicketTierId(ticketTierId);
//
//                        boolean selectingAfters = true;
//                        while (selectingAfters) {
//                            System.out.println("Do you want to buy an After Party Ticket? (Yes/No)");
//                            String yesOrNo = scanner.next();
//                            if (yesOrNo.equalsIgnoreCase("yes")) {
//                                ticketBuilder.setAfterParty(true);
//                                selectingAfters = false;
//                            } else if (yesOrNo.equalsIgnoreCase("no")) {
//                                ticketBuilder.setAfterParty(false);
//                                selectingAfters = false;
//                            } else {
//                                System.out.println("Enter Yes or No");
//                            }
//                        }
//
//                        System.out.println("Select payment: ");
//                        boolean paying = true;
//                        while (paying) {
//                            System.out.println("1: Visa");
//                            System.out.println("2: Mastercard");
//                            int paymentSelection = scanner.nextInt();
//                            scanner.nextLine();
//                            switch (paymentSelection) {
//                                case 1:
//                                    System.out.println("Visa");
//                                    ticketBuilder.setPaymentType(PaymentType.VISA);
//                                    paying = false;
//                                    break;
//                                case 2:
//                                    System.out.println("Mastercard");
//                                    ticketBuilder.setPaymentType(PaymentType.MASTERCARD);
//                                    paying = false;
//                                    break;
//                                default:
//                                    System.out.println("Invalid selection");
//                                    break;
//                            }
//                        }
//
//                        Ticket ticket = ticketBuilder.build();
//
//                        BuyTicketRequest buyTicketRequest = BuyTicketRequest
//                                .newBuilder()
//                                .setTicket(ticket)
//                                .build();
//                        waitForConnection();
//
//                        BuyTicketRespond buyTicketRespond = buyTicketServiceBlockingStub.buyTicket(buyTicketRequest);
//                        if (buyTicketRespond.getSuccess()) {
//                            System.out.println("Ticket purchase successfully");
//                        } else {
//                            System.out.println("Ticket purchase failed");
//                            System.out.println(buyTicketRespond.getMessage());
//                        }
//
//                        break;
//                    case 4:
//                        promptBox("Quit");
//                        running = false;
//                        break;
//                    default:
//                        System.out.println("Invalid menu selection");
//                        break;
//                }
//            } catch (Exception e) {
//                System.out.println("Error: " + e.getMessage());
//            }
//        }
//    }
}
