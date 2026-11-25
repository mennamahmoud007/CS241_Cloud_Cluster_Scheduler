import java.util.ArrayList;
import java.util.Scanner;

public class Main {
    static Scanner in = new Scanner(System.in);
    public static void main(String[] args){
        while(true){
            System.out.println("\n--------- CPU Scheduling Simulator ---------\n");
            System.out.println("1. Preemptive SJF (SRTF)");
            System.out.println("2. Adaptive Container Scheduler (ACS)");
            System.out.println("3. Run Both Algorithms");
            System.out.println("4. Exit");
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

    static int readInt(String message) {
        System.out.print(message);
        while (!in.hasNextInt()) {
            in.next();
            System.out.print("Enter a number: ");
        }
        return in.nextInt();
    }
}
