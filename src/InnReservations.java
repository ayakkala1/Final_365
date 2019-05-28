import java.util.Scanner;
import java.sql.*;
import java.math.*;

public class InnReservations {
    Connection conn;
    PreparedStatement reserveRoom;

    public InnReservations() {
        String URL = "jdbc:mysql://db.labthreesixfive.com/nstapp?autoReconnect=true";
        String USER = "nstapp";
        String PASS = "S19_CSC-365-014333757";
        try {
            conn = DriverManager.getConnection(URL, USER, PASS);
            System.out.println("Connected!");
            run();
        } catch (SQLException error) {
            System.out.println("Not connected!");
        }
    }

    private void displayOptions() {
        System.out.println("Options: ");
        System.out.println("\tRooms and Rates");
        System.out.println("\tReserve a room");
        System.out.println("\tChange a reservation");
        System.out.println("\tCancel a reservation");
        System.out.println("\tReservation detail");
        System.out.println("\tRevenue");
    }

    private void executeInput(String userInput) {
        if (userInput.equals("Reserve a room")) {
            reserveARoom();
        }
    }

    private void reserveARoom() {
        Scanner reader = new Scanner(System.in); // Reading from System.in
        System.out.print("First name: ");
        String firstName = reader.nextLine(); // Scans the next token of the input as an int.
        System.out.print("Last name: ");
        String lastName = reader.nextLine(); // Scans the next token of the input as an int.
        System.out.print("Room code: ");
        String roomCode = reader.nextLine(); // Scans the next token of the input as an int.
        System.out.print("Bed type: ");
        String bedType = reader.nextLine(); // Scans the next token of the input as an int.
        System.out.print("Check in: ");
        String checkIn = reader.nextLine(); // Scans the next token of the input as an int.
        System.out.print("Check out: ");
        String checkOut = reader.nextLine(); // Scans the next token of the input as an int.
        System.out.print("Number of children: ");
        Integer numChildren = reader.nextInt(); // Scans the next token of the input as an int.
        System.out.print("Number of adults: ");
        Integer numAdults = reader.nextInt(); // Scans the next token of the input as an int.
        reader.close();

        System.out.println("You entered: ");
        System.out.println(firstName);
        System.out.println(lastName);
        System.out.println(roomCode);
        System.out.println(bedType);
        System.out.println(checkIn);
        System.out.println(checkOut);
        System.out.println(numChildren);
        System.out.println(numAdults);

    }

    public static void main(String args[]) {
        InnReservations InnReserver = new InnReservations();
    }

    private void run() {
        displayOptions();
        Scanner reader = new Scanner(System.in); // Reading from System.in
        System.out.print("Enter an option: ");
        String userInput = reader.nextLine(); // Scans the next token of the input as an int.
        executeInput(userInput);
        reader.close();
    }

}
