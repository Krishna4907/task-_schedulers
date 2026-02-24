import java.util.List;

public class Scheduler {

    private static final int MAX_DAYS = 10; // Extended to 2 weeks

    public static void generateSchedule(List<Project> projects) {
        List<Project> history = PredictiveService.getHistoryFromDB();

        // 1. Pre-process: Filter out invalid data
        projects.removeIf(p -> p.deadline <= 0 || p.revenue <= 0);

        // 2. Predictive Analysis Sort (Job Sequencing by Value)
        // We evaluate every project's strategic value, which considers revenue,
        // deadlines, and history.
        // Sorting in descending order of value guarantees we attempt to fit the most
        // valuable items first.
        projects.sort((p1, p2) -> Double.compare(
                PredictiveService.calculateStrategicValue(p2, history),
                PredictiveService.calculateStrategicValue(p1, history)));

        // 3. Allocation via Backwards Slotting to strictly enforce deadlines
        // Array of slots 1 to 10 (MAX_DAYS). Index 0 is unused.
        Project[] scheduledSlots = new Project[MAX_DAYS + 1];

        for (Project p : projects) {
            int maxSlot = Math.min(p.deadline, MAX_DAYS);

            // Search backwards from its deadline down to day 1 to find the latest available
            // slot.
            // This optimally reserves earlier slots for projects with tighter deadlines!
            for (int j = maxSlot; j >= 1; j--) {
                if (scheduledSlots[j] == null) {
                    scheduledSlots[j] = p; // Lock the project into this slot
                    break;
                }
            }
        }

        // 4. Finalize and Output
        printSchedule(scheduledSlots, history);
    }

    private static void printSchedule(Project[] scheduledSlots, List<Project> history) {
        String[] days = { "Monday", "Tuesday", "Wednesday", "Thursday", "Friday" };
        int week1Revenue = 0;
        int week2ProjectsCount = 0;

        System.out.println("\n [OPTIMAL PREDICTIVE ALGORITHM] 2-WEEK SCHEDULE");
        System.out.println("--------------------------------------------------");

        System.out.println("\n === WEEK 1 (This Week) ===");
        for (int i = 1; i <= 5; i++) {
            printSlot(scheduledSlots[i], history, days[i - 1], i);
            if (scheduledSlots[i] != null) {
                week1Revenue += scheduledSlots[i].revenue;
            }
        }
        System.out.println("Week 1 Revenue: \u20B9" + week1Revenue);

        for (int i = 6; i <= 10; i++) {
            if (scheduledSlots[i] != null) {
                week2ProjectsCount++;
            }
        }

        System.out.println("\n === ANALYSIS & PREDICTION ===");

        double pastTotalRev = history.stream().mapToDouble(p -> p.revenue).sum();
        double pastWeeksCount = Math.max(1, Math.ceil(history.size() / 5.0));
        double pastWeekAvgRevenue = history.isEmpty() ? 0 : pastTotalRev / pastWeeksCount;

        if (history.isEmpty()) {
            System.out.println("   [Comparison] No historical projects available to compare.");
        } else {
            if (week1Revenue > pastWeekAvgRevenue) {
                System.out.println("   [Comparison] This week (\u20B9" + week1Revenue
                        + ") is BETTER than the historical weekly average (\u20B9"
                        + String.format("%.2f", pastWeekAvgRevenue) + ").");
            } else if (week1Revenue < pastWeekAvgRevenue) {
                System.out.println("   [Comparison] This week (\u20B9" + week1Revenue
                        + ") is WORSE than the historical weekly average (\u20B9"
                        + String.format("%.2f", pastWeekAvgRevenue) + ").");
            } else {
                System.out.println("   [Comparison] This week's revenue equals the historical weekly average (\u20B9"
                        + String.format("%.2f", pastWeekAvgRevenue) + ").");
            }
        }

        if (week2ProjectsCount > 0) {
            System.out.println(
                    "\n   [Next Week Prediction] We have deferred " + week2ProjectsCount + " project(s) to next week.");
        } else {
            System.out.println("\n   [Next Week Prediction] All slots for next week are currently open.");
            System.out.println(
                    "   -> We have full capacity to take on new, high-value urgent projects next week without risk to upcoming revenue.");
        }
    }

    private static void printSlot(Project p, List<Project> history, String dayName, int currentDay) {
        if (p != null) {
            double sv = PredictiveService.calculateStrategicValue(p, history);
            String tag;
            if (p.deadline == currentDay) {
                tag = " [Expires Today!]";
            } else if (p.deadline > 5) {
                tag = " [Deferred/Flexible]";
            } else {
                tag = " [Urgent]";
            }
            System.out.println(dayName + " \u2192 " + p.title +
                    " (Deadline: " + p.deadline + " Days, \u20B9" + p.revenue + ") [Score: " + String.format("%.2f", sv)
                    + "]" + tag);
        } else {
            System.out.println(dayName + " \u2192 No Project (Free Slot)");
        }
    }
}
