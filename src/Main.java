import java.util.Scanner;
import java.util.Vector;

/** Application entry point — initialises data, seeds the admin on first run, then drives the top-level menu loop. */
public class Main {

    public static void main(String[] args) throws Exception {
        Scanner scanner = new Scanner(System.in);
        Files file = new Files();
        file.errorLogging();

        Vector<Room>     rooms    = new Vector<>();
        Vector<Bookings> bookings = new Vector<>();
        Vector<Account>  users    = new Vector<>();

        // Auto-seed admin account on first run
        CLI.withSpinner("Initialising system", () -> {
            if (!file.adminExists()) {
                file.createUsersFile();
                SeedManager.seedAdmin(file);
            }
        });

        file.getUsers(users);

        if (!file.checkFile()) {
            System.out.println(CLI.error("Critical data files (Rooms or Bookings) are missing. Exiting."));
            return;
        }

        CLI.withSpinner("Loading rooms and bookings", () -> {
            file.populateRooms(rooms);
            file.populateBookings(rooms, bookings);
        });

        // Main menu loop
        while (true) {
            CLI.clearScreen();
            CLI.printBanner("HOTEL ROOM BOOKING SYSTEM");
            System.out.println();
            CLI.printMenuItem("1", "Continue as Guest");
            CLI.printMenuItem("2", "Login");
            CLI.printMenuItem("3", "Register");
            CLI.printFooter("Exit");
            String choice = CLI.readChoice(scanner);

            switch (choice) {
                case "1":
                    Account guestAccount = GuestMenu.show(scanner, rooms, users);
                    if (guestAccount != null) {
                        dispatch(scanner, guestAccount, rooms, bookings, users, file);
                    }
                    break;
                case "2":
                    Account loggedAccount = new Account().login(users, scanner);
                    if (loggedAccount != null) {
                        CLI.randomSpinner("Logging in");
                        dispatch(scanner, loggedAccount, rooms, bookings, users, file);
                    }
                    break;
                case "3":
                    if (new Account().register(users, scanner)) {
                        CLI.randomSpinner("Creating account");
                    }
                    break;
                case "ESC":
                    CLI.clearScreen();
                    System.out.println(CLI.success("Goodbye! See you next time."));
                    return;
                default:
                    System.out.println(CLI.warning("Invalid option. Enter 1–3."));
                    pause(scanner);
            }
        }
    }

    /** Route the authenticated account to its role-specific menu. */
    private static void dispatch(Scanner scanner, Account account,
                                  Vector<Room> rooms, Vector<Bookings> bookings,
                                  Vector<Account> users, Files file) throws Exception {
        switch (account.getRole()) {
            case "USER":
                UserMenu.show(scanner, account, rooms, bookings, file);
                break;
            case "RECEPTION":
                ReceptionMenu.show(scanner, account, rooms, bookings, users, file);
                break;
            case "MANAGER":
                ManagerMenu.show(scanner, account, rooms, bookings, users, file);
                break;
            default:
                System.out.println(CLI.error("Unknown role '" + account.getRole() + "'. Contact admin."));
                file.writeErrors("Unknown role: " + account.getRole() + " - user: " + account.getUsername());
        }
    }

    /** Press Enter to continue — used after error messages before clearing the screen. */
    static void pause(Scanner scanner) {
        CLI.waitForKey(scanner);
    }
}
