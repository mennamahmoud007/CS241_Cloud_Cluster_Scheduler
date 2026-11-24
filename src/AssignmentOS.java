import java.util.ArrayList;
public class AssignmentOS{

    public static void main (String[]args){
        Process p1 = new Process("P1",0,2,1);
        Process p2 = new Process("P2", 8, 3, 2);

        ArrayList<Process> processes = new ArrayList<>();
        processes.add(p1);
        processes.add(p2);
    
        SJFScheduler scheduler = new SJFScheduler();
        scheduler.run(processes, 1);
    }
}
