import java.util.List;
import java.util.Scanner;

public class Main {

    public static void main(String[] args) {

        Scanner sc = new Scanner(System.in);
        ProjectDAO dao = new ProjectDAO();

        while (true) {
            System.out.println("\n ProManage Solutions ");
            System.out.println("1. Add Project");
            System.out.println("2. View Projects");
            System.out.println("3. Generate Weekly Schedule");
            System.out.println("4. Mark Project as Completed");
            System.out.println("5. Exit");
            System.out.print("Choose option: ");

            int choice = sc.nextInt();
            sc.nextLine();

            switch (choice) {

                case 1:
                    System.out.print("Enter project title: ");
                    String title = sc.nextLine();

                    System.out.print("Enter deadline  : ");
                    int deadline = sc.nextInt();

                    System.out.print("Enter revenue: ");
                    int revenue = sc.nextInt();

                    dao.addProject(title, deadline, revenue);
                    break;

                case 2:
                    List<Project> projects = dao.getAllProjects();
                    System.out.println("\n📋 PROJECT LIST");
                    for (Project p : projects) {
                        System.out.println(p.id + " | " + p.title +
                                " | Deadline: " + p.deadline +
                                " | Revenue: ₹" + p.revenue);
                    }
                    break;

                case 3:
                    Scheduler.generateSchedule(dao.getAllProjects());
                    break;

                case 4:
                    System.out.println("\n📋 PROJECT LIST FOR COMPLETION");
                    for (Project p : dao.getAllProjects()) {
                        System.out.println(
                                p.id + " | " + p.title + " (Deadline: " + p.deadline + ", Revenue: " + p.revenue + ")");
                    }
                    System.out.println("--------------------------------");
                    System.out.print("Enter Project ID to complete: ");
                    int id = sc.nextInt();
                    dao.completeProject(id);
                    break;

                case 5:
                    System.out.println("Exiting system...");
                    System.exit(0);

                default:
                    System.out.println(" Invalid option!");
            }
        }
    }
}
