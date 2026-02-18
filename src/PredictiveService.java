import java.sql.*;
import java.util.*;

public class PredictiveService {

    /**
     * Prediction Model: Calculates a "Strategic Value" for a project.
     * Strategic Value = Revenue + (Predictive Weight based on Deadline and history)
     *
     * Edge Case Handling:
     * If a project has a long deadline (>5), it has "Flexibility".
     * If history shows we usually get high-revenue short-deadline projects later,
     * we penalize using slots for long-deadline projects now.
     */
    public static double calculateStrategicValue(Project p, List<Project> history) {
        if (history.isEmpty()) {
            return p.revenue;
        }

        double currentDensity = (double) p.revenue / Math.max(1, p.deadline);

        // Calculate Average Revenue Density from history (Revenue/Deadline)
        double avgDensity = history.stream()
                .mapToDouble(hp -> (double) hp.revenue / Math.max(1, hp.deadline))
                .average()
                .orElse(0.0);

        // Potential future revenue: If we save a slot, what's the average revenue we
        // get?
        double expectedFutureRev = history.stream()
                .mapToDouble(hp -> hp.revenue)
                .average()
                .orElse(0.0);

        double urgencyFactor = 1.0;

        // If current project has higher density than average, it's a high-value
        // opportunity
        if (currentDensity > avgDensity) {
            urgencyFactor += 0.2;
        }

        // Penalty for projects that can wait (Deadline > MAX_DAYS)
        // If it can wait, and it's not significantly better than an average project,
        // lower its priority
        if (p.deadline > 5) {
            if (p.revenue < expectedFutureRev * 1.5) {
                urgencyFactor -= 0.3; // Defer if not a "big win"
            }
        } else {
            urgencyFactor += 0.1; // Boost for projects that expire this week
        }

        return p.revenue * urgencyFactor;
    }

    public static List<Project> getHistoryFromDB() {
        List<Project> history = new ArrayList<>();
        String sql = "SELECT title, deadline, revenue FROM historical_projects";

        try (Connection con = DBConnection.getConnection();
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery(sql)) {

            while (rs.next()) {
                history.add(new Project(
                        0,
                        rs.getString("title"),
                        rs.getInt("deadline"),
                        rs.getInt("revenue")));
            }
        } catch (Exception e) {
            // Silently fail or log (maybe table doesn't exist yet)
        }
        return history;
    }
}
