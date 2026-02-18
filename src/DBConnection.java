import java.sql.Connection;
import java.sql.DriverManager;

public class DBConnection {

    private static final String URL = "jdbc:postgresql://localhost:5432/promanage_new";
    private static final String USER = "postgres";
    private static final String PASSWORD = "Krishna@4907";

    public static Connection getConnection() {
        try {

            return DriverManager.getConnection(URL, USER, PASSWORD);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
