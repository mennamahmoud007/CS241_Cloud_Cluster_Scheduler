import java.util.ArrayList;
public class SJFScheduler {
    private ArrayList<ScheduleEvent> schedule=new ArrayList<>();

    private void addEvent(String name, int startTime, int endTime) {
        if(startTime == endTime){
            return;
        }
        if (!schedule.isEmpty()) {

            ScheduleEvent lastEvent = schedule.get(schedule.size() - 1);
            if (lastEvent.getName().equals(name) && lastEvent.getEndTime() == startTime) {
                lastEvent.setEndTime(endTime);
                return;
            }
        }
        schedule.add(new ScheduleEvent(name, startTime, endTime));
    }

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
                int csstart=currentTime;
                currentTime+=contextSwitchTime;
                addEvent("CS", csstart, currentTime);
                currentProcess=selectShortestRemainingProcess(processes,currentTime);
            }
            
            if(currentProcess==null){
                addEvent("Idle", currentTime, currentTime + 1);
                currentTime++;
                continue;
            }

            if(currentProcess.getFirstStartTime()==-1){
                currentProcess.setFirstStartTime(currentTime);
            }
            previousProcess=currentProcess;
            addEvent(currentProcess.getProcessId(), currentTime, currentTime + 1);
            currentProcess.setRemainingTime(currentProcess.getRemainingTime()-1);
            currentTime++;
            if(currentProcess.getRemainingTime()==0){
                completed++;
                int csstart=currentTime;
                currentTime+=contextSwitchTime;
                addEvent("CS", csstart, currentTime);
                currentProcess.setCompletionTime(currentTime);
                previousProcess=null;
            }
            
        }
        GanttChart.print(schedule);
        Metrics.printResults(processes);

    }   

}