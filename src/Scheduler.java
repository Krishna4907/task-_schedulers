import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class Scheduler {

    private static final int MAX_DAYS = 5;

    static class ScheduleState {
        double strategicValue;
        int actualRevenue;
        List<Project> projects;

        ScheduleState() {
            this.strategicValue = 0;
            this.actualRevenue = 0;
            this.projects = new ArrayList<>();
        }

        ScheduleState(double strategicValue, int actualRevenue, List<Project> projects) {
            this.strategicValue = strategicValue;
            this.actualRevenue = actualRevenue;
            this.projects = new ArrayList<>(projects);
        }
    }

    public static void generateSchedule(List<Project> projects) {
        List<Project> history = PredictiveService.getHistoryFromDB();

        // Filter invalid projects
        projects.removeIf(p -> p.deadline <= 0 || p.revenue <= 0);

        // Sort by deadline to satisfy constraints, then by revenue
        projects.sort(Comparator.comparingInt((Project p) -> p.deadline)
                .thenComparing(p -> p.revenue, Comparator.reverseOrder()));

        ScheduleState[] dp = new ScheduleState[MAX_DAYS + 1];

        for (int i = 0; i <= MAX_DAYS; i++) {
            dp[i] = new ScheduleState();
        }

        for (Project p : projects) {
            double strategicValue = PredictiveService.calculateStrategicValue(p, history);
            int maxK = Math.min(p.deadline, MAX_DAYS);

            for (int k = maxK; k >= 1; k--) {
                double potentialValue = dp[k - 1].strategicValue + strategicValue;

                if (potentialValue > dp[k].strategicValue) {
                    List<Project> newSet = new ArrayList<>(dp[k - 1].projects);
                    newSet.add(p);
                    dp[k] = new ScheduleState(potentialValue, dp[k - 1].actualRevenue + p.revenue, newSet);
                }
            }
        }

        ScheduleState optimalState = dp[0];
        for (int k = 1; k <= MAX_DAYS; k++) {
            if (dp[k].strategicValue > optimalState.strategicValue) {
                optimalState = dp[k];
            }
        }

        printSchedule(optimalState.projects, history);
    }

    private static void printSchedule(List<Project> selectedProjects, List<Project> history) {
        selectedProjects.sort(Comparator.comparingInt(p -> p.deadline));

        String[] days = { "Monday", "Tuesday", "Wednesday", "Thursday", "Friday" };
        int totalRevenue = 0;

        System.out.println("\n PREDICTIVE WEEKLY PROJECT SCHEDULE");
        System.out.println("------------------------------------");

        for (int i = 0; i < MAX_DAYS; i++) {
            if (i < selectedProjects.size()) {
                Project p = selectedProjects.get(i);
                double sv = PredictiveService.calculateStrategicValue(p, history);
                String tag = (p.deadline > MAX_DAYS) ? " [Deferred Potential]" : " [Urgent]";

                System.out.println(days[i] + " \u2192 " + p.title +
                        " (Deadline: " + p.deadline + ", \u20B9" + p.revenue + ") [Score: " + String.format("%.2f", sv)
                        + "]" + tag);
                totalRevenue += p.revenue;
            } else {
                System.out.println(days[i] + " \u2192 No Project (Optimal Slot Saved)");
            }
        }

        System.out.println("\n\uD83D\uDCB0 Total Actual Revenue: \u20B9" + totalRevenue);
        System.out.println("\uD83D\uDCC8 Predictive Analysis: Adjusted for deadline edge cases and historical trends.");
    }
}
