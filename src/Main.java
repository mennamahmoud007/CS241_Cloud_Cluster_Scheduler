import java.util.ArrayList;
import java.util.Scanner;
import java.io.OutputStream;
import java.io.PrintStream;

public class Main {
    static Scanner in = new Scanner(System.in);
    public static void main(String[] args){
        while(true){
            System.out.println("\n--------- CPU Scheduling Simulator ---------\n");
            System.out.println("1. Preemptive SJF (SRTF)");
            System.out.println("2. Adaptive Container Scheduler (ACS)");
            System.out.println("3. Run Both Algorithms");
            System.out.println("4. Exit");
            System.out.println("5. Run Experiments (for the report)");
            int choice = readInt("\nChoose an option: ");

            try{
                switch(choice){
                    case 1: {
                        ArrayList<Process> processes = readProcesses(false);
                        int cs = readInt("Context Switching Time: ");
                        System.out.println("\n--------- Preemptive SJF (SRTF) ---------");
                        new SJFScheduler().run(copyAll(processes), cs);
                        break;
                    }
                    case 2: {
                        ArrayList<Process> processes = readProcesses(true);
                        int quantum = readInt("Time Quantum: ");
                        int cs = readInt("Context Switching Time: ");
                        System.out.println("\n--------- Adaptive Container Scheduler (ACS) ---------");
                        new ACSScheduler().run(copyAll(processes), quantum, cs);
                        break;
                    }
                    case 3: {
                        ArrayList<Process> processes = readProcesses(true);
                        int quantum = readInt("Time Quantum: ");
                        int cs = readInt("Context Switching Time: ");
                        System.out.println("\n--------- Preemptive SJF (SRTF) ---------");
                        new SJFScheduler().run(copyAll(processes), cs);
                        System.out.println("\n--------- Adaptive Container Scheduler (ACS) ---------");
                        new ACSScheduler().run(copyAll(processes), quantum, cs);
                        break;
                    }
                    case 4: {
                        System.out.println("Simulation Ended .. Goodbye");
                        return;
                    }
                    case 5: {
                        System.out.println("\n--------- Test Case 1 ---------");
                        runExperiments(testCase1());
                        runSJFExperiments(testCase1());
                        System.out.println("\n--------- Test Case 2 ---------");
                        runExperiments(testCase2());
                        runSJFExperiments(testCase2());
                        break;
                    }
                    default: System.out.println("Invalid option.");
                }
            }catch (IllegalArgumentException e) {
                System.out.println("Error: " + e.getMessage());
            }
        }
    }

    static ArrayList<Process> readProcesses(boolean readPriority) {
        int n = readInt("Number of processes: ");
        ArrayList<Process> list = new ArrayList<>();
        for (int i = 1; i <= n; i++) {
            System.out.print("Process " + i + " ID: ");
            String id = in.next();
            int arrival = readInt("Arrival Time: ");
            int burst = readInt("Burst Time: ");
            int priority = readPriority ? readInt("Initial Priority: ") : 0;
            list.add(new Process(id, arrival, burst, priority));
        }
        return list;
    }

    static ArrayList<Process> copyAll(ArrayList<Process> original) {
        ArrayList<Process> copies = new ArrayList<>();
        for (Process p : original) {
            copies.add(p.copy());
        }
        return copies;
    }

    static ArrayList<Process> testCase1() {
        ArrayList<Process> list = new ArrayList<>();
        list.add(new Process("P1", 0, 2, 2));
        list.add(new Process("P2", 1, 7, 3));
        list.add(new Process("P3", 3, 2, 1));
        return list;
    }

    static ArrayList<Process> testCase2() {
        ArrayList<Process> list = new ArrayList<>();
        list.add(new Process("P1", 0, 5, 4));
        list.add(new Process("P2", 1, 6, 2));
        list.add(new Process("P3", 4, 4, 3));
        return list;
    }

    static void runExperiments(ArrayList<Process> processes) {
        int[] quanta = {1, 2, 3, 4, 5};
        int[] switches = {0, 1, 2, 3};
        PrintStream real = System.out;

        System.out.println("\nACS Results");
        System.out.println("Q\tCS\tAWT\tATAT\tART");
        for (int cs : switches) {
            for (int q : quanta) {
                ArrayList<Process> run = copyAll(processes);
                System.setOut(new PrintStream(OutputStream.nullOutputStream()));
                new ACSScheduler().run(run, q, cs);
                System.setOut(real);
                System.out.println(q + "\t" + cs + "\t"
                    + Metrics.calculateAverageWaitingTime(run) + "\t"
                    + Metrics.calculateAverageTurnaroundTime(run) + "\t" 
                    + Metrics.calculateAverageResponseTime(run)) ;
            }
        }
    }

    static void runSJFExperiments(ArrayList<Process> processes) {
        int[] switches = {0, 1, 2, 3};
        PrintStream real = System.out;

        System.out.println("\nSJF Results");
        System.out.println("CS\tAWT\tATAT\tART");
        for (int cs : switches) {
            ArrayList<Process> run = copyAll(processes);
            System.setOut(new PrintStream(OutputStream.nullOutputStream()));
            new SJFScheduler().run(run, cs);
            System.setOut(real);
            System.out.println(cs + "\t"
                + Metrics.calculateAverageWaitingTime(run) + "\t"
                + Metrics.calculateAverageTurnaroundTime(run) + "\t"
                + Metrics.calculateAverageResponseTime(run));
        }
    }

    static int readInt(String message) {
        System.out.print(message);
        while (!in.hasNextInt()) {
            in.next();
            System.out.print("Enter a number: ");
        }
        return in.nextInt();
    }
}
