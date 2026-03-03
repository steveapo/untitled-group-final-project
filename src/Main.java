import java.io.File;
import java.io.FileNotFoundException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.ResolverStyle;
import java.util.Scanner;
import java.util.Vector;


public class Main {
    Vector<Bookings> bookings = new Vector<>();
    Vector<Room> roomVector = new Vector<>();
    Vector<Room> available_rooms = new Vector<>();
    Vector<Account> userVector = new Vector<>();

    public static void main(String[] args) throws Exception {
        int LineNumber;
        Scanner scanner = new Scanner(System.in);
        Main main = new Main();
        Account account = new Account();
        Files file = new Files();
        file.errorLogging();
        file.getUsers(main.userVector);
        //account.register(main.userVector);
        if (account.login(main.userVector)) {
            System.out.println("Welcome to Booking System");
            if (file.checkFile()) {
                file.populateRooms(main.roomVector);
                file.populatebookings(main.roomVector, main.bookings);
                DateInput date = new DateInput();
                LocalDate user_checkIn = date.checkInDate();
                LocalDate user_checkOut = date.checkOutDate();
                while (user_checkOut.isBefore(user_checkIn)) {
                    System.out.println("Your check-out date is prior to the check-in.\nPlease enter a check-out date that is after the check-in");
                    user_checkOut = date.checkOutDate();
                }
                int index = 0;
                date.checkBookingsDate(user_checkIn, user_checkOut, main.available_rooms, main.roomVector);
                for (Room availableRooms : main.available_rooms) {
                    System.out.println(index + 1 + ". " + availableRooms);
                    index += 1;
                }
                System.out.println("Choose the room you want from 1 - " + index + " : ");
                int choice = scanner.nextInt();
                while (choice < 1 || choice > index) {
                    System.err.println("Invalid option.\nEnter a room from 0-" + index + " :");
                    choice = scanner.nextInt();
                }
                System.out.println("Do you want to book this room? (yes or no):  ");
                scanner.nextLine();
                String confirm = scanner.nextLine();
                while (!confirm.equalsIgnoreCase("yes") && !confirm.equalsIgnoreCase("no")) {
                    System.err.println("Invalid option");
                    file.writeErrors("Invalid option" + main.getClass());
                    System.out.print("Enter either (yes) to confirm booking or (no) to cancel: ");
                    confirm = scanner.nextLine();
                }
                if (confirm.equalsIgnoreCase("yes")) {
                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-uuuu").withResolverStyle(ResolverStyle.STRICT);
                    for (Room rooms : main.roomVector) {
                        if (main.available_rooms.get(choice - 1).getRoom_no().equals(rooms.getRoom_no())) {
                            Bookings bookings1 = new Bookings(rooms, user_checkIn.format(formatter).toString(), user_checkOut.format(formatter).toString());
                            main.bookings.add(bookings1);
                            file.updateBookings(main.bookings);
                            main.available_rooms.remove(choice - 1);
                            break;
                        }
                    }
                } else {
                    System.out.println("Bookings has been cancelled");
                }
            }
        } else {
            System.err.println("Wrong password");
             LineNumber = Thread.currentThread().getStackTrace()[1].getLineNumber();
            file.writeErrors("Wrong password" +" - "+ main.getClass() + " - Line:" + LineNumber);
        }
    }
    }
