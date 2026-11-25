import java.util.ArrayList;
public class Metrics{

    public static int calculateTurnaroundTime(Process p) {
        return p.getCompletionTime() - p.getArrivalTime();
    }

    public static int calculateWaitingTime(Process p) {
        return calculateTurnaroundTime(p) - p.getBurstTime();
    }

    public static int calculateResponseTime(Process p) {  // first response time
        return p.getFirstStartTime() - p.getArrivalTime();
    }

    public static double calculateAverageWaitingTime(ArrayList<Process>processes){
        if(processes.isEmpty()){
            return 0.0;
        }

        int totalWaitingTime = 0;
        int processCount = processes.size();

        for(int i = 0; i < processCount; i++){
            totalWaitingTime += calculateWaitingTime(processes.get(i));
        }

        return (double) totalWaitingTime/(processCount);
    }

    public static double calculateAverageTurnaroundTime(ArrayList<Process>processes){
        if(processes.isEmpty()){
            return 0.0;
        }

        int totalTurnaroundTime = 0;
        int processCount = processes.size();

        for(int i = 0; i < processCount; i++){
            totalTurnaroundTime += calculateTurnaroundTime(processes.get(i));
        }
        return (double) totalTurnaroundTime/(processCount);
    }

    public static double calculateAverageResponseTime(ArrayList<Process> processes){
        if(processes.isEmpty()){
            return 0.0;
        }

        int totalResponseTime = 0;
        int processCount = processes.size();

        for(int i = 0; i < processCount; i++){
            totalResponseTime += calculateResponseTime(processes.get(i));
        }
        return (double) totalResponseTime/(processCount);
    }

    public static void printResults(ArrayList<Process> processes) {
        System.out.println("\nScheduling Results:");
        for (Process p : processes) {
            System.out.println("\nProcess: " + p.getProcessId());
            System.out.println("Waiting Time: " + calculateWaitingTime(p));
            System.out.println("Turnaround Time: " + calculateTurnaroundTime(p));
            System.out.println("Response Time: " + calculateResponseTime(p));
        }
        System.out.printf("\nAverage Waiting Time: %.4f%n" , calculateAverageWaitingTime(processes));
        System.out.printf("Average Turnaround Time: %.4f%n" , calculateAverageTurnaroundTime(processes));
        System.out.printf("Average Response Time: %.4f%n" , calculateAverageResponseTime(processes));
    }

}