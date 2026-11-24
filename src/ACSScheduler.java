import java.util.ArrayList;

public class ACSScheduler {
    public static final int DEFAULT_AGING_INTERVAL = 4;

    private final ArrayList<Process> tier2Queue = new ArrayList<>();
    private final int agingInterval;

    public ACSScheduler() {
        this(DEFAULT_AGING_INTERVAL);
    }

    public ACSScheduler(int agingInterval) {
        if (agingInterval <= 0) {
            throw new IllegalArgumentException("Aging interval must be positive");
        }
        this.agingInterval = agingInterval;
    }

    public int getAgingInterval() {
        return agingInterval;
    }

    public int calculateEffectivePriority(Process p, int currentTime) {
        if (p.getTier2EntryTime() == -1) {
            return p.getInitialPriority();
        }
        int waitingTime = Math.max(0, currentTime - p.getTier2EntryTime());
        return p.getInitialPriority() - (waitingTime / agingInterval);
    }

    public void demoteToTier2(Process p, int currentTime) {
        if (p == null || p.isCompleted()) {
            return;
        }
        p.setTier2EntryTime(currentTime);
        if (!tier2Queue.contains(p)) {
            tier2Queue.add(p);
        }
    }

    public boolean isTier2Empty() {
        return tier2Queue.isEmpty();
    }

    public ArrayList<Process> getTier2Queue() {
        return new ArrayList<>(tier2Queue);
    }

    public Process selectNextTier2Process(int currentTime) {
        Process best = null;
        for (Process p : tier2Queue) {
            if (p.isCompleted()) {
                continue;
            }
            if (best == null || hasHigherPriority(p, best, currentTime)) {
                best = p;
            }
        }
        return best;
    }

    private boolean hasHigherPriority(Process a, Process b, int currentTime) {
        int pa = calculateEffectivePriority(a, currentTime);
        int pb = calculateEffectivePriority(b, currentTime);
        if (pa != pb) {
            return pa < pb;
        }
        if (a.getTier2EntryTime() != b.getTier2EntryTime()) {
            return a.getTier2EntryTime() < b.getTier2EntryTime();
        }
        return a.getArrivalTime() < b.getArrivalTime();
    }

    public int executeTier2Process(Process p, int startTime) {
        if (p == null || !tier2Queue.contains(p)) {
            throw new IllegalArgumentException("Process is not in Tier 2");
        }
        if (p.getFirstStartTime() == -1) {
            p.setFirstStartTime(startTime);
        }
        int endTime = startTime + p.getRemainingTime();
        p.setRemainingTime(0);
        p.setCompletionTime(endTime);
        tier2Queue.remove(p);
        return endTime;
    }

    public String tier2Snapshot(int currentTime) {
        if (tier2Queue.isEmpty()) {
            return "Tier 2: [Empty]";
        }
        StringBuilder sb = new StringBuilder("Tier 2: [");
        for (int i = 0; i < tier2Queue.size(); i++) {
            Process p = tier2Queue.get(i);
            if (i > 0) {
                sb.append(", ");
            }
            sb.append(p.getProcessId())
              .append(" (remaining: ").append(p.getRemainingTime())
              .append(", Priority: ").append(calculateEffectivePriority(p, currentTime))
              .append(")");
        }
        return sb.append("]").toString();
    }
}