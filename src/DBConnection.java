import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;


 
public class DBConnection {

   
    private static final String URL = "jdbc:mysql://localhost:3306/lostfound_db";
    private static final String USER = "root";
    private static final String PASSWORD = "root123"; // change this to your MySQL root password

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }

    
    public static void main(String[] args) {
        try (Connection con = getConnection()) {
            System.out.println("Connected to MySQL successfully!");
            System.out.println("Database: " + con.getCatalog());
            System.out.println("MySQL version: " + con.getMetaData().getDatabaseProductVersion());
        } catch (SQLException e) {
            System.out.println("Connection failed: " + e.getMessage());
        }
    }
}
