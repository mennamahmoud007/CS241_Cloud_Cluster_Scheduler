import java.util.ArrayList;
public class Metrics{

    public static int calculateTurnaroundTime(Process p) {
        return p.getCompletionTime() - p.getArrivalTime();
    }

    public static int calculateWaitingTime(Process p) {
        return calculateTurnaroundTime(p) - p.getBurstTime();
    }

    public static int calculateResponseTime(Process p) {
        return p.getFirstStartTime() - p.getArrivalTime();
    }
    public static double calculateAverageWaitingTime(ArrayList<Process>processes){
        if(processes.isEmpty()){
            return 0.0;
        }
        int totalWaitingTime = 0;
        int processCount=processes.size();
        for(int i=0;i<processCount;i++){
            totalWaitingTime+=calculateWaitingTime(processes.get(i));
        }
        return (double) totalWaitingTime/(processCount);
    }
    public static double calculateAverageTurnaroundTime(ArrayList<Process>processes){
        if(processes.isEmpty()){
            return 0.0;
        }
        int totalTurnaroundTime = 0;
        int processCount=processes.size();
        for(int i=0;i<processCount;i++){
            totalTurnaroundTime+=calculateTurnaroundTime(processes.get(i));
        }
        return (double) totalTurnaroundTime/(processCount);
    }

}