import java.util.ArrayList;
import java.util.Scanner;
import java.util.List;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.sql.*;
import java.math.*;

public class InnReservations {
    Connection conn;
    PreparedStatement reserveRoom;

    public InnReservations() {
    }

    // Display the main menu
    private void displayOptions() {
        System.out.println("Options: ");
        System.out.println("\tRooms and Rates");
        System.out.println("\tReserve a room");
        System.out.println("\tChange a reservation");
        System.out.println("\tCancel a reservation");
        System.out.println("\tReservation detail");
        System.out.println("\tRevenue");
    }

    // Execute the chosen menu option
    private void executeInput(String userInput) {
        if (userInput.equals("Reserve a room")) {
            reserveARoom();
        }
    }

    // Reserve a room
    private void reserveARoom() {
        // Get the user's info and desired room info
        Scanner reader = new Scanner(System.in);
        System.out.print("First name: ");
        String firstName = reader.nextLine();
        System.out.print("Last name: ");
        String lastName = reader.nextLine();
        System.out.print("Room code: ");
        String roomCode = reader.nextLine();
        System.out.print("Bed type: ");
        String bedType = reader.nextLine();
        System.out.print("Check in: ");
        String checkIn = reader.nextLine();
        System.out.print("Check out: ");
        String checkOut = reader.nextLine();
        System.out.print("Number of children: ");
        Integer numChildren = reader.nextInt();
        System.out.print("Number of adults: ");
        Integer numAdults = reader.nextInt();

        // Database connection values
        String URL = "jdbc:mysql://db.labthreesixfive.com/nstapp?autoReconnect=true";
        String USER = "nstapp";
        String PASS = "S19_CSC-365-014333757";
        // Connect to the database
        try (Connection conn = DriverManager.getConnection(URL, USER, PASS)) {
            System.out.println("Connected!");
            // SQL query for finding available reservations
            String availableRoomsSql = "SELECT DISTINCT RoomCode, bedType, maxOcc, basePrice, RoomName, ROUND(((((DATEDIFF(?, ?)) - ((WEEK(?) + ((YEAR(?) - YEAR(?)) * 52) - WEEK(?)) * 2) - (CASE WHEN WEEKDAY(?) = 6 THEN 1 ELSE 0 END) - (CASE WHEN WEEKDAY(?) = 5 THEN 1 ELSE 0 END)) * basePrice) + (((DATEDIFF(?, ?)) - ((WEEK(?) + ((YEAR(?) - YEAR(?)) * 52) - WEEK(?)) * 2) - (CASE WHEN WEEKDAY(?) = 6 THEN 0 ELSE 1 END) - (CASE WHEN WEEKDAY(?) = 5 THEN 0 ELSE 1 END)) * basePrice * 1.1)) * 1.18, 2) AS TotalCost FROM lab7_rooms "
                    + "WHERE RoomCode LIKE (?) AND (bedType LIKE (?) AND maxOcc >= ? AND RoomCode NOT IN (SELECT Room FROM lab7_reservations WHERE ? < CheckOut AND ? > CheckIn))";

            // Prepare SQL statement to fetch matching rooms
            try (PreparedStatement availableRoomsStatement = conn.prepareStatement(availableRoomsSql)) {
                // Set correct dates for calculating total cost
                availableRoomsStatement.setDate(1, java.sql.Date.valueOf(checkOut));
                availableRoomsStatement.setDate(2, java.sql.Date.valueOf(checkIn));
                availableRoomsStatement.setDate(3, java.sql.Date.valueOf(checkIn));
                availableRoomsStatement.setDate(4, java.sql.Date.valueOf(checkIn));
                availableRoomsStatement.setDate(5, java.sql.Date.valueOf(checkIn));
                availableRoomsStatement.setDate(6, java.sql.Date.valueOf(checkIn));
                availableRoomsStatement.setDate(7, java.sql.Date.valueOf(checkIn));
                availableRoomsStatement.setDate(8, java.sql.Date.valueOf(checkIn));
                availableRoomsStatement.setDate(9, java.sql.Date.valueOf(checkOut));
                availableRoomsStatement.setDate(10, java.sql.Date.valueOf(checkIn));
                availableRoomsStatement.setDate(11, java.sql.Date.valueOf(checkIn));
                availableRoomsStatement.setDate(12, java.sql.Date.valueOf(checkIn));
                availableRoomsStatement.setDate(13, java.sql.Date.valueOf(checkIn));
                availableRoomsStatement.setDate(14, java.sql.Date.valueOf(checkIn));
                availableRoomsStatement.setDate(15, java.sql.Date.valueOf(checkIn));
                availableRoomsStatement.setDate(16, java.sql.Date.valueOf(checkIn));

                // All rooms are matching if user inputted "Any"
                if (roomCode.equals("Any")) {
                    availableRoomsStatement.setString(17, "%%");
                } else {
                    availableRoomsStatement.setString(17, roomCode);
                }
                // All bed types are matching if user inputted "any"
                if (bedType.equals("Any")) {
                    availableRoomsStatement.setString(18, "%%");
                } else {
                    availableRoomsStatement.setString(18, bedType);
                }

                availableRoomsStatement.setInt(19, numChildren + numAdults);
                availableRoomsStatement.setDate(20, java.sql.Date.valueOf(checkIn));
                availableRoomsStatement.setDate(21, java.sql.Date.valueOf(checkOut));

                System.out.println(availableRoomsStatement);

                // Execute the SQL query
                try (ResultSet availableRooms = availableRoomsStatement.executeQuery()) {
                    // Index for room options
                    int option = 1;
                    // Metadata and column count of the returned rows
                    ResultSetMetaData metaData = availableRooms.getMetaData();
                    int columns = metaData.getColumnCount();
                    // List of rows returned from the query
                    List<Map<String, Object>> roomList = new ArrayList<>();
                    // For each row
                    while (availableRooms.next()) {
                        // Add the row's data to the list
                        Map<String, Object> row = new HashMap<>(columns);
                        for (int i = 1; i <= columns; ++i) {
                            row.put(metaData.getColumnName(i), availableRooms.getObject(i));
                        }
                        roomList.add(row);
                        // Display the roomcode, bedtype, and maximum occupancy of each option
                        String availableRoomCode = availableRooms.getString("RoomCode");
                        String availableBedType = availableRooms.getString("bedType");
                        String availableOcc = availableRooms.getString("maxOcc");
                        System.out.format("%d %s %s %s\n", option, availableRoomCode, availableBedType, availableOcc);
                        option++;
                    }
                    // Get the desired option
                    System.out.print("Option: ");
                    Integer optionChosen = reader.nextInt();
                    reader.nextLine();
                    // Get the data for the chosen option
                    Map<String, Object> chosenRoom = roomList.get(optionChosen - 1);
                    System.out.println(chosenRoom);
                    // Display a confirmation page for the reservation
                    System.out.println(
                            "===========================================================================================");
                    System.out.format("|| %s | %s | %s | %s | %s | %s | %d | %d | %.2f ||%n",
                            firstName + " " + lastName, chosenRoom.get("RoomCode"), chosenRoom.get("RoomName"),
                            chosenRoom.get("bedType"), checkIn, checkOut, numAdults, numChildren,
                            chosenRoom.get("TotalCost"));
                    System.out.println(
                            "===========================================================================================");
                    System.out.print("Confirm? (Yes/No): ");
                    String confirmed = reader.nextLine();
                    if (confirmed.equals("No")) {
                        return;
                    } else {
                        String addReservationSQL = "INSERT INTO lab7_reservations (CODE, Room, CheckIn, Checkout, Rate, LastName, FirstName, Adults, Kids) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

                        try (PreparedStatement addReservationStatement = conn.prepareStatement(addReservationSQL)) {
                            Random random = new Random();
                            // Set the reservation ID
                            addReservationStatement.setInt(1, random.nextInt(200000));
                            addReservationStatement.setString(2, (String) chosenRoom.get("RoomCode"));
                            addReservationStatement.setDate(3, java.sql.Date.valueOf(checkIn));
                            addReservationStatement.setDate(4, java.sql.Date.valueOf(checkOut));
                            addReservationStatement.setBigDecimal(5, (BigDecimal) chosenRoom.get("TotalCost"));
                            addReservationStatement.setString(6, lastName);
                            addReservationStatement.setString(7, firstName);
                            addReservationStatement.setInt(8, numAdults);
                            addReservationStatement.setInt(9, numChildren);

                            System.out.println(addReservationStatement);

                            try {
                                addReservationStatement.executeUpdate();
                                System.out.println("Confirmed!");

                            } catch (SQLException error) {
                                System.out.println(error);
                                System.out.println("Error! Reservation not made");
                            }

                        } catch (SQLException error) {
                            System.out.println(error);
                            System.out.println("Bad insert prepare!");
                        }

                    }

                } catch (SQLException error) {
                    System.out.println(error);
                    System.out.println("Bad fetch!");
                }
            } catch (SQLException error) {
                System.out.println(error);
                System.out.println("Bad prepare!");
            }
        } catch (SQLException error) {
            System.out.println("Not connected!");
        }
        reader.close();

    }

    public static void main(String args[]) {
        InnReservations InnReserver = new InnReservations();
        InnReserver.run();
    }

    // Main loop
    private void run() {
        Scanner reader = new Scanner(System.in);
        // Run until the user quits
        while (true) {
            displayOptions();
            System.out.print("Enter an option: ");
            String userInput = reader.nextLine(); // Scans the next token of the input as an int.
            if (userInput.equals("Quit")) {
                break;
            }
            executeInput(userInput);
        }
        reader.close();
    }

}
