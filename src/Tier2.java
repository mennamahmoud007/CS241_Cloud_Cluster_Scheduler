import java.util.ArrayList;

public class Tier2 {
    private final ArrayList<Process> queue = new ArrayList<>();
    private final Tier3 aging;

    public Tier2(Tier3 aging) {
        if (aging == null) {
            throw new IllegalArgumentException("Aging policy cannot be null");
        }
        this.aging = aging;
    }

    public void clear() {
        queue.clear();
    }

    public boolean isEmpty() {
        return queue.isEmpty();
    }

    public ArrayList<Process> getQueue() {
        return new ArrayList<>(queue);
    }

    public void demote(Process p, int currentTime) {
        if (p == null || p.isCompleted()) {
            return;
        }
        p.setTier2EntryTime(currentTime);
        if (!queue.contains(p)) {
            queue.add(p);
        }
    }

    public Process selectNext(int currentTime) {
        Process best = null;
        for (Process p : queue) {
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
        int pa = aging.calculateEffectivePriority(a, currentTime);
        int pb = aging.calculateEffectivePriority(b, currentTime);
        if (pa != pb) {
            return pa < pb;
        }
        if (a.getTier2EntryTime() != b.getTier2EntryTime()) {
            return a.getTier2EntryTime() < b.getTier2EntryTime();
        }
        return a.getArrivalTime() < b.getArrivalTime();
    }

    public int execute(Process p, int startTime) {
        if (p == null || !queue.contains(p)) {
            throw new IllegalArgumentException("Process is not in Tier 2");
        }
        if (p.getFirstStartTime() == -1) {
            p.setFirstStartTime(startTime);
        }
        int endTime = startTime + p.getRemainingTime();
        p.setRemainingTime(0);
        p.setCompletionTime(endTime);
        queue.remove(p);
        return endTime;
    }

    public String snapshot(int currentTime, Process hidden) {
        StringBuilder sb = new StringBuilder("Tier 2: [");
        boolean first = true;
        for (Process p : queue) {
            if (p == hidden) {
                continue;
            }
            if (!first) {
                sb.append(", ");
            }
            first = false;
            sb.append(p.getProcessId())
              .append(" (remaining: ").append(p.getRemainingTime())
              .append(", Priority: ").append(aging.calculateEffectivePriority(p, currentTime))
              .append(")");
        }
        if (first) {
            return "Tier 2: [Empty]";
        }
        return sb.append("]").toString();
    }
}
