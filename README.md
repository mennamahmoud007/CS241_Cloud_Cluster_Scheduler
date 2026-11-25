# Cloud-Cluster CPU Scheduling Simulator

**Course:** CS241 – Operating Systems 1, Faculty of Computers and Artificial Intelligence, Cairo University
**Assignment:** 2 – Cloud-Cluster CPU Scheduling (Fall 2026)
**Language:** Java

A Java simulator for a node running mixed containerized workloads: CPU-heavy batch jobs (e.g. model training) and latency-sensitive interactive jobs (e.g. a web server). It implements two schedulers, accounts for context-switch (CS) time, and reports waiting, turnaround and response times.

---

## Schedulers

### 1. Preemptive Shortest-Job First (SRTF)

- At every time unit the process with the shortest remaining time runs.
- A context switch is inserted whenever the CPU moves to a different process.
- Output: Gantt chart (including `CS` and `Idle` blocks) and per-process metrics.

### 2. Adaptive Container Scheduler (ACS)

A three-tier scheduler.

| Tier | Class | Policy | Behaviour |
|------|-------|--------|-----------|
| **Tier 1** | `Tier1` | Round Robin (static quantum `Q`) | Every new process starts here, which keeps interactive jobs responsive. |
| **Tier 2** | `Tier2` | Non-preemptive Priority | A process that uses its whole quantum without finishing is flagged as a batch job and demoted here. Once it starts running it cannot be preempted until it completes. Tier 2 runs only when Tier 1 is empty. A lower number means a higher priority. |
| **Tier 3** | `Tier3` | Aging | While a process waits in Tier 2 its priority improves, which prevents starvation. |

`ACSScheduler` only coordinates the tiers: it owns the time loop, context switches, the Gantt chart and the demotion matrix.

**Aging formula** (`Tier3.calculateEffectivePriority`):

```
effectivePriority = initialPriority - floor((currentTime - tier2EntryTime) / agingInterval)
```

- `tier2EntryTime` is the time the process was demoted (when its quantum expired, before the CS).
- `agingInterval` defaults to **4** (`Tier3.DEFAULT_AGING_INTERVAL`) and is configurable.
- Tier 2 tie-breaking: lower effective priority, then earlier Tier 2 entry time, then earlier arrival time.

**Scheduling rules used by `ACSScheduler.run`:**

- Tier 1 always has priority over Tier 2.
- The next process is chosen at the decision time, before the CS. Aging is evaluated at that time, as in the assignment's Test Case 2.
- A CS of `contextSwitchTime` units is inserted whenever the CPU moves to a different process (including after a process completes, and between tiers). It is not counted in the finishing process's completion time.
- No CS for the first process, when the same process continues, or after an `Idle` period.
- A process arriving during a CS waits for the next scheduling decision.

---

## Project Structure

```
.
├── src/
│   ├── AssignmentOS.java    # Entry point (currently runs a small SJF example)
│   ├── Process.java         # Shared process model
│   ├── ScheduleEvent.java   # One Gantt block (process / CS / Idle)
│   ├── GanttChart.java      # Prints the Gantt chart
│   ├── Metrics.java         # Waiting / turnaround / response time + averages
│   ├── SJFScheduler.java    # Preemptive SJF
│   ├── Tier1.java           # ACS Tier 1: Round Robin queue and quantum slices
│   ├── Tier2.java           # ACS Tier 2: non-preemptive priority queue
│   ├── Tier3.java           # ACS Tier 3: aging policy
│   └── ACSScheduler.java    # ACS run loop: tiers, CS, Gantt, demotion matrix
├── lib/
├── bin/                     # Compiled output
└── README.md
```

### Class overview

| Class | Responsibility |
|-------|----------------|
| `Process` | ID, arrival, burst, remaining time, initial priority, first-start / completion / Tier 2 entry times. Has `copy()` and a copy constructor (resets runtime state). Validates input (non-blank ID, arrival ≥ 0, burst > 0). |
| `ScheduleEvent` | Name, start and end time of one Gantt block. |
| `GanttChart` | Prints the blocks and their time boundaries. |
| `Metrics` | `waiting = turnaround - burst`, `turnaround = completion - arrival`, `response = firstStart - arrival`, plus averages and a result printer. |
| `SJFScheduler` | `run(ArrayList<Process>, int contextSwitchTime)`: simulation, Gantt chart and metrics. |
| `Tier1` | Sorts processes by arrival, admits arrivals, serves them FIFO, runs one quantum slice per call, and rejects `Q <= 0`. |
| `Tier2` | Demotion, selection, non-preemptive execution and snapshot of the batch queue. |
| `Tier3` | Aging: effective priority from waiting time. |
| `ACSScheduler` | `run(processes, quantum, contextSwitchTime)` ties everything together. |

### API summary

**`Tier1`**

| Method | Description |
|--------|-------------|
| `Tier1(quantum, processes)` | Validates the quantum and sorts a copy of the processes by arrival. |
| `admitArrivals(time)` | Moves processes with `arrival <= time` into the ready queue. |
| `peek()` / `poll()` / `isEmpty()` | Queue access. |
| `sliceEnd(p, start)` / `runSlice(p, start)` | End time of one quantum slice, and executing it (sets first-start and completion time). |
| `snapshot(hidden)` | `Tier 1: [P2 (remaining: 6, Priority: 2)]` or `Tier 1: [Empty]`. |

**`Tier2`**

| Method | Description |
|--------|-------------|
| `Tier2(Tier3 aging)` | Uses the given aging policy for priorities. |
| `demote(p, time)` | Records the Tier 2 entry time and queues the process (no duplicates). |
| `selectNext(time)` | Best effective priority at `time`, or `null`. |
| `execute(p, start)` | Runs to completion (non-preemptive), sets first-start and completion time, removes it, returns the end time. |
| `snapshot(time, hidden)` | `Tier 2: [P1 (remaining: 2, Priority: 1), ...]` or `Tier 2: [Empty]`. |
| `isEmpty()` / `getQueue()` / `clear()` | Queue helpers. |

**`Tier3`**

| Method | Description |
|--------|-------------|
| `Tier3()` / `Tier3(agingInterval)` | Default interval 4, or a custom positive value. |
| `calculateEffectivePriority(p, time)` | Applies the aging formula. |

**`ACSScheduler`**

| Method | Description |
|--------|-------------|
| `ACSScheduler()` / `ACSScheduler(agingInterval)` | Default or custom aging interval. |
| `run(processes, quantum, contextSwitchTime)` | Runs the simulation and prints the demotion matrix, Gantt chart and metrics. Rejects an empty list, `Q <= 0` and `CS < 0`. |

---

## Build and Run

Requires JDK 11 or newer.

```bash
javac -d bin src/*.java
java -cp bin AssignmentOS
```

SJF example:

```java
ArrayList<Process> processes = new ArrayList<>();
processes.add(new Process("P1", 0, 2, 1));   // id, arrival, burst, priority
processes.add(new Process("P2", 8, 3, 2));

new SJFScheduler().run(processes, 1);        // context switch = 1
```

ACS example (Test Case 2):

```java
ArrayList<Process> processes = new ArrayList<>();
processes.add(new Process("P1", 0, 5, 4));
processes.add(new Process("P2", 1, 6, 2));
processes.add(new Process("P3", 4, 4, 3));

new ACSScheduler().run(processes, 3, 1);     // quantum = 3, context switch = 1
```

Create fresh `Process` objects (or use `copy()`) for every run, since a run changes their state.

---

## Output

For ACS, `run` prints:

1. **Demotion matrix**, one row per time unit, with the running process (or `CS` / `Idle`) and the contents of Tier 1 and Tier 2 with remaining time and effective priority. The process chosen at a decision point is still shown in its queue on that row.
2. **Gantt chart** with `CS` and `Idle` blocks.
3. **Per-process metrics** (waiting, turnaround, response) and the averages.

Excerpt for Test Case 2:

```
Time   Process  Tier State
3      CS       Tier 1: [P2 (remaining: 6, Priority: 2)] | Tier 2: [P1 (remaining: 2, Priority: 4)]
4      P2       Tier 1: [P3 (remaining: 4, Priority: 3)] | Tier 2: [P1 (remaining: 2, Priority: 4)]
...
| P1 | CS | P2 | CS | P3 | CS | P2 | CS | P1 | CS | P3 |
0    3    4    7    8    11   12   15   16   18   19   20
```

---

## Test Cases and Expected Results

Taken from the assignment sheet. The ACS implementation reproduces these results.

**Test Case 1** (Q = 3, CS = 0)

| Process | Arrival | Burst | Priority |
|---------|---------|-------|----------|
| P1 | 0 | 4 | 2 |
| P2 | 1 | 7 | 3 |
| P3 | 3 | 2 | 1 |

| Process | Finish | Turnaround | Waiting |
|---------|--------|------------|---------|
| P1 | 9 | 9 | 5 |
| P2 | 13 | 12 | 5 |
| P3 | 8 | 5 | 3 |

Average turnaround = 8.67, average waiting = 4.33.

**Test Case 2** (Q = 3, CS = 1)

| Process | Arrival | Burst | Priority |
|---------|---------|-------|----------|
| P1 | 0 | 5 | 4 |
| P2 | 1 | 6 | 2 |
| P3 | 4 | 4 | 3 |

| Process | Finish | Turnaround | Waiting |
|---------|--------|------------|---------|
| P1 | 18 | 18 | 13 |
| P2 | 15 | 14 | 8 |
| P3 | 20 | 16 | 12 |

Average turnaround = 16.00, average waiting = 11.00.

---

## Report (PDF) Requirements

1. For each test case, run several CS and Q values and plot their effect on average waiting and turnaround time.
2. Discuss the trade-offs of ACS Tier 2 demotion: CPU utilization versus response time.

---

## Team Responsibilities

| Member | Responsibility | Deliverables |
|--------|----------------|--------------|
| 1 | Preemptive SJF | `SJFScheduler.java`, SJF testing, Gantt chart validation |
| 2 | ACS Tier 1 | `Tier1.java`: Round Robin, quantum handling, demotion |
| 3 | ACS Tier 2 and Tier 3 | `Tier2.java` (non-preemptive priority) and `Tier3.java` (aging) |
| 4 | Shared model and metrics | `Process.java`, metric calculations, process-copy validation |
| 5 | Main and integration | `Main.java`, integration (`ACSScheduler.run`), test cases, experiments, graphs, report |

---

## Status

- [x] Preemptive SJF
- [x] ACS Tier 1 (Round Robin), Tier 2 (non-preemptive priority) and Tier 3 (aging)
- [x] ACS run loop with context switches, Gantt chart and per-step demotion matrix, verified on Test Cases 1 and 2
- [ ] `Main.java`, experiments with different Q and CS values, graphs, PDF report
- [ ] Bonus: GUI

## Notes

- `SJFScheduler` adds the CS after a process completes and includes it in that process's completion time, while the ACS test cases exclude it. Align the two before comparing results in the report.
- The assignment's sample tables show a few priority values that do not follow the aging rule (Test Case 1 at t = 8, Test Case 2 at t = 4 and t = 7). The schedules and final results are unaffected.
- `Process.getEffectivePriority` (fixed interval of 4) is no longer used by ACS; `Tier3` is the single source of aging logic.
