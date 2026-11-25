public class Tier3 {
    public static final int DEFAULT_AGING_INTERVAL = 4;

    private final int agingInterval;

    public Tier3() {
        this(DEFAULT_AGING_INTERVAL);
    }

    public Tier3(int agingInterval) {
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
}
