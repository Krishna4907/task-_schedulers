import java.sql.Connection;
import java.sql.Statement;

public class InitHistoryTable {
    public static void main(String[] args) {
        String sql = "CREATE TABLE IF NOT EXISTS historical_projects (" +
                "id SERIAL PRIMARY KEY, " +
                "title VARCHAR(100), " +
                "deadline INT, " +
                "revenue INT, " +
                "completion_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP)";

        try (Connection con = DBConnection.getConnection();
             Statement stmt = con.createStatement()) {

            if (con != null) {
                stmt.execute(sql);
                System.out.println("Historical projects table created successfully.");

                // Add some dummy data for predictive analysis
                String dumpData = "INSERT INTO historical_projects (title, deadline, revenue) VALUES " +
                        "('Past UI Work', 5, 50000), " +
                        "('Past Backend', 4, 45000), " +
                        "('Past Design', 7, 30000), " +
                        "('Emergency Fix', 1, 80000), " +
                        "('Small Bug', 10, 5000), " +
                        "('Consultation', 2, 20000);";
                stmt.execute(dumpData);
                System.out.println("Mock history data populated.");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
