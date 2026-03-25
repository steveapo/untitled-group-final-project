import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Scanner;

public abstract class Menu {
    protected static int LoginStatus;
    protected String account;
    protected int selection;
    protected String input;
    protected Scanner scanner;
    protected String opMessage;
    protected ArrayList<String> options = new ArrayList<>();

    // Temporary user database, replace it with the real one
    public static final HashMap<String, String> database = new HashMap<>();

    public static void setLoginStatus(int loginStatus) {
        LoginStatus = loginStatus;
    }

    public void setAccount(String account) {
        this.account = account;
    }

    public void setInput(String input) {
        this.input = input;
    }

    public void setSelection(int selection) {
        this.selection = selection;
    }

    public String getAccount() {
        return account;
    }

    public String getInput() {
        return input;
    }

    public int getSelection() {
        return selection;
    }

    /*Different types of start method according to the input requested to the user : int, String, other
        a. For menus that return an option δηλ. αριθμό int 1,2,3, ... (Guest, Customer, Receptionist, Manager Menu)
        b. For menus that return a String (Login - the string is the user that logged in)
        c. Other menus require override of start method (Browse by date)

     */

    protected void launchMainMenu(){
        if (LoginStatus==1){
            new CustomerMenu();
        }
        else if (LoginStatus==2){
            new ReceptionistMenu();
        }
        else if (LoginStatus==3){
            new ManagerMenu();
        }
        else {
            new GuestMenu();
        }
    }

    protected String startMenu(String openingMessage){  //Launches menu, change message to fit the options
        System.out.println("LoginStatus: " + LoginStatus);
        String input;
        System.out.println(openingMessage);
        while(true) {
            scanner = new Scanner(System.in);
            input = scanner.nextLine();
            if (input.equals("e")) {
                System.out.println("Application Closed"); //Maybe change the closing message too
                return input;
            } else if (options.contains(input)) {
                System.out.println("You chose option: " + input);
                return input;
            }
            else {
                System.out.println("You must type " + options + " or e");
            }
        }
    }


    /*
    // Alternative method to startMenu. Selection must be int
    public void start() {
        int selection;
        do {
            printOptions();
            selection = getInput();
        } while (handleSelection(selection));
    }

    protected abstract void printOptions();
    protected abstract boolean handleSelection(int selection);

     */

    // REPLACE IT WITH REAL DATABASE
    public static void createDatabase(){
        database.put("u1", "p1");
        database.put("u2", "p2");
        database.put("u3", "p3");
    }
}
