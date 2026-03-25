// SUBMENU 2

import java.util.HashMap;
import java.util.Scanner;

public class Register extends Menu{
    private String userEmail = "";
    private String password = "";
    private String user;
    Register(HashMap<String, String> users){
        Scanner scanner = new Scanner(System.in);
        while(true) {
            System.out.println("Enter your Email, or enter 'e' to exit to menu: " );
            String email;
            email = scanner.nextLine();
            if (email.equals("e")){
                System.out.println("Exited to Main Menu");
                break;
            }
            else if (email == null || email.isEmpty()) {
                System.out.println("You have not entered any text");
            }
            else if (!email.matches("^[\\w.-]+@[\\w.-]+\\.[a-zA-Z]{2,}$")){
                System.out.println("Wrong format");
            }
            else if (users.containsKey(email)) {
                System.out.println("You have entered " + email);
                System.out.println("Email already exists");
            } else {
                System.out.println("Email Confirmed");
                System.out.println("Enter Password: ");
                String password = scanner.nextLine();
                if (password == null || password.isEmpty()) {
                    System.out.println("You have not entered any text");
                }
                else {
                    database.put(email,password);
                    user = email;
                    System.out.println("Account Registered");
                    System.out.println(user + " You are logged in");
                    Menu.setLoginStatus(1);
                    break;
                }
            }
        }
        System.out.println("New database: "  + database);
        new GuestMenu();
    }
}
