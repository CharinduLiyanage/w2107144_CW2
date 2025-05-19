package lk.ac.iit.ds.charindu.client;

import java.io.IOException;

public class MainClass {
    public static void main(String[] args) {
        if (args.length != 1) {
            System.out.println("Usage CheckBalanceServiceClient <customer|clerk|coordinator|organizer>");
            System.exit(1);
        }
        String operation = args[0];

        Client client;
        try {
            switch (operation) {
                case "customer":
                    client = new CustomerClient();
                    client.start();
                    break;
                case "clerk":
                    client = new BoxOfficeClerksClient();
                    client.start();
                    break;
                case "coordinator":
                    client = new EventCoordinatorClient();
                    client.start();
                    break;
                case "organizer":
                    client = new EventOrganizerClient();
                    client.start();
                    break;
                default:
                    System.out.println("Invalid operation");
                    break;
            }
        } catch (IOException | InterruptedException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }
}
