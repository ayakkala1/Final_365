import java.util.ArrayList;
import java.util.Scanner;
import java.util.List;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.sql.*;

import static java.sql.Date.valueOf;
import java.math.BigDecimal;

public class InnReservations {
    Connection conn;

    public InnReservations() {
        String URL = System.getenv("LAB_URL");
        String USER = System.getenv("LAB_USER");
        String PASS = System.getenv("LAB_PASS");
        try {
            conn = DriverManager.getConnection(URL, USER, PASS);
            System.out.println("Connected!");
            run();
        } catch (SQLException error) {
            System.out.println("Not connected!");
        }
    }

    // Display the main menu
    private void displayOptions() {
        System.out.println("Options: ");
        System.out.println(" 1. Rooms and Rates");
        System.out.println(" 2. Reserve a room");
        System.out.println(" 3. Change a reservation");
        System.out.println(" 4. Cancel a reservation");
        System.out.println(" 5. Reservation detail");
        System.out.println(" 6. Revenue");
    }

    private void executeInput(String userInput, Scanner in) {
        if (userInput.equals("Reserve a room") || userInput.equals("2")) {
            reserveARoom(in);
        } else if (userInput.equals("Rooms and Rates") || userInput.equals("1")) {
            roomRates();
        } else if (userInput.equals("Cancel a reservation") || userInput.equals("4")) {
            cancel(in);
        } else if (userInput.equals("Reservation detail") || userInput.equals("5")) {
            filter(in);
        } else if (userInput.equals("Change a reservation") || userInput.equals("3")) {
            change(in);
        } else if (userInput.equals("Revenue") || userInput.equals("6")) {
            revenue();
        }
    }

    private void revenue() {
        try {
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery("" + "SELECT Room, "
                    + "   ROUND(SUM(case when MONTH(Checkout) = 1 then DATEDIFF(Checkout,Checkin) * Rate end),2)  AS January, "
                    + "       ROUND(SUM(case when MONTH(Checkout) = 2 then DATEDIFF(Checkout,Checkin) * Rate end),2)   AS February, "
                    + "       ROUND(SUM(case when MONTH(Checkout) = 3 then DATEDIFF(Checkout,Checkin) * Rate end),2)   AS March, "
                    + "       ROUND(SUM(case when MONTH(Checkout) = 4 then DATEDIFF(Checkout,Checkin) * Rate end),2)  AS April, "
                    + "       ROUND(SUM(case when MONTH(Checkout) = 5 then DATEDIFF(Checkout,Checkin) * Rate  end),2)  AS May, "
                    + "       ROUND(SUM(case when MONTH(Checkout) = 6 then DATEDIFF(Checkout,Checkin) * Rate end),2)   AS June, "
                    + "       ROUND(SUM(case when MONTH(Checkout) = 7 then DATEDIFF(Checkout,Checkin) * Rate end),2)   AS July, "
                    + "       ROUND(SUM(case when MONTH(Checkout) = 8 then DATEDIFF(Checkout,Checkin) * Rate end),2)   AS August, "
                    + "       ROUND(SUM(case when MONTH(Checkout) = 9 then DATEDIFF(Checkout,Checkin) * Rate end),2)   AS September, "
                    + "       ROUND(SUM(case when MONTH(Checkout) = 10 then DATEDIFF(Checkout,Checkin) * Rate end),2)   AS October, "
                    + "       ROUND(SUM(case when MONTH(Checkout) = 11 then DATEDIFF(Checkout,Checkin) * Rate end),2)   AS November, "
                    + "       ROUND(SUM(case when MONTH(Checkout) = 12 then DATEDIFF(Checkout,Checkin) * Rate end),2)   AS December, "
                    + "       ROUND(SUM(DATEDIFF(Checkout,Checkin) * Rate),2)  AS TOTAL "
                    + "       FROM lab7_reservations " + "GROUP BY Room");
            DBTablePrinter.printResultSet(rs);
            return;
        } catch (Exception e) {
            System.out.println("An error has occurred.");
            return;
        }
    }

    private void filter(Scanner in) {
        try {
            int FIRST_NAME_POS = 1;
            int LAST_NAME_POS = 2;
            int START_DATE_POS = 3;
            int END_DATE_POS = 4;
            int START_DATE_POS_2 = 5;
            int END_DATE_POS_2 = 6;
            int RES_CODE_POS = 7;
            int ROOM_CODE_POS = 8;
            int COUNTER_POS = 9;

            String firstName = " ";
            String lastName = " ";
            Date startDate = valueOf("2900-11-09");
            Date endDate = valueOf("2900-11-09");
            String resCode = " ";
            String roomCode = " ";

            PreparedStatement pstmt = conn.prepareStatement(
                    "SELECT DISTINCT CODE, Room, CheckIn, Checkout, Rate, LastName, FirstName, Adults, Kids, RoomCode, RoomName, Beds, bedType, maxOcc, basePrice, decor FROM ( "
                            + "SELECT *,COUNT(*) OVER (PARTITION BY CODE) counts FROM ( "
                            + "SELECT * FROM lab7_reservations v "
                            + " INNER JOIN lab7_rooms r on (v.Room = r.RoomCode) " + "WHERE FIrstName LIKE ? "
                            + "UNION ALL " + "SELECT * FROM lab7_reservations v "
                            + " INNER JOIN lab7_rooms r on (v.Room = r.RoomCode) " + "WHERE LastName LIKE ? "
                            + "UNION ALL " + "SELECT * FROM lab7_reservations v "
                            + " INNER JOIN lab7_rooms r on (v.Room = r.RoomCode) "
                            + "WHERE (CheckIn BETWEEN ? and ?) or (Checkout BETWEEN ? and ?) " + "UNION ALL "
                            + "SELECT * FROM lab7_reservations v "
                            + " INNER JOIN lab7_rooms r on (v.Room = r.RoomCode) " + "WHERE CODE LIKE ? " + "UNION ALL "
                            + "SELECT * FROM lab7_reservations v "
                            + " INNER JOIN lab7_rooms r on (v.Room = r.RoomCode) " + "WHERE Room LIKE ? " + ") t "
                            + ") counttable " + "WHERE counts = ?;");

            int counter = 0;
            System.out.println(
                    "Choose a filter:\n" + "- First Name (enter F)\n" + "- Last Name (enter L)\n" + "- Date (enter D)\n"
                            + "- Room Code (enter R)\n" + "- Reservation Code (enter V)\n" + "- Execute (enter E)");

            String code = in.next().toUpperCase();
            String line = in.nextLine();
            String choices = "";

            boolean breakCase = false;
            while (counter <= 5) {
                if (line.length() > 1) {
                    code = "chicken";
                }
                switch (code) {
                case "F":
                    if (choices.indexOf("F") == -1) {
                        System.out.println(" Enter First Name (Wildcard Expressions are okay)");
                        firstName = in.next();
                        counter++;
                        choices = choices + code;
                        System.out.println(" Filter Saved. ");
                    } else {
                        System.out.println("You already filtered on First Name.");
                    }
                    break;
                case "L":
                    if (choices.indexOf("L") == -1) {
                        System.out.println(" Enter Last Name (Wildcard Expressions are okay)");
                        lastName = in.next();
                        counter++;
                        choices = choices + code;

                        System.out.println(" Filter Saved. ");
                    } else {
                        System.out.println("You already filtered on Last Name.");
                    }
                    break;
                case "D":
                    if (choices.indexOf("D") == -1) {
                        System.out.println(" Enter Start Date | Format: Year-Month-Day");
                        startDate = Date.valueOf(in.next());
                        System.out.println(" Enter End Date | Format: Year-Month-Day");
                        endDate = Date.valueOf(in.next());
                        System.out.println(" Filter Saved. ");
                        choices = choices + code;
                        counter++;
                    } else {
                        System.out.println("You already filtered on Date.");
                    }
                    break;
                case "R":
                    if (choices.indexOf("R") == -1) {
                        System.out.println(" Enter Room Code (Wildcard Expressions are okay)");
                        roomCode = in.next();
                        counter++;
                        System.out.println(" Filter Saved. ");
                        choices = choices + code;
                    } else {
                        System.out.println("You already filtered on Room Code.");
                    }
                    break;
                case "V":
                    if (choices.indexOf("V") == -1) {
                        System.out.println(" Enter Reservation Code (Wildcard Expressions are okay)");
                        resCode = in.next();
                        counter++;
                        System.out.println(" Filter Saved. ");
                        choices = choices + code;
                    } else {
                        System.out.println("You already filtered on Reservation Code.");
                    }
                    break;
                case "E":
                    System.out.println("Will execute search now!");
                    breakCase = true;
                    break;
                default:
                    System.out.println("Not a valid input!");
                }
                if (breakCase) {
                    break;
                }
                code = in.next().toUpperCase();
                if (in.hasNextLine()) {
                    line = in.nextLine();
                }
            }
            pstmt.setString(FIRST_NAME_POS, firstName);
            pstmt.setString(LAST_NAME_POS, lastName);
            pstmt.setDate(START_DATE_POS, startDate);
            pstmt.setDate(END_DATE_POS, endDate);
            pstmt.setDate(START_DATE_POS_2, startDate);
            pstmt.setDate(END_DATE_POS_2, endDate);
            pstmt.setString(ROOM_CODE_POS, roomCode);
            pstmt.setString(RES_CODE_POS, resCode);
            pstmt.setInt(COUNTER_POS, counter);

            if (counter == 5) {
                System.out.println("All filters have been used, will execute search now!");
            } else if (counter == 0) {
                System.out.println("You gave no filters, no search will be executed.");
            } else {
                ResultSet rs = pstmt.executeQuery();
                DBTablePrinter.printResultSet(rs);
            }
        } catch (Exception e) {
            System.out.println("An error has occurred.");
        }
    }

    // Reserve a room
    private void reserveARoom(Scanner reader) {
        ResultSet availableRooms = null;
        Boolean roomsExist = null;
        Boolean noRoomsAvailable = null;
        PreparedStatement availableRoomsStatement = null;
        // Get the user's info and desired room info
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
        reader.nextLine();

        String checkMaxOccSql = "SELECT RoomCode FROM lab7_rooms WHERE maxOcc >= ?";

        // Check if any rooms are large enough
        try (PreparedStatement checkMaxOccStatment = conn.prepareStatement(checkMaxOccSql)) {
            checkMaxOccStatment.setInt(1, numChildren + numAdults);

            try (ResultSet rooms = checkMaxOccStatment.executeQuery()) {
                if (rooms.next() == false) {
                    roomsExist = false;
                } else {
                    roomsExist = true;
                }
            } catch (SQLException error) {
                System.out.println(error);
                System.out.println("Bad room size check!");
            }
        } catch (SQLException error) {
            System.out.println(error);
            System.out.println("Bad room size statement!");
        }
        // No rooms are large enough
        if (!roomsExist) {
            System.out.println("No rooms large enough, you must make multiple reservations");
        } else { // Large enough rooms exist
            // SQL query for finding available reservations
            String availableRoomsSQL = ""
                    + "SELECT DISTINCT RoomCode, bedType, maxOcc, basePrice, RoomName, ? AS checkIn, ? AS checkOut, ROUND(((((DATEDIFF(?, ?)) - ((WEEK(?) + ((YEAR(?) - YEAR(?)) * 52) - WEEK(?)) * 2) - (CASE WHEN WEEKDAY(?) = 6 THEN 1 ELSE 0 END) - (CASE WHEN WEEKDAY(?) = 5 THEN 1 ELSE 0 END)) * basePrice) + (((DATEDIFF(?, ?)) - ((WEEK(?) + ((YEAR(?) - YEAR(?)) * 52) - WEEK(?)) * 2) - (CASE WHEN WEEKDAY(?) = 6 THEN 0 ELSE 1 END) - (CASE WHEN WEEKDAY(?) = 5 THEN 0 ELSE 1 END)) * basePrice * 1.1)) * 1.18, 2) AS TotalCost FROM lab7_rooms WHERE RoomCode LIKE (?) AND (bedType LIKE (?) AND maxOcc >= ? AND RoomCode NOT IN (SELECT room FROM lab7_reservations WHERE ? < checkOut AND ? > checkIn))";
            // Prepare SQL statement to fetch matching rooms
            try {
                availableRoomsStatement = conn.prepareStatement(availableRoomsSQL);
                availableRoomsStatement.setDate(1, java.sql.Date.valueOf(checkIn));
                availableRoomsStatement.setDate(2, java.sql.Date.valueOf(checkOut));
                // Set correct dates for calculating total cost
                availableRoomsStatement.setDate(3, java.sql.Date.valueOf(checkOut));
                availableRoomsStatement.setDate(4, java.sql.Date.valueOf(checkIn));
                availableRoomsStatement.setDate(5, java.sql.Date.valueOf(checkIn));
                availableRoomsStatement.setDate(6, java.sql.Date.valueOf(checkIn));
                availableRoomsStatement.setDate(7, java.sql.Date.valueOf(checkIn));
                availableRoomsStatement.setDate(8, java.sql.Date.valueOf(checkIn));
                availableRoomsStatement.setDate(9, java.sql.Date.valueOf(checkIn));
                availableRoomsStatement.setDate(10, java.sql.Date.valueOf(checkIn));
                availableRoomsStatement.setDate(11, java.sql.Date.valueOf(checkOut));
                availableRoomsStatement.setDate(12, java.sql.Date.valueOf(checkIn));
                availableRoomsStatement.setDate(13, java.sql.Date.valueOf(checkIn));
                availableRoomsStatement.setDate(14, java.sql.Date.valueOf(checkIn));
                availableRoomsStatement.setDate(15, java.sql.Date.valueOf(checkIn));
                availableRoomsStatement.setDate(16, java.sql.Date.valueOf(checkIn));
                availableRoomsStatement.setDate(17, java.sql.Date.valueOf(checkIn));
                availableRoomsStatement.setDate(18, java.sql.Date.valueOf(checkIn));

                // All rooms are matching if user inputted "Any"
                if (roomCode.equals("Any")) {
                    availableRoomsStatement.setString(19, "%%");
                } else {
                    availableRoomsStatement.setString(19, roomCode);
                }
                // All bed types are matching if user inputted "any"
                if (bedType.equals("Any")) {
                    availableRoomsStatement.setString(20, "%%");
                } else {
                    availableRoomsStatement.setString(20, bedType);
                }

                availableRoomsStatement.setInt(21, numChildren + numAdults);
                availableRoomsStatement.setDate(22, java.sql.Date.valueOf(checkIn));
                availableRoomsStatement.setDate(23, java.sql.Date.valueOf(checkOut));
                availableRooms = availableRoomsStatement.executeQuery();

                if (availableRooms.next() == false) {
                    noRoomsAvailable = true;
                }
            } catch (SQLException error) {
                System.out.println(error);
                System.out.println("Bad available!");
            }

            // No exact matching rooms found, find the nearest available dates
            if (noRoomsAvailable) {
                System.out.println("No exact matches, similar available dates shown below");
                String similarAvailableRoomsSQL = "SELECT DISTINCT Room AS RoomCode, bedType, maxOcc, basePrice, RoomName, nextAvailability AS checkIn, DATE_ADD(nextAvailability, INTERVAL DATEDIFF(?, ?) DAY) AS checkOut, ROUND(((((DATEDIFF(?, ?)) - ((WEEK(?) + ((YEAR(?) - YEAR(?)) * 52) - WEEK(?)) * 2) - (CASE WHEN WEEKDAY(?) = 6 THEN 1 ELSE 0 END) - (CASE WHEN WEEKDAY(?) = 5 THEN 1 ELSE 0 END)) * basePrice) + (((DATEDIFF(?, ?)) - ((WEEK(?) + ((YEAR(?) - YEAR(?)) * 52) - WEEK(?)) * 2) - (CASE WHEN WEEKDAY(?) = 6 THEN 0 ELSE 1 END) - (CASE WHEN WEEKDAY(?) = 5 THEN 0 ELSE 1 END)) * basePrice * 1.1)) * 1.18, 2) AS TotalCost FROM  "
                        + "( " + " SELECT DISTINCT recent.* " + " FROM " + "  ( "
                        + "  SELECT Room, RoomName,Checkin as lastCheckIn,Checkout as lastCheckOut, recentLength, "
                        + "       (SELECT  " + "                            (CASE "
                        + "        WHEN cases = 1 THEN cor1.Checkout " + "                                ELSE ? "
                        + "       END " + "                            ) " + "                            FROM "
                        + "        ( "
                        + "                                SELECT v1.Room,Checkin,Checkout,RANK() over (ORDER BY DATEDIFF(Checkout,?))  ranks, "
                        + "           ( " + "                                            CASE "
                        + "                                             WHEN  DATEDIFF(Checkin,?) > 0  and ? < (SELECT MIN(Checkin) FROM lab7_reservations "
                        + "                                       WHERE "
                        + "                                        lab7_reservations.CheckOut >= ? "
                        + "                                        and lab7_reservations.Room = t.Room  "
                        + "                                     ) THEN 0 " + "           ELSE 1 "
                        + "                                            END "
                        + "                                            ) cases " + "         FROM lab7_reservations v1 "
                        + "          INNER JOIN lab7_rooms r1 on (v1.Room = r1.RoomCode) " + "         WHERE  "
                        + "           ( " + "                                            v1.CheckOut >= ? "
                        + "           and v1.Room = t.Room  " + "           and  r1.RoomName = t.RoomName  "
                        + "           and v1.Checkout NOT IN  " + "               ( "
                        + "               SELECT Checkin FROM lab7_reservations "
                        + "               WHERE lab7_reservations.Room = v1.Room and lab7_reservations.CODE <> v1.CODE "
                        + "               ) " + "           )  " + "        ) cor1 " + "       WHERE ranks = 1 "
                        + "       ) nextAvailability " + "   FROM ( " + "    SELECT *,  "
                        + "     DATEDIFF(Checkout,CheckIn) recentLength , " + "     DATEDIFF(?,CheckIn), "
                        + "     RANK() over (PARTITION by Room ORDER BY DATEDIFF(?,CheckIn)) ranks  "
                        + "    FROM lab7_reservations v " + "     INNER JOIN lab7_rooms r on (v.Room = r.RoomCode) "
                        + "     WHERE DATEDIFF(?,CheckIn) >= 0  " + "      ) AS t " + "  WHERE ranks = 1 "
                        + "  ) AS recent " + ") AS r23 " + "NATURAL JOIN " + "( "
                        + " SELECT Room,Beds,bedType,maxOcc,basePrice,decor, " + "  ROUND( " + "   SUM( " + "    CASE  "
                        + "     WHEN (Checkout < date_sub(?, INTERVAL 180 DAY) or (Checkin > ?)) THEN 0 "
                        + "     WHEN CheckIn < ? and Checkout > ? THEN DATEDIFF(?,Checkin) "
                        + "     WHEN Checkin < date_sub(?, INTERVAL 180 DAY) and Checkout > date_sub(?, INTERVAL 180 DAY) THEN DATEDIFF(Checkout,?) "
                        + "     ELSE DATEDIFF(Checkout,Checkin) " + "    END  " + "       )/ 180  "
                        + "           , 2) AS proportion_last_180_days " + "  FROM lab7_reservations v "
                        + "   INNER JOIN lab7_rooms r on (v.Room = r.RoomCode) "
                        + " GROUP BY Room,RoomName) AS r1 WHERE maxOcc >= ? AND Room NOT IN (SELECT room FROM lab7_reservations WHERE nextAvailability < checkOut AND DATE_ADD(nextAvailability, INTERVAL DATEDIFF(?, ?) DAY) > checkIn) LIMIT 5;";

                try {
                    availableRoomsStatement = conn.prepareStatement(similarAvailableRoomsSQL);
                    // Set correct dates for calculating total cost
                    availableRoomsStatement.setDate(1, java.sql.Date.valueOf(checkOut));
                    availableRoomsStatement.setDate(2, java.sql.Date.valueOf(checkIn));
                    availableRoomsStatement.setDate(3, java.sql.Date.valueOf(checkOut));
                    availableRoomsStatement.setDate(4, java.sql.Date.valueOf(checkIn));
                    availableRoomsStatement.setDate(5, java.sql.Date.valueOf(checkIn));
                    availableRoomsStatement.setDate(6, java.sql.Date.valueOf(checkIn));
                    availableRoomsStatement.setDate(7, java.sql.Date.valueOf(checkIn));
                    availableRoomsStatement.setDate(8, java.sql.Date.valueOf(checkIn));
                    availableRoomsStatement.setDate(9, java.sql.Date.valueOf(checkIn));
                    availableRoomsStatement.setDate(10, java.sql.Date.valueOf(checkIn));
                    availableRoomsStatement.setDate(11, java.sql.Date.valueOf(checkOut));
                    availableRoomsStatement.setDate(12, java.sql.Date.valueOf(checkIn));
                    availableRoomsStatement.setDate(13, java.sql.Date.valueOf(checkIn));
                    availableRoomsStatement.setDate(14, java.sql.Date.valueOf(checkIn));
                    availableRoomsStatement.setDate(15, java.sql.Date.valueOf(checkIn));
                    availableRoomsStatement.setDate(16, java.sql.Date.valueOf(checkIn));
                    availableRoomsStatement.setDate(17, java.sql.Date.valueOf(checkIn));
                    availableRoomsStatement.setDate(18, java.sql.Date.valueOf(checkIn));
                    availableRoomsStatement.setDate(19, java.sql.Date.valueOf(checkIn));
                    availableRoomsStatement.setDate(20, java.sql.Date.valueOf(checkIn));
                    availableRoomsStatement.setDate(21, java.sql.Date.valueOf(checkIn));
                    availableRoomsStatement.setDate(22, java.sql.Date.valueOf(checkIn));
                    availableRoomsStatement.setDate(23, java.sql.Date.valueOf(checkIn));
                    availableRoomsStatement.setDate(24, java.sql.Date.valueOf(checkIn));
                    availableRoomsStatement.setDate(25, java.sql.Date.valueOf(checkIn));
                    availableRoomsStatement.setDate(26, java.sql.Date.valueOf(checkIn));
                    availableRoomsStatement.setDate(27, java.sql.Date.valueOf(checkIn));
                    availableRoomsStatement.setDate(28, java.sql.Date.valueOf(checkIn));
                    availableRoomsStatement.setDate(29, java.sql.Date.valueOf(checkIn));
                    availableRoomsStatement.setDate(30, java.sql.Date.valueOf(checkIn));
                    availableRoomsStatement.setDate(31, java.sql.Date.valueOf(checkIn));
                    availableRoomsStatement.setDate(32, java.sql.Date.valueOf(checkIn));
                    availableRoomsStatement.setDate(33, java.sql.Date.valueOf(checkIn));
                    availableRoomsStatement.setDate(34, java.sql.Date.valueOf(checkIn));
                    availableRoomsStatement.setDate(35, java.sql.Date.valueOf(checkIn));

                    availableRoomsStatement.setInt(36, numChildren + numAdults);
                    availableRoomsStatement.setDate(37, java.sql.Date.valueOf(checkOut));
                    availableRoomsStatement.setDate(38, java.sql.Date.valueOf(checkIn));

                    availableRooms = availableRoomsStatement.executeQuery();
                } catch (SQLException error) {
                    System.out.println(error);
                    System.out.println("Bad similar!");
                }
            }
            try {
                // Index for room options
                int option = 1;
                // Metadata and column count of the returned rows
                ResultSetMetaData metaData = availableRooms.getMetaData();
                int columns = metaData.getColumnCount();
                // List of rows returned from the query
                List<Map<String, Object>> roomList = new ArrayList<>();
                System.out.println(
                        "===========================================================================================");
                // For each row
                while (availableRooms.next()) {
                    // Add the row's data to the list
                    Map<String, Object> row = new HashMap<>(columns);
                    for (int i = 1; i <= columns; ++i) {
                        row.put(metaData.getColumnLabel(i), availableRooms.getObject(i));
                    }
                    roomList.add(row);
                    // Display the roomcode, bedtype, and maximum occupancy of each option
                    String availableRoomCode = availableRooms.getString("RoomCode");
                    String availableBedType = availableRooms.getString("bedType");
                    String availableOcc = availableRooms.getString("maxOcc");
                    String availableIn = availableRooms.getString("checkIn");
                    String availableOut = availableRooms.getString("checkOut");
                    System.out.format("|| %d | %s | %s | %s | %s | %s\n", option, availableRoomCode, availableBedType,
                            availableOcc, availableIn, availableOut);

                    option++;
                }
                System.out.println(
                        "===========================================================================================");
                // Get the desired option
                System.out.print("Option (type Cancel to cancel): ");
                String optionChosen = reader.nextLine();
                boolean validOption = true;

                try {
                    Integer.parseInt(optionChosen);
                } catch (NumberFormatException e) {

                    validOption = false;
                }
                // The chosen option is valid
                if (validOption && Integer.parseInt(optionChosen) < roomList.size()) {
                    // Get the data for the chosen option
                    Map<String, Object> chosenRoom = roomList.get(Integer.parseInt(optionChosen) - 1);
                    // Display a confirmation page for the reservation
                    System.out.println(
                            "===========================================================================================");
                    System.out.format("|| %s | %s | %s | %s | %s | %s | %d | %d | %.2f ||%n",
                            firstName + " " + lastName, chosenRoom.get("RoomCode"), chosenRoom.get("RoomName"),
                            chosenRoom.get("bedType"), chosenRoom.get("checkIn"), chosenRoom.get("checkOut"), numAdults,
                            numChildren, chosenRoom.get("TotalCost"));
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
                            addReservationStatement.setDate(3,
                                    java.sql.Date.valueOf((String) chosenRoom.get("checkIn")));
                            addReservationStatement.setDate(4,
                                    java.sql.Date.valueOf((String) chosenRoom.get("checkOut")));
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
                }
            } catch (SQLException error) {
                System.out.println(error);
                System.out.println("Bad results!");
            }

        }
        try {
            availableRoomsStatement.close();
        } catch (SQLException error) {
            System.out.println(error);
            System.out.println("Bad closing!");
        }
    }

    private void cancel(Scanner in) {
        try {
            PreparedStatement pstmt = conn.prepareStatement("DELETE FROM lab7_reservations WHERE CODE = ?");
            // Scanner in = new Scanner(System.in);
            System.out.println("Give the code for the reservation you wish to cancel: ");
            Integer code = Integer.parseInt(in.next());
            System.out.println("Are you sure you want to cancel this Reservation? (Yes | No)");
            String answer = in.next();
            if (answer.equals("Yes") | answer.equals("yes")) {
                pstmt.setInt(1, code);
                int rowCount = pstmt.executeUpdate();
                if (rowCount == 0) {
                    System.out.println("Reservation is not found.");
                } else {
                    System.out.println("Reservation has been cancelled.");
                }
            } else {
                System.out.println("Reservation remains.");
            }
        } catch (Exception e) {
            System.out.println("An error as occurred.");
        }
    }

    private void roomRates() {
        try {
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT * FROM  " + "( " + " SELECT DISTINCT recent.* " + " FROM " + "  ( "
                    + "  SELECT Room,RoomName,Checkin as lastCheckIn,Checkout as lastCheckOut, recentLength, "
                    + "       (SELECT  " + "                            (CASE "
                    + "        WHEN cases = 1 THEN cor1.Checkout " + "                                ELSE CURDATE() "
                    + "       END " + "                            ) " + "                            FROM "
                    + "        ( "
                    + "                                SELECT v1.Room,Checkin,Checkout,RANK() over (ORDER BY DATEDIFF(Checkout,CURDATE()))  ranks, "
                    + "           ( " + "                                            CASE "
                    + "                                             WHEN  DATEDIFF(Checkin,CURDATE()) > 0  and CURDATE() < (SELECT MIN(Checkin) FROM lab7_reservations "
                    + "                                       WHERE "
                    + "                                        lab7_reservations.CheckOut >= CURDATE() "
                    + "                                        and lab7_reservations.Room = t.Room  "
                    + "                                     ) THEN 0 " + "           ELSE 1 "
                    + "                                            END "
                    + "                                            ) cases " + "         FROM lab7_reservations v1 "
                    + "          INNER JOIN lab7_rooms r1 on (v1.Room = r1.RoomCode) " + "         WHERE  "
                    + "           ( " + "                                            v1.CheckOut >= CURDATE() "
                    + "           and v1.Room = t.Room  " + "           and  r1.RoomName = t.RoomName  "
                    + "           and v1.Checkout NOT IN  " + "               ( "
                    + "               SELECT Checkin FROM lab7_reservations "
                    + "               WHERE lab7_reservations.Room = v1.Room and lab7_reservations.CODE <> v1.CODE "
                    + "               ) " + "           )  " + "        ) cor1 " + "       WHERE ranks = 1 "
                    + "       ) nextAvailability " + "   FROM ( " + "    SELECT *,  "
                    + "     DATEDIFF(Checkout,CheckIn) recentLength , " + "     DATEDIFF(curdate(),CheckIn), "
                    + "     RANK() over (PARTITION by Room ORDER BY DATEDIFF(curdate(),CheckIn)) ranks  "
                    + "    FROM lab7_reservations v " + "     INNER JOIN lab7_rooms r on (v.Room = r.RoomCode) "
                    + "     WHERE DATEDIFF(curdate(),CheckIn) >= 0  " + "      ) AS t " + "  WHERE ranks = 1 "
                    + "  ) AS recent " + ") AS r23 " + "NATURAL JOIN " + "( "
                    + " SELECT Room,Beds,bedType,maxOcc,basePrice,decor, " + "  ROUND( " + "   SUM( " + "    CASE  "
                    + "     WHEN (Checkout < date_sub(CURDATE(), INTERVAL 180 DAY) or (Checkin > CURDATE())) THEN 0 "
                    + "     WHEN CheckIn < CURDATE() and Checkout > CURDATE() THEN DATEDIFF(CURDATE(),Checkin) "
                    + "     WHEN Checkin < date_sub(CURDATE(), INTERVAL 180 DAY) and Checkout > date_sub(curdate(), INTERVAL 180 DAY) THEN DATEDIFF(Checkout,CURDATE()) "
                    + "     ELSE DATEDIFF(Checkout,Checkin) " + "    END  " + "       )/ 180  "
                    + "           , 2) AS proportion_last_180_days " + "  FROM lab7_reservations v "
                    + "   INNER JOIN lab7_rooms r on (v.Room = r.RoomCode) " + "  GROUP BY Room,RoomName "
                    + ") AS r1 ");
            System.out.println("");
            DBTablePrinter.printResultSet(rs);
            return;
        } catch (Exception e) {
            System.out.println("Error has occurred!");
        }
    }

    private void change(Scanner reader) {
        System.out.print("Enter reservation code: ");
        int code = reader.nextInt();
        // Check if the given code exists
        String query = "SELECT EXISTS (SELECT * FROM lab7_reservations WHERE CODE = ?) AS CodeExists";
        try {
            PreparedStatement pstmt = conn.prepareStatement(query);
            pstmt.setInt(1, code);
            ResultSet rs = pstmt.executeQuery();
            rs.next();
            int res = rs.getInt("CodeExists");
            if (res == 0) {
                System.out.println("Invalid room code");
                return;
            }
        } catch (Exception e) {
            System.out.println(e);
            System.out.println("An error has occurred");
            return;
        }
        reader.nextLine();
        System.out.println("Enter the new value to set (or leave blank to leave unchanged)");
        System.out.print("First name: ");
        String first = reader.nextLine();
        System.out.print("Last name: ");
        String last = reader.nextLine();

        Date begin, end;
        String roomCode;
        try {
            PreparedStatement pstmt = conn
                    .prepareStatement("SELECT CheckIn, CheckOut FROM lab7_reservations WHERE CODE = ?");
            pstmt.setInt(1, code);
            ResultSet rs = pstmt.executeQuery();
            rs.next();
            begin = rs.getDate("CheckIn");
            end = rs.getDate("CheckOut");
            roomCode = rs.getString("Room");

            System.out.print("Begin date (YYYY-MM-DD): ");
            String beginStr = reader.nextLine();
            if (!beginStr.equals("")) {
                begin = valueOf(beginStr);
            }
            System.out.print("End date (YYYY-MM-DD): ");
            String endStr = reader.nextLine();
            if (!endStr.equals("")) {
                end = valueOf(endStr);
            }
            System.out.println(begin + " " + end);
            if (end.compareTo(begin) <= 0) {
                System.out.println("CheckOut cannot be before CheckIn");
                return;
            }
        } catch (Exception e) {
            System.out.println(e);
            System.out.println("Invalid date format");
            return;
        }

        /*
         * from stackoverflow: checking for date overlapping SELECT * FROM tbl WHERE
         * existing_start BETWEEN $newStart AND $newEnd OR $newStart BETWEEN
         * existing_start AND existing_end
         */
        try {
            PreparedStatement pstmt = conn.prepareStatement("SELECT EXISTS ( SELECT * FROM lab7_reservations WHERE"
                    + "((CheckIn >= ? AND CheckIn < ?) OR (? >= CheckIn AND ? < CheckOut))"
                    + "AND CODE != ? AND Room = ?)");
            pstmt.setDate(1, begin);
            pstmt.setDate(2, end);
            pstmt.setDate(3, begin);
            pstmt.setDate(4, begin);
            pstmt.setInt(5, code);
            pstmt.setString(6, roomCode);
            ResultSet rs = pstmt.executeQuery();
            rs.next();
            if (rs.getInt(1) == 1) {
                System.out.println("Reservation overlaps with existing reservation");
                return;
            }
        } catch (SQLException e) {
            System.out.println(e);
            return;
        }
        System.out.print("Number of kids (-1 to leave unchanged):");
        int children = reader.nextInt();
        System.out.print("Number of adults (-1 to leave unchanged): ");
        int adults = reader.nextInt();

        String setStr = "UPDATE lab7_reservations" + "SET ? = ?" + "WHERE CODE = ?";
        try {
            PreparedStatement pstmt = conn.prepareStatement(setStr);
            pstmt.setInt(3, code);
            if (!first.equals("")) {
                pstmt.setString(1, "FirstName");
                pstmt.setString(2, first);
                pstmt.executeUpdate();
            }
            if (!last.equals("")) {
                pstmt.setString(1, "LastName");
                pstmt.setString(2, last);
                pstmt.executeUpdate();
            }
            // Always sets date even if unchanged
            pstmt.setString(1, "CheckIn");
            pstmt.setDate(2, begin);
            pstmt.executeUpdate();

            pstmt.setString(1, "CheckOut");
            pstmt.setDate(2, end);
            pstmt.executeUpdate();

            if (adults != -1) {
                pstmt.setString(1, "Adults");
                pstmt.setInt(2, adults);
                pstmt.executeUpdate();
            }

            if (children != -1) {
                pstmt.setString(1, "Kids");
                pstmt.setInt(2, children);
                pstmt.executeUpdate();
            }
        } catch (SQLException e) {
            System.out.println("An error has occurred");
        }
        System.out.println("Update completed successfully");
    }

    public static void main(String args[]) {
        InnReservations InnReserver = new InnReservations();
    }

    // Main loop
    private void run() {
        Scanner reader = new Scanner(System.in); // Reading from System.in
        while (true) {
            displayOptions();
            System.out.print("Enter an option (type Exit to exit): ");
            String userInput = reader.nextLine(); // Scans the next token of the input as an int.
            if (userInput.equals("Exit")) {
                break;
            }
            // ExecuteInput takes in reader to allow bash script to read in text files
            // without errors
            executeInput(userInput, reader);
        }
        reader.close();
    }

}
