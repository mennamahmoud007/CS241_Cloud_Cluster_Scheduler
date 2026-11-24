import java.util.ArrayList;
public class SJFScheduler {

    public Process selectShortestRemainingProcess(ArrayList<Process> processes, int currentTime) {
        Process shortest = null;
        for (Process p : processes) {
            if (p != null && p.getRemainingTime() > 0 && p.getArrivalTime() <= currentTime) {
                if (shortest == null || p.getRemainingTime() < shortest.getRemainingTime()) {
                    shortest = p;
                }
            }
        }
        return shortest;
    }

    public void run(ArrayList<Process> processes, int contextSwitchTime) {
        
        if (processes == null || processes.isEmpty()) {
            throw new IllegalArgumentException("Process list cannot be null or empty");
        }
        if (contextSwitchTime < 0) {
            throw new IllegalArgumentException("Context switch time cannot be negative");
        }
        int currentTime=0;
        int allProcesses=processes.size();
        int completed=0;
        Process previousProcess = null;

        while(completed<allProcesses){
            Process currentProcess=selectShortestRemainingProcess(processes,currentTime);
            
            if(previousProcess!=null && previousProcess != currentProcess){
                currentTime+=contextSwitchTime;
                currentProcess=selectShortestRemainingProcess(processes,currentTime);
            }
            
            if(currentProcess==null){
                currentTime++;
                continue;
            }

            if(currentProcess.getFirstStartTime()==-1){
                currentProcess.setFirstStartTime(currentTime);
            }
            previousProcess=currentProcess;
            currentProcess.setRemainingTime(currentProcess.getRemainingTime()-1);
            currentTime++;
            if(currentProcess.getRemainingTime()==0){
                completed++;
                currentTime+=contextSwitchTime;
                currentProcess.setCompletionTime(currentTime);
                previousProcess=null;
            }
            
        }

    }   

}