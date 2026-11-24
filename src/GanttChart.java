import java.util.ArrayList;
public class GanttChart {

    public static void print(ArrayList<ScheduleEvent> schedule) {

        System.out.println("\nGantt Chart:");

        for (ScheduleEvent event : schedule) {
            System.out.printf("| %-8s", event.getName());
        }

        System.out.println("|");

        if (!schedule.isEmpty()) {
            System.out.printf("%-10d",
                schedule.get(0).getStartTime());

            for (ScheduleEvent event : schedule) {
                System.out.printf("%-10d", event.getEndTime());
            }
        }

        System.out.println();
    }
}
