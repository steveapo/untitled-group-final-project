import java.util.Scanner;
import java.util.Vector;

public class Main {

    public static void main(String[] args) throws Exception {
        Scanner scanner = new Scanner(System.in);
        Files file = new Files();
        file.errorLogging();

        Vector<Room>     rooms    = new Vector<>();
        Vector<Bookings> bookings = new Vector<>();
        Vector<Account>  users    = new Vector<>();

        // ── Auto-seed admin account on first run ─────────────────────────
        CLI.withSpinner("Initialising system", () -> {
            if (!file.adminExists()) {
                file.createUsersFile();
                SeedManager.seedAdmin(file);
            }
        });

        file.getUsers(users);

        if (!file.checkFile()) {
            System.err.println(CLI.error("Critical data files (Rooms or Bookings) are missing. Exiting."));
            return;
        }

        CLI.withSpinner("Loading rooms and bookings", () -> {
            file.populateRooms(rooms);
            file.populatebookings(rooms, bookings);
        });

        // ── Main loop ────────────────────────────────────────────────────
        while (true) {
            CLI.clearScreen();
            CLI.printBanner("HOTEL ROOM BOOKING SYSTEM");
            System.out.println();
            CLI.printMenuItem("1", "Continue as Guest");
            CLI.printMenuItem("2", "Login");
            CLI.printMenuItem("3", "Register");
            CLI.printMenuItem("4", "Exit");
            CLI.printDivider();
            System.out.print(CLI.prompt("Choice: "));
            String choice = scanner.nextLine().trim();

            switch (choice) {
                case "1":
                    Account fromGuest = GuestMenu.show(scanner, rooms, users);
                    if (fromGuest != null) {
                        dispatch(scanner, fromGuest, rooms, bookings, users, file);
                    }
                    break;
                case "2":
                    Account logged = new Account().login(users, scanner);
                    if (logged != null) {
                        dispatch(scanner, logged, rooms, bookings, users, file);
                    }
                    break;
                case "3":
                    new Account().register(users, scanner);
                    break;
                case "4":
                    CLI.clearScreen();
                    System.out.println(CLI.success("Goodbye! See you next time."));
                    return;
                default:
                    System.out.println(CLI.warning("Invalid option. Enter 1–4."));
                    pause(scanner);
            }
        }
    }

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
                System.err.println(CLI.error("Unknown role '" + account.getRole() + "'. Contact admin."));
                file.writeErrors("Unknown role: " + account.getRole() + " - user: " + account.getUsername());
        }
    }

    // Press Enter to continue (used after error messages before clearing screen)
    static void pause(Scanner scanner) {
        System.out.print(CLI.dim("Press Enter to continue..."));
        scanner.nextLine();
    }
}
