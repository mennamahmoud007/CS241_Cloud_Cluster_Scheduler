import java.util.ArrayList;

public class ACSScheduler {
    private final ArrayList<ScheduleEvent> schedule = new ArrayList<>();
    private final Tier2 tier2;
    private Tier1 tier1;

    public ACSScheduler() {
        this(Tier3.DEFAULT_AGING_INTERVAL);
    }

    public ACSScheduler(int agingInterval) {
        this.tier2 = new Tier2(new Tier3(agingInterval));
    }

    private void addEvent(String name, int startTime, int endTime) {
        if (startTime == endTime) {
            return;
        }
        if (!schedule.isEmpty()) {
            ScheduleEvent last = schedule.get(schedule.size() - 1);
            if (last.getName().equals(name) && last.getEndTime() == startTime) {
                last.setEndTime(endTime);
                return;
            }
        }
        schedule.add(new ScheduleEvent(name, startTime, endTime));
    }

    private void record(int time, String label, Process hidden) {
        tier1.admitArrivals(time);
        System.out.printf("%-6d %-8s %s | %s%n", time, label,
                tier1.snapshot(hidden), tier2.snapshot(time, hidden));
    }

    private int contextSwitch(int time, int cs) {
        for (int i = 0; i < cs; i++) {
            record(time + i, "CS", null);
        }
        addEvent("CS", time, time + cs);
        return time + cs;
    }

    public void run(ArrayList<Process> processes, int quantum, int contextSwitchTime) {
        if (processes == null || processes.isEmpty()) {
            throw new IllegalArgumentException("Process list cannot be null or empty");
        }
        if (quantum <= 0) {
            throw new IllegalArgumentException("Quantum must be positive");
        }
        if (contextSwitchTime < 0) {
            throw new IllegalArgumentException("Context switch time cannot be negative");
        }
        tier2.clear();
        schedule.clear();
        tier1 = new Tier1(quantum, processes);

        int total = processes.size();
        int completed = 0;
        int currentTime = 0;
        Process previous = null;

        System.out.println("\nDemotion Matrix (Q = " + quantum + ", CS = " + contextSwitchTime + "):");
        System.out.printf("%-6s %-8s %s%n", "Time", "Process", "Tier State");

        while (completed < total) {
            tier1.admitArrivals(currentTime);

            Process next;
            boolean fromTier1 = !tier1.isEmpty();
            if (fromTier1) {
                next = tier1.peek();
            } else {
                next = tier2.selectNext(currentTime);
            }

            if (next == null) {
                record(currentTime, "Idle", null);
                addEvent("Idle", currentTime, currentTime + 1);
                currentTime++;
                previous = null;
                continue;
            }

            int decisionTime = currentTime;
            if (previous != null && previous != next) {
                currentTime = contextSwitch(currentTime, contextSwitchTime);
                tier1.admitArrivals(currentTime);
                fromTier1 = !tier1.isEmpty();
                if (fromTier1) {
                    next = tier1.peek();
                } else {
                    next = tier2.selectNext(currentTime);
                }
            }

            int start = currentTime;
            int end = fromTier1 ? tier1.sliceEnd(next, start) : start + next.getRemainingTime();
            for (int t = start; t < end; t++) {
                record(t, next.getProcessId(), t == decisionTime ? null : next);
            }

            if (fromTier1) {
                tier1.poll();
                currentTime = tier1.runSlice(next, start);
                tier1.admitArrivals(currentTime);
                if (!next.isCompleted()) {
                    tier2.demote(next, currentTime);
                }
            } else {
                currentTime = tier2.execute(next, start);
            }
            addEvent(next.getProcessId(), start, currentTime);
            if (next.isCompleted()) {
                completed++;
                currentTime = contextSwitch(currentTime, contextSwitchTime);
                next.setCompletionTime(currentTime);
                previous = null;
            } else {
                previous = next;
            }
        }

        record(currentTime, "-", null);
        GanttChart.print(schedule);
        Metrics.printResults(processes);
    }
}
