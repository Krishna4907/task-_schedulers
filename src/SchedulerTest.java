import java.util.ArrayList;
import java.util.List;

public class SchedulerTest {
    public static void main(String[] args) {
        List<Project> projects = new ArrayList<>();
        projects.add(new Project(1, "Task-A (D1)", 1, 100));
        projects.add(new Project(2, "Task-B (D1)", 1, 200));
        projects.add(new Project(3, "Task-C (D3)", 3, 50));
        projects.add(new Project(4, "Task-D (D10)", 10, 500));
        projects.add(new Project(5, "Task-E (D2)", 2, 70));
        projects.add(new Project(6, "Task-F (D100)", 100, 300));
        projects.add(new Project(7, "Task-G (D2)", 2, 120));
        projects.add(new Project(8, "Task-H (Negative)", 5, -50));
        projects.add(new Project(9, "Task-I (Deadline 0)", 0, 100));

        System.out.println("Running generic Scheduler test with filtering...");
        Scheduler.generateSchedule(projects);
    }
}
