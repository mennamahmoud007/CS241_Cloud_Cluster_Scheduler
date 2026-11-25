import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.Queue;

public class Tier1 {
    private final Queue<Process> readyQueue = new LinkedList<>();
    private final ArrayList<Process> allProcesses;
    private final int quantum;
    private int nextProcessIndex = 0;

    public Tier1(int quantum, ArrayList<Process> processes) {
        if (quantum <= 0) {
            throw new IllegalArgumentException("Quantum must be positive");
        }
        if (processes == null || processes.isEmpty()) {
            throw new IllegalArgumentException("Process list cannot be null or empty");
        }
        this.quantum = quantum;
        this.allProcesses = new ArrayList<>(processes);
        this.allProcesses.sort(Comparator.comparingInt(Process::getArrivalTime));
    }

    public int getQuantum() {
        return quantum;
    }

    public void admitArrivals(int currentTime) {
        while (nextProcessIndex < allProcesses.size()
                && allProcesses.get(nextProcessIndex).getArrivalTime() <= currentTime) {
            readyQueue.add(allProcesses.get(nextProcessIndex));
            nextProcessIndex++;
        }
    }

    public boolean isEmpty() {
        return readyQueue.isEmpty();
    }

    public Process peek() {
        return readyQueue.peek();
    }

    public Process poll() {
        return readyQueue.poll();
    }

    public int sliceEnd(Process p, int startTime) {
        return startTime + Math.min(p.getRemainingTime(), quantum);
    }

    public int runSlice(Process p, int startTime) {
        if (p.getFirstStartTime() == -1) {
            p.setFirstStartTime(startTime);
        }
        int executed = Math.min(p.getRemainingTime(), quantum);
        p.setRemainingTime(p.getRemainingTime() - executed);
        int endTime = startTime + executed;
        // if (p.isCompleted()) {
        //     p.setCompletionTime(endTime);
        // }
        return endTime;
    }

    public String snapshot(Process hidden) {
        StringBuilder sb = new StringBuilder("Tier 1: [");
        boolean first = true;
        for (Process p : readyQueue) {
            if (p == hidden) {
                continue;
            }
            if (!first) {
                sb.append(", ");
            }
            first = false;
            sb.append(p.getProcessId())
              .append(" (remaining: ").append(p.getRemainingTime())
              .append(", Priority: ").append(p.getInitialPriority())
              .append(")");
        }
        if (first) {
            return "Tier 1: [Empty]";
        }
        return sb.append("]").toString();
    }
}
