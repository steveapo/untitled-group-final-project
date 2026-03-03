import java.io.File;
import java.util.*;
import java.util.regex.Pattern;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Scanner;

public class Account {
    private String username;
    private String firstName;
    private String lastName;
    private String email;
    private byte[] hashed_password;
    private byte[] stored_salt;

    public Account() {}

    public Account(String username,String firstName, String lastName, String email, byte[] hashed_password,byte[] stored_salt) {
        this.username = username;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.hashed_password = hashed_password;
        this.stored_salt = stored_salt;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getUsername() {return username;}

    public String getFirstName() {
        return firstName;
    }

    public String getEmail() {
        return email;
    }

    public String getLastName() {
        return lastName;
    }

    public byte[] getHashed_password() {
        return hashed_password;
    }

    public byte[] getSalt() {
        return stored_salt;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setPassword(byte[] hashed_password) {
        this.hashed_password = hashed_password;
    }
    public void setSalt(byte[] stored_salt) {
        this.stored_salt = stored_salt;
    }


    //Registration for Accounts
    public void register(Vector <Account> users) throws Exception {
        Files file = new Files();

        Scanner scanner = new Scanner(System.in);

        String username,fname,lastname,email;

        username = checkUsername(users);

        int lineNumber;

        while (true) {
            //Verify names don't contain any numbers
            System.out.println("Enter your first name:");

             fname = scanner.nextLine().trim();
             //Do not allow number for names.
            if (fname.matches("[A-Za-z]+")) {
                break;
            } else {
                System.err.println("Names cannot have numbers");
                 lineNumber = Thread.currentThread().getStackTrace()[1].getLineNumber();
                file.writeErrors("Names cannot have numbers" + " - " + getClass() + " - Line: " + lineNumber);
            }

        }

        while (true){
            //Verify last_names do not contain any numbers
            System.out.println("Enter your last name: ");

             lastname = scanner.nextLine().trim();

            if(lastname.matches("[A-Za-z]+")){
                System.out.println("Last name added");
                break;
            }else{
                System.err.println("Surnames cannot have numbers");
                lineNumber = Thread.currentThread().getStackTrace()[1].getLineNumber();
                file.writeErrors("Surnames cannot have numbers" + " - " + getClass() + " - Line: " + lineNumber);

            }

        }

        while(true){
            //Check for email validity (must contain the '@' symbol)
            System.out.println("Enter your email: ");

             email = scanner.nextLine().trim();

            String regex = "^(?=.{1,64}@)[A-Za-z0-9_-]+(\\.[A-Za-z0-9_-]+)*@"
                    + "[^-][A-Za-z0-9-]+(\\.[A-Za-z0-9-]+)*(\\.[A-Za-z]{2,})$";

            if(Pattern.matches(regex,email)){
                System.out.println("Email has been added.");
                break;
            }else {
                System.err.println("Please check again your email.");
                lineNumber = Thread.currentThread().getStackTrace()[1].getLineNumber();
                file.writeErrors("Email has been added." + " - " + getClass() + " - Line: " + lineNumber);
            }

        }

        String[] hash_and_salt = hashPassword();
        String hashed_pass = hash_and_salt[0];
        String salt = hash_and_salt[1];
        System.out.println("Registration complete");
        System.out.println("Name: " + fname +"\nSurname: " + lastname + "\nEmail: " + email);
        file.CreateUsersFile();
        //set an update file function
        file.updateUsersFile(username,fname, lastname, email, hashed_pass,salt);
        //Update Database inside register.
        file.getUsers(users);
    }

    //Password Hashing function
    public String[] hashPassword() throws Exception{
        SecureRandom random = new SecureRandom();
        byte[] salt = new byte[16];
        random.nextBytes(salt);
        MessageDigest md = MessageDigest.getInstance("SHA-512");
        md.update(salt);
        System.out.println("Enter your password: ");
        Scanner scanner = new Scanner(System.in);
        String pass = scanner.nextLine();
        byte[] hashedpass = md.digest(pass.getBytes(StandardCharsets.UTF_8));
        String saltedpass = Base64.getEncoder().encodeToString(salt);
        String hashed_password = Base64.getEncoder().encodeToString(hashedpass);
        return new String[] {hashed_password, saltedpass};
    }

    //Check username availability
    public String checkUsername(Vector<Account> users){
        String username;
        Scanner scanner = new Scanner(System.in);

        while(true){

            boolean taken = false;
            System.out.println("Register - Enter your username: ");
            username = scanner.nextLine();
            for(Account user : users){

                if(user.getUsername().equalsIgnoreCase(username)){
                    taken = true;
                    break;
                }

            }

            if (taken) {
               System.err.println("Username already taken");
               Files file = new Files();
               int lineNumber = Thread.currentThread().getStackTrace()[1].getLineNumber();
               file.writeErrors("Username already taken" + " - " + getClass() + " Line: " + lineNumber);

            }else {
                break;
            }

        }

        return username;
    }


    //Login method
    public boolean login(Vector<Account> users) throws Exception {

        Scanner scanner = new Scanner(System.in);

        while (true) {

            int index = -1;

            System.out.println("Enter your username: ");
            String username = scanner.nextLine();

            for (Account user : users) {
                if (user.getUsername().equals(username)) {
                    index = users.indexOf(user);
                    break;
                }
            }

            if (index == -1) {
                System.out.println("Username does not exist");
                System.out.println("Try again");
            } else {

                System.out.println("Enter your password: ");
                String password = scanner.nextLine();

                byte[] salt = users.get(index).getSalt();

                MessageDigest md =
                        MessageDigest.getInstance("SHA-512");

                md.update(salt);

                byte[] hashedpass =
                        md.digest(password.getBytes(StandardCharsets.UTF_8));

                byte[] storedHash = users.get(index).getHashed_password();

                return MessageDigest.isEqual(hashedpass, storedHash);
            }
        }
    }

    public String toString(){
        return firstName + "," + lastName + "," + email + "," +
                Base64.getEncoder().encodeToString(hashed_password) + "," +
                Base64.getEncoder().encodeToString(stored_salt);
    }
}


