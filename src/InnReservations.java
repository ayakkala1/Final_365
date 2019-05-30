import java.util.Scanner;
import java.sql.*;
import java.math.*;

public class InnReservations {
    Connection conn;
    PreparedStatement reserveRoom;

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

    private void displayOptions() {
        System.out.println("Options: ");
        System.out.println(" Rooms and Rates");
        System.out.println(" Reserve a room");
        System.out.println(" Change a reservation");
        System.out.println(" Cancel a reservation");
        System.out.println(" Reservation detail");
        System.out.println(" Revenue");
    }

    private void executeInput(String userInput) {
        if (userInput.equals("Reserve a room")) {
            reserveARoom();
        }
        else if (userInput.equals("Rooms and Rates")){
            roomRates();
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
    private void roomRates(){
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
            ResultSetMetaData rsmd = rs.getMetaData();
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
            System.out.println("================================================================================================================================================================================================");
            return;
        }
        catch(Exception e){
            System.out.println("Error has occurred!");
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
        executeInput(userInput);
        reader.close();
    }

}
