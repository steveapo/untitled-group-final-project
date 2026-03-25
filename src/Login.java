// SUBMENU 1

import java.util.HashMap;
import java.util.Scanner;

public class Login extends Menu{
    private String userEmail = "";
    private String password = "";
    private String user;
    Login(HashMap<String, String> users) {
        Scanner scanner = new Scanner(System.in);
        while(true) {
            System.out.println("Enter your Email, or enter 'exit' to exit to menu: " );
            String email;
            email = scanner.nextLine();
            if (email.equals("exit")){
                System.out.println("Exited to Main Menu");
                break;
            }
            else if (email == null || email.isEmpty()) {
                System.out.println("You have not entered any text");
            } else if (!users.containsKey(email)) {
                System.out.println("You have entered " + email);
                System.out.println("Email doesn't exists");
            } else {
                System.out.println("Email Confirmed");
                System.out.println("Enter Password: ");
                String password = scanner.nextLine();
                if (password == null || password.isEmpty()) {
                    System.out.println("You have not entered any text");
                } else if (!users.get(email).equals(password)) {
                    System.out.println("Wrong password");
                } else {
                    user = database.get(email);
                    System.out.println("Password Confirmed");
                    System.out.println(email + " You are logged in");
                    Menu.setLoginStatus(1);
                    break;
                }
            }
        }
        launchMainMenu();
    }

}
