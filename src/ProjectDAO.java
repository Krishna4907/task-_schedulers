import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ProjectDAO {

    public void addProject(String title, int deadline, int revenue) {
        String sql = "INSERT INTO projects (title, deadline, revenue) VALUES (?, ?, ?)";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, title);
            ps.setInt(2, deadline);
            ps.setInt(3, revenue);
            ps.executeUpdate();

            System.out.println(" Project added successfully!");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public List<Project> getAllProjects() {
        List<Project> projects = new ArrayList<>();
        String sql = "SELECT * FROM projects";

        try (Connection con = DBConnection.getConnection();
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery(sql)) {

            while (rs.next()) {
                projects.add(new Project(
                        rs.getInt("project_id"),
                        rs.getString("title"),
                        rs.getInt("deadline"),
                        rs.getInt("revenue")));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return projects;
    }

    public void completeProject(int id) {
        String selectSql = "SELECT * FROM projects WHERE project_id = ?";
        String insertSql = "INSERT INTO historical_projects (title, deadline, revenue) VALUES (?, ?, ?)";
        String deleteSql = "DELETE FROM projects WHERE project_id = ?";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement psSel = con.prepareStatement(selectSql);
             PreparedStatement psIns = con.prepareStatement(insertSql);
             PreparedStatement psDel = con.prepareStatement(deleteSql)) {

            // Ensure the history table exists before we try to use it
            ensureHistoryTableExists(con);

            psSel.setInt(1, id);
            ResultSet rs = psSel.executeQuery();

            if (rs.next()) {
                psIns.setString(1, rs.getString("title"));
                psIns.setInt(2, rs.getInt("deadline"));
                psIns.setInt(3, rs.getInt("revenue"));
                psIns.executeUpdate();

                psDel.setInt(1, id);
                psDel.executeUpdate();
                System.out.println(" Project marked as completed and moved to history!");
            } else {
                System.out.println(" Project not found!");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void ensureHistoryTableExists(Connection con) throws SQLException {
        String sql = "CREATE TABLE IF NOT EXISTS historical_projects (" +
                "id SERIAL PRIMARY KEY, " +
                "title VARCHAR(100), " +
                "deadline INT, " +
                "revenue INT, " +
                "completion_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP)";
        try (Statement st = con.createStatement()) {
            st.execute(sql);
        }
    }
}
