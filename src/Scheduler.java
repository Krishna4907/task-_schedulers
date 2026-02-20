import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.PriorityQueue;

public class Scheduler {

    private static final int MAX_DAYS = 5;

    public static void generateSchedule(List<Project> projects) {
        List<Project> history = PredictiveService.getHistoryFromDB();

        // 1. Pre-process: Filter out invalid data
        projects.removeIf(p -> p.deadline <= 0 || p.revenue <= 0);

        // Sort by deadline first (chronological order)
        // If deadlines are the same, order by highest strategic score first
        projects.sort(Comparator.comparingInt((Project p) -> p.deadline)
                .thenComparing(p -> PredictiveService.calculateStrategicValue(p, history), Comparator.reverseOrder()));

        // 2. Priority Queue (Min-Heap based on Strategic Score)
        // This keeps track of our "Best Projects" selected so far.
        // It always leaves the "WEAKEST" project at the front.
        PriorityQueue<Project> selectionHeap = new PriorityQueue<>(
                Comparator.comparingDouble(p -> PredictiveService.calculateStrategicValue(p, history)));

        for (Project p : projects) {
            double pScore = PredictiveService.calculateStrategicValue(p, history);
            int effectiveDeadline = Math.min(p.deadline, MAX_DAYS);

            if (selectionHeap.size() < effectiveDeadline) {
                // If we still have an open slot before this project's deadline, safely add it.
                selectionHeap.add(p);
            } else if (!selectionHeap.isEmpty()) {
                // If slots are full up to this deadline, check our weakest selected project
                Project weakest = selectionHeap.peek();
                double weakestScore = PredictiveService.calculateStrategicValue(weakest, history);

                // If this new project is strategically better than the weakest one we saved...
                if (pScore > weakestScore) {
                    // Kick out the weaker project and keep this better one!
                    selectionHeap.poll();
                    selectionHeap.add(p);
                }
            }
        }

        // 3. Finalize: Extract best 5 and print
        List<Project> finalProjects = new ArrayList<>(selectionHeap);
        printSchedule(finalProjects, history);
    }

    private static void printSchedule(List<Project> selectedProjects, List<Project> history) {
        // Sort final selection by deadline so they print nicely from Monday to Friday
        selectedProjects.sort(Comparator.comparingInt(p -> p.deadline));

        String[] days = { "Monday", "Tuesday", "Wednesday", "Thursday", "Friday" };
        int totalRevenue = 0;

        System.out.println("\n [OPTIMAL HEAP] PREDICTIVE WEEKLY SCHEDULE");
        System.out.println("-------------------------------------------");

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
        System.out.println("\uD83D\uDCC8 Optimized using Chronological Priority Selection.");
    }
}
