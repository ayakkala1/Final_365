import java.util.Scanner;
import java.sql.*;
import java.math.*;

import static java.sql.Date.valueOf;

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
        System.out.println(" 1. Rooms and Rates");
        System.out.println(" 2. Reserve a room");
        System.out.println(" 3. Change a reservation");
        System.out.println(" 4. Cancel a reservation");
        System.out.println(" 5. Reservation detail");
        System.out.println(" 6. Revenue");
    }

    private void executeInput(String userInput, Scanner in) {
        if (userInput.equals("Reserve a room") || userInput.equals("2")) {
            reserveARoom();
        } else if (userInput.equals("Rooms and Rates") || userInput.equals("1")) {
            roomRates();
        } else if (userInput.equals("Cancel a reservation") || userInput.equals("4")) {
            cancel(in);
        } else if (userInput.equals("Reservation detail") || userInput.equals("5")) {
            filter(in);
        } else if (userInput.equals("Change a reservation") || userInput.equals("3")) {
            change(in);
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

            //Scanner in = new Scanner(System.in);

            PreparedStatement pstmt = conn.prepareStatement("SELECT DISTINCT CODE, Room, CheckIn, Checkout, Rate, LastName, FirstName, Adults, Kids, RoomCode, RoomName, Beds, bedType, maxOcc, basePrice, decor FROM ( " +
                    "SELECT *,COUNT(*) OVER (PARTITION BY CODE) counts FROM ( " +
                    "SELECT * FROM lab7_reservations v " +
                    " INNER JOIN lab7_rooms r on (v.Room = r.RoomCode) " +
                    "WHERE FIrstName LIKE ? " +
                    "UNION ALL " +
                    "SELECT * FROM lab7_reservations v " +
                    " INNER JOIN lab7_rooms r on (v.Room = r.RoomCode) " +
                    "WHERE LastName LIKE ? " +
                    "UNION ALL " +
                    "SELECT * FROM lab7_reservations v " +
                    " INNER JOIN lab7_rooms r on (v.Room = r.RoomCode) " +
                    "WHERE (CheckIn BETWEEN ? and ?) or (Checkout BETWEEN ? and ?) " +
                    "UNION ALL " +
                    "SELECT * FROM lab7_reservations v " +
                    " INNER JOIN lab7_rooms r on (v.Room = r.RoomCode) " +
                    "WHERE CODE LIKE ? " +
                    "UNION ALL " +
                    "SELECT * FROM lab7_reservations v " +
                    " INNER JOIN lab7_rooms r on (v.Room = r.RoomCode) " +
                    "WHERE Room LIKE ? " +
                    ") t " +
                    ") counttable " +
                    "WHERE counts = ?;");

            int counter = 0;
            System.out.println("Choose a filter:\n" +
                    "- First Name (enter F)\n" +
                    "- Last Name (enter L)\n" +
                    "- Date (enter D)\n" +
                    "- Room Code (enter R)\n" +
                    "- Reservation Code (enter V)\n" +
                    "- Execute (enter E)");

            String code = in.next();
            String line = in.nextLine();
            String choices = "";

            boolean breakCase = false;
            while (counter <= 5) {
                if (line.length() > 1) {
                    code = "chicken";
                }
                switch (code) {
                    case "F":
                        if(choices.indexOf("F") == -1) {
                            System.out.println(" Enter First Name (Wildcard Expressions are okay)");
                            firstName = in.next();
                            counter++;
                            choices = choices + code;
                            System.out.println(" Filter Saved. ");
                        }
                        else{
                            System.out.println("You already filtered on First Name.");
                        }
                        break;
                    case "L":
                        if(choices.indexOf("L") == -1) {
                            System.out.println(" Enter Last Name (Wildcard Expressions are okay)");
                            lastName = in.next();
                            counter++;
                            choices = choices + code;

                            System.out.println(" Filter Saved. ");
                        }
                        else{
                            System.out.println("You already filtered on Last Name.");
                        }
                        break;
                    case "D":
                        System.out.println(" Enter Start Date | Format: Year-Month-Day");
                        startDate = valueOf(in.next());
                        System.out.println(" Enter End Date | Format: Year-Month-Day");
                        endDate = valueOf(in.next());
                        System.out.println(" Filter Saved. ");
                        counter++;
                        if(choices.indexOf("D") == -1) {
                            System.out.println(" Enter Start Date | Format: Year-Month-Day");
                            startDate = Date.valueOf(in.next());
                            System.out.println(" Enter End Date | Format: Year-Month-Day");
                            endDate = Date.valueOf(in.next());
                            System.out.println(" Filter Saved. ");
                            choices = choices + code;
                            counter++;
                        }
                        else{
                            System.out.println("You already filtered on Date.");
                        }
                        break;
                    case "R":
                        if(choices.indexOf("R") == -1) {
                            System.out.println(" Enter Room Code (Wildcard Expressions are okay)");
                            roomCode = in.next();
                            counter++;
                            System.out.println(" Filter Saved. ");
                            choices = choices + code;
                        }
                        else{
                            System.out.println("You already filtered on Room Code.");
                        }
                        break;
                    case "V":
                        if(choices.indexOf("V") == -1) {
                            System.out.println(" Enter Reservation Code (Wildcard Expressions are okay)");
                            resCode = in.next();
                            counter++;
                            System.out.println(" Filter Saved. ");
                            choices = choices + code;
                        }
                        else{
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
                code = in.next();
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

    private void cancel(Scanner in) {
        try {
            PreparedStatement pstmt = conn.prepareStatement("DELETE FROM lab7_reservations WHERE CODE = ?");
            //Scanner in = new Scanner(System.in);
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
            ResultSet rs = stmt.executeQuery("SELECT * FROM  " +
                    "( " +
                    " SELECT DISTINCT recent.* " +
                    " FROM " +
                    "  ( " +
                    "  SELECT Room,RoomName,Checkin as lastCheckIn,Checkout as lastCheckOut, recentLength, " +
                    "       (SELECT  " +
                    "                            (CASE " +
                    "        WHEN cases = 1 THEN cor1.Checkout " +
                    "                                ELSE CURDATE() " +
                    "       END " +
                    "                            ) " +
                    "                            FROM " +
                    "        ( " +
                    "                                SELECT v1.Room,Checkin,Checkout,RANK() over (ORDER BY DATEDIFF(Checkout,CURDATE()))  ranks, " +
                    "           ( " +
                    "                                            CASE " +
                    "                                             WHEN  DATEDIFF(Checkin,CURDATE()) > 0  and CURDATE() < (SELECT MIN(Checkin) FROM lab7_reservations " +
                    "                                       WHERE " +
                    "                                        lab7_reservations.CheckOut >= CURDATE() " +
                    "                                        and lab7_reservations.Room = t.Room  " +
                    "                                     ) THEN 0 " +
                    "           ELSE 1 " +
                    "                                            END " +
                    "                                            ) cases " +
                    "         FROM lab7_reservations v1 " +
                    "          INNER JOIN lab7_rooms r1 on (v1.Room = r1.RoomCode) " +
                    "         WHERE  " +
                    "           ( " +
                    "                                            v1.CheckOut >= CURDATE() " +
                    "           and v1.Room = t.Room  " +
                    "           and  r1.RoomName = t.RoomName  " +
                    "           and v1.Checkout NOT IN  " +
                    "               ( " +
                    "               SELECT Checkin FROM lab7_reservations " +
                    "               WHERE lab7_reservations.Room = v1.Room and lab7_reservations.CODE <> v1.CODE " +
                    "               ) " +
                    "           )  " +
                    "        ) cor1 " +
                    "       WHERE ranks = 1 " +
                    "       ) nextAvailability " +
                    "   FROM ( " +
                    "    SELECT *,  " +
                    "     DATEDIFF(Checkout,CheckIn) recentLength , " +
                    "     DATEDIFF(curdate(),CheckIn), " +
                    "     RANK() over (PARTITION by Room ORDER BY DATEDIFF(curdate(),CheckIn)) ranks  " +
                    "    FROM lab7_reservations v " +
                    "     INNER JOIN lab7_rooms r on (v.Room = r.RoomCode) " +
                    "     WHERE DATEDIFF(curdate(),CheckIn) >= 0  " +
                    "      ) AS t " +
                    "  WHERE ranks = 1 " +
                    "  ) AS recent " +
                    ") AS r23 " +
                    "NATURAL JOIN " +
                    "( " +
                    " SELECT Room,Beds,bedType,maxOcc,basePrice,decor, " +
                    "  ROUND( " +
                    "   SUM( " +
                    "    CASE  " +
                    "     WHEN (Checkout < date_sub(CURDATE(), INTERVAL 180 DAY) or (Checkin > CURDATE())) THEN 0 " +
                    "     WHEN CheckIn < CURDATE() and Checkout > CURDATE() THEN DATEDIFF(CURDATE(),Checkin) " +
                    "     WHEN Checkin < date_sub(CURDATE(), INTERVAL 180 DAY) and Checkout > date_sub(curdate(), INTERVAL 180 DAY) THEN DATEDIFF(Checkout,CURDATE()) " +
                    "     ELSE DATEDIFF(Checkout,Checkin) " +
                    "    END  " +
                    "       )/ 180  " +
                    "           , 2) AS proportion_last_180_days " +
                    "  FROM lab7_reservations v " +
                    "   INNER JOIN lab7_rooms r on (v.Room = r.RoomCode) " +
                    "  GROUP BY Room,RoomName " +
                    ") AS r1 ");
            System.out.println("");
            DBTablePrinter.printResultSet(rs);
          /*  ResultSetMetaData rsmd = rs.getMetaData();
            System.out.println("================================================================================================================================================================================================");
            System.out.format("|| %4s | %30s | %12s | %12s | %12s | %17s | %4s | %10s | %7s | %7s | %12s | %24s ||%n", rsmd.getColumnName(1),
                    rsmd.getColumnName(2), rsmd.getColumnName(3),rsmd.getColumnName(4),rsmd.getColumnName(5),rsmd.getColumnName(6),
                    rsmd.getColumnName(7),rsmd.getColumnName(8),rsmd.getColumnName(9),rsmd.getColumnName(10),rsmd.getColumnName(11),
                    rsmd.getColumnName(12));
            System.out.println("================================================================================================================================================================================================");
            while (rs.next()) {
                String Room = rs.getString("Room");
                String RoomName = rs.getString("RoomName");
                String lastCheckIn = rs.getString("lastCheckIn");
                String lastCheckOut = rs.getString("lastCheckOut");
                Integer recentLength = rs.getInt("recentLength");
                java.sql.Date nextAvailability = rs.getDate("nextAvailability");
                int Beds = rs.getInt("Beds");
                String bedType = rs.getString("bedType");
                Integer maxOcc = rs.getInt("maxOcc");
                Double basePrice = rs.getDouble("basePrice");
                String decor = rs.getString("decor");
                Double proportion_last_180_days = rs.getDouble("proportion_last_180_days");
                System.out.format("|| %4s | %30s | %12s | %12s | %12s | %17s | %4s | %10s | %7s | $%8.2f | %12s | %23.2f  ||%n", Room, RoomName, lastCheckIn,lastCheckOut,recentLength,nextAvailability,Beds,bedType,maxOcc,basePrice,decor, proportion_last_180_days);
            }
            System.out.println("================================================================================================================================================================================================");*/
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
        } catch (SQLException e) {
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
        try {
            PreparedStatement pstmt = conn.prepareStatement("SELECT CheckIn, CheckOut FROM lab7_reservations WHERE CODE = ?");
            pstmt.setInt(1, code);
            ResultSet rs = pstmt.executeQuery();
            rs.next();
            begin = rs.getDate("CheckIn");
            end = rs.getDate("CheckOut");

            System.out.print("Begin date (YYYY-MM-DD): ");
            String beginStr = reader.nextLine();
            if (!beginStr.equals("")) {
                begin = valueOf(beginStr);
            }
            System.out.print("End date (YYYY-MM-DD): ");
            String endStr = reader.nextLine();
            if (!endStr.equals("")) {
                end = valueOf(beginStr);
            }
            if (begin.compareTo(end) >= 0) {
                System.out.println("CheckIn cannot be before CheckOut");
                return;
            }
        } catch (Exception e) {
            System.out.println("Invalid date format");
            return;
        }

        System.out.print("Number of children (-1 to leave unchanged):");
        int children = reader.nextInt();
        System.out.print("Number of adults (-1 to leave unchanged): ");
        int adults = reader.nextInt();

        String setStr = "UPDATE lab7_reservations" +
                            "SET ? = ?" +
                            "WHERE CODE = ?";
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
        } catch (SQLException e) {
            System.out.println("An error has occurred");
        }
    }



    final private static void printResultSet(ResultSet rs) throws SQLException {

        // Prepare metadata object and get the number of columns.
        ResultSetMetaData rsmd = rs.getMetaData();
        int columnsNumber = rsmd.getColumnCount();

        // Print column names (a header).
        for (int i = 1; i <= columnsNumber; i++) {
            if (i > 1) System.out.print(" | ");
            System.out.print(rsmd.getColumnName(i));
        }
        System.out.println("");

        while (rs.next()) {
            for (int i = 1; i <= columnsNumber; i++) {
                if (i > 1) System.out.print(" | ");
                System.out.print(rs.getString(i));
            }
            System.out.println("");
        }
    }

    public static void main(String args[]) {
        InnReservations InnReserver = new InnReservations();
    }

    private void run() {
        displayOptions();
        Scanner reader = new Scanner(System.in); // Reading from System.in
        System.out.print("Enter an option: ");
        String userInput = reader.nextLine(); // Scans the next token of the input as an int.

        //ExecuteInput takes in reader to allow bash script to read in text files without errors
        executeInput(userInput, reader);
        reader.close();
    }

}
