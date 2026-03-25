// MAIN MENU 0 = DEFAULT MAIN MENU

import java.util.ArrayList;
import java.util.Scanner;

public class GuestMenu extends Menu {
    GuestMenu(){
        opMessage = "Welcome! Type 1 2 or 3 to select an option or 'e' to exit the application:\n1.Login\n2.Register as User\n3.BrowseRooms";
        options.add("1");
        options.add("2");
        options.add("3");
        input = startMenu(opMessage);
        handleInput();
    }

    private void handleInput() {
        if (input.equals("1")) {
            new Login(database);
        } else if (input.equals("2")) {
            new Register(database);
        } else if (input.equals("3")) {
            new BrowseRooms();
        }
    }
}
