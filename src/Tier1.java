import java.util.Queue;
import java.util.LinkedList;
import java.util.List;
import java.util.ArrayList;
import java.util.Comparator;

public class Tier1 {
    private Queue<Process> readyQueue;
    private List<Process> allProcesses;
    private int currentTime;
    private ACSScheduler tier2Scheduler;
    private final int quantum;
    private int nextprocessIndex=0;

    public Tier1(int quantum,ACSScheduler tier2AcsScheduler){
        this.quantum=quantum;
        this.tier2Scheduler=tier2AcsScheduler;
        this.currentTime=0;
        this.readyQueue = new LinkedList<Process>();
        this.allProcesses =new ArrayList<Process>();
    }
    public void addProcess(Process p){
        allProcesses.add(p);
    }

    private void addArrivedProcesses(){
        while(nextprocessIndex < allProcesses.size()){
            Process p = allProcesses.get(nextprocessIndex);
            if(p.getArrivalTime()<=currentTime){
                readyQueue.add(p);
                nextprocessIndex++;
            }
            else{
                break;
            }
        }
    }
    
    public void runTier1(){
         allProcesses.sort(Comparator.comparingInt(Process::getArrivalTime));
         while(nextprocessIndex<allProcesses.size()|| !readyQueue.isEmpty()){
            addArrivedProcesses();

            if(readyQueue.isEmpty()){
                if(!tier2Scheduler.isTier2Empty())
                    break;
                currentTime=allProcesses.get(nextprocessIndex).getArrivalTime();
                addArrivedProcesses();
            }
            Process p =readyQueue.poll();
            if(p.getFirstStartTime()==-1)
                p.setFirstStartTime(currentTime);

            int exuecutionTime = Math.min(p.getRemainingTime(),quantum);
            p.setRemainingTime(p.getRemainingTime()- exuecutionTime);
            currentTime += exuecutionTime;
            addArrivedProcesses();
            if(p.isCompleted())
                p.setCompletionTime(currentTime);
            else{
                tier2Scheduler.demoteToTier2(p, currentTime);
            }
         }
    }
    public int getCurrentTime(){
        return currentTime;
    }
    public void updateCurrentTime(int time){
        if(time> currentTime)
            currentTime=time;
    }
}
