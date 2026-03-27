import java.util.Scanner;
import java.util.Vector;

public class GuestMenu {

    public static Account show(Scanner scanner, Vector<Room> rooms, Vector<Account> users)
            throws Exception {
        while (true) {
            CLI.clearScreen();
            CLI.printBanner("GUEST MENU");
            System.out.println();
            CLI.printMenuItem("1", "View available rooms");
            CLI.printMenuItem("2", "Login");
            CLI.printMenuItem("3", "Register");
            CLI.printMenuItem("4", "Back to main menu");
            CLI.printDivider();
            System.out.print(CLI.prompt("Choice: "));
            String choice = scanner.nextLine().trim();

            switch (choice) {
                case "1":
                    viewRooms(rooms);
                    Main.pause(scanner);
                    break;
                case "2":
                    Account logged = new Account().login(users, scanner);
                    if (logged != null) {
                        CLI.randomSpinner("Logging in");
                        return logged;
                    }
                    break;
                case "3":
                    if (new Account().register(users, scanner)) {
                        CLI.randomSpinner("Creating account");
                    }
                    Main.pause(scanner);
                    break;
                case "4":
                    return null;
                default:
                    System.out.println(CLI.warning("Invalid option. Enter 1–4."));
                    Main.pause(scanner);
            }
        }
    }

    private static void viewRooms(Vector<Room> rooms) {
        CLI.randomSpinner("Loading rooms");
        CLI.clearScreen();
        CLI.printBanner("AVAILABLE ROOMS");
        System.out.println();
        boolean found = false;
        for (Room r : rooms) {
            if (r.getStatus().equals("AVAILABLE")) {
                System.out.printf("  %s  %-6s | %-8s | %s/night%n",
                        CLI.green("●"),
                        CLI.bold(r.getRoomNumber()),
                        r.getType(),
                        CLI.yellow(String.format("$%.2f", r.getPrice())));
                found = true;
            }
        }
        if (!found) System.out.println(CLI.dim("  No rooms currently available."));
        CLI.printDivider();
    }
}
