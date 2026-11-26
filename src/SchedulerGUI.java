import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashSet;

public class SchedulerGUI extends JFrame {
    private static final String SJF = "Preemptive SJF";
    private static final String ACS = "ACS";
    private static final String BOTH = "Both";
    private static final String[] COLUMNS = {"Process ID", "Arrival", "Burst", "Priority"};

    private final DefaultTableModel tableModel = new DefaultTableModel(COLUMNS, 0);
    private final JTable table = new JTable(tableModel);
    private final JComboBox<String> algorithmBox = new JComboBox<>(new String[]{SJF, ACS, BOTH});
    private final JSpinner quantumSpinner = new JSpinner(new SpinnerNumberModel(3, 1, 1000, 1));
    private final JSpinner contextSwitchSpinner = new JSpinner(new SpinnerNumberModel(1, 0, 1000, 1));
    private final JTextArea outputArea = new JTextArea();

    public SchedulerGUI() {
        super("CPU Scheduling Simulator");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout(8, 8));

        add(buildSettingsPanel(), BorderLayout.NORTH);

        JSplitPane center = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, buildProcessPanel(), buildOutputPanel());
        center.setResizeWeight(0.3);
        add(center, BorderLayout.CENTER);

        loadTestCase1();
        setSize(1150, 650);
        setLocationRelativeTo(null);
    }

    private JPanel buildSettingsPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 8));
        panel.add(new JLabel("Algorithm:"));
        panel.add(algorithmBox);
        panel.add(new JLabel("Time Quantum (ACS):"));
        panel.add(quantumSpinner);
        panel.add(new JLabel("Context Switch:"));
        panel.add(contextSwitchSpinner);

        JButton runButton = new JButton("Run");
        runButton.addActionListener(e -> runSimulation());
        panel.add(runButton);

        JButton clearButton = new JButton("Clear Output");
        clearButton.addActionListener(e -> outputArea.setText(""));
        panel.add(clearButton);
        return panel;
    }

    private JPanel buildProcessPanel() {
        JPanel panel = new JPanel(new BorderLayout(4, 4));
        panel.setBorder(BorderFactory.createTitledBorder("Processes"));
        panel.add(new JScrollPane(table), BorderLayout.CENTER);

        JPanel buttons = new JPanel(new GridLayout(2, 2, 4, 4));
        JButton addButton = new JButton("Add Process");
        addButton.addActionListener(e -> addRow());
        JButton removeButton = new JButton("Remove Selected");
        removeButton.addActionListener(e -> removeSelectedRow());
        JButton case1Button = new JButton("Load Test Case 1");
        case1Button.addActionListener(e -> loadTestCase1());
        JButton case2Button = new JButton("Load Test Case 2");
        case2Button.addActionListener(e -> loadTestCase2());
        buttons.add(addButton);
        buttons.add(removeButton);
        buttons.add(case1Button);
        buttons.add(case2Button);
        panel.add(buttons, BorderLayout.SOUTH);
        return panel;
    }

    private JPanel buildOutputPanel() {
        outputArea.setEditable(false);
        outputArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 13));

        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Output"));
        panel.add(new JScrollPane(outputArea), BorderLayout.CENTER);
        return panel;
    }

    private void addRow() {
        int number = tableModel.getRowCount() + 1;
        tableModel.addRow(new Object[]{"P" + number, "0", "1", "1"});
    }

    private void removeSelectedRow() {
        int row = table.getSelectedRow();
        if (row >= 0) {
            tableModel.removeRow(row);
        }
    }

    private void loadProcesses(ArrayList<Process> processes) {
        tableModel.setRowCount(0);
        for (Process p : processes) {
            tableModel.addRow(new Object[]{
                p.getProcessId(),
                String.valueOf(p.getArrivalTime()),
                String.valueOf(p.getBurstTime()),
                String.valueOf(p.getInitialPriority())
            });
        }
    }

    private void loadTestCase1() {
        loadProcesses(Main.testCase1());
        quantumSpinner.setValue(3);
        contextSwitchSpinner.setValue(0);
    }

    private void loadTestCase2() {
        loadProcesses(Main.testCase2());
        quantumSpinner.setValue(3);
        contextSwitchSpinner.setValue(1);
    }

    private ArrayList<Process> readProcesses() {
        if (table.isEditing()) {
            table.getCellEditor().stopCellEditing();
        }
        if (tableModel.getRowCount() == 0) {
            throw new IllegalArgumentException("Add at least one process");
        }

        ArrayList<Process> processes = new ArrayList<>();
        HashSet<String> usedIds = new HashSet<>();
        for (int row = 0; row < tableModel.getRowCount(); row++) {
            String id = tableModel.getValueAt(row, 0).toString().trim();
            if (!usedIds.add(id)) {
                throw new IllegalArgumentException("Row " + (row + 1) + ": duplicate process ID " + id);
            }
            try {
                int arrival = Integer.parseInt(tableModel.getValueAt(row, 1).toString().trim());
                int burst = Integer.parseInt(tableModel.getValueAt(row, 2).toString().trim());
                int priority = Integer.parseInt(tableModel.getValueAt(row, 3).toString().trim());
                processes.add(new Process(id, arrival, burst, priority));
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("Row " + (row + 1) + ": arrival, burst and priority must be whole numbers");
            }
        }
        return processes;
    }

    private void runSimulation() {
        try {
            ArrayList<Process> processes = readProcesses();
            int quantum = (Integer) quantumSpinner.getValue();
            int contextSwitch = (Integer) contextSwitchSpinner.getValue();
            String algorithm = (String) algorithmBox.getSelectedItem();

            outputArea.setText(simulate(algorithm, processes, quantum, contextSwitch));
        } catch (IllegalArgumentException e) {
            outputArea.setText("Error: " + e.getMessage());
        }
        outputArea.setCaretPosition(0);
    }

    static String simulate(String algorithm, ArrayList<Process> processes, int quantum, int contextSwitch) {
        return captureOutput(() -> {
            if (!algorithm.equals(ACS)) {
                System.out.println("--------- Preemptive SJF (SRTF) ---------");
                new SJFScheduler().run(copyAll(processes), contextSwitch);
            }
            if (!algorithm.equals(SJF)) {
                System.out.println("--------- Adaptive Container Scheduler (ACS) ---------");
                new ACSScheduler().run(copyAll(processes), quantum, contextSwitch);
            }
        });
    }

    static String captureOutput(Runnable task) {
        PrintStream original = System.out;
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        System.setOut(new PrintStream(buffer, true));
        try {
            task.run();
        } finally {
            System.setOut(original);
        }
        return buffer.toString();
    }

    static ArrayList<Process> copyAll(ArrayList<Process> original) {
        ArrayList<Process> copies = new ArrayList<>();
        for (Process p : original) {
            copies.add(p.copy());
        }
        return copies;
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new SchedulerGUI().setVisible(true));
    }
}