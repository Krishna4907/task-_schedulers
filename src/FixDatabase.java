import java.sql.Connection;
import java.sql.Statement;

public class FixDatabase {
    public static void main(String[] args) {
        String sql = "ALTER TABLE projects DROP CONSTRAINT IF EXISTS projects_deadline_check";

        try (Connection con = DBConnection.getConnection();
             Statement stmt = con.createStatement()) {

            if (con != null) {
                stmt.execute(sql);
                System.out.println("Constraint 'projects_deadline_check' dropped successfully.");
            } else {
                System.out.println("Failed to connect to database.");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
