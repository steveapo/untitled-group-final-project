import java.util.Scanner;
import java.util.ArrayList;
import java.util.List;

/** Application entry point — initialises data, seeds the admin on first run, then drives the top-level menu loop. */
public class Main {

    private static final String DOCS_URL = "https://untitled-group-self.vercel.app";

    public static void main(String[] args) throws Exception {
        Scanner scanner = new Scanner(System.in);
        Files file = new Files();
        file.errorLogging();

        List<Room>     rooms    = new ArrayList<>();
        List<Bookings> bookings = new ArrayList<>();
        List<Account>  users    = new ArrayList<>();

        // Auto-seed default accounts and data files on first run
        CLI.withSpinner("Initialising system", () -> {
            if (!file.adminExists()) {
                file.createUsersFile();
                SeedManager.seedDefaultAccounts(file);
            }
            if (!file.roomsFileExists()) {
                SeedManager.seedRooms(file);
            }
            if (!file.bookingsFileExists()) {
                SeedManager.seedBookings(file);
            }
        });

        file.getUsers(users);

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
            CLI.printMenuItem("4", "User Guide \u2197");
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
                case "4":
                    CLI.openUrl(DOCS_URL);
                    System.out.println(CLI.success("Opening User Guide in browser..."));
                    pause(scanner);
                    break;
                case "ESC":
                    CLI.clearScreen();
                    System.out.println(CLI.success("Goodbye! See you next time."));
                    return;
                default:
                    System.out.println(CLI.warning("[ERR_OPTION] Invalid option. Enter 1–4."));
                    pause(scanner);
            }
        }
    }

    /** Route the authenticated account to its role-specific menu. */
    private static void dispatch(Scanner scanner, Account account,
                                  List<Room> rooms, List<Bookings> bookings,
                                  List<Account> users, Files file) throws Exception {
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
