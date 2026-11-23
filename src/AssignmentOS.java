import java.util.ArrayList;
public class AssignmentOS{

public static void main (String[]args){
    Process p1 = new Process("P1",1,3,2);
    p1.setFirstStartTime(2);
    p1.setCompletionTime(8);
    Process p2 = new Process("P2", 2, 3, 3);

    p2.setFirstStartTime(3);
    p2.setCompletionTime(9);

    ArrayList<Process> processes = new ArrayList<>();
    processes.add(p1);
    processes.add(p2);

    System.out.println(Metrics.calculateAverageWaitingTime(processes));
    System.out.println(Metrics.calculateAverageTurnaroundTime(processes));
   
 
    

}
}