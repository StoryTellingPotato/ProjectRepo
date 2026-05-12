import java.sql.Connection;
import java.sql.DriverManager;

public class testDB {
    public static void main(String[] args) {
        // Change 'coffee_shop_db' to your actual database name in XAMPP
        String url = "jdbc:mysql://localhost:3306/coffee_shop_db";
        String user = "root"; 
        String password = ""; // XAMPP default is empty

        try {
            Class.forName("com.mysql.cj.jdbc.Driver"); // Add this line
            Connection conn = DriverManager.getConnection(url, user, password);
            // ... rest of your code
        } catch (Exception e) {
            System.out.println("Connection failed. Is XAMPP running?");
            e.printStackTrace();
        }
    }
}