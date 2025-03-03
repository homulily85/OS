import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;

public class GUI
{
    private JFrame frame;
    private JPanel mainPanel;
    private CustomPanel chartPanel;
    private JScrollPane tablePane;
    private JScrollPane chartPane;
    private JTable table;
    private JButton addBtn;
    private JButton removeBtn;
    private JButton computeBtn;
    private JLabel wtLabel;
    private JLabel wtResultLabel;
    private JLabel tatLabel;
    private JLabel tatResultLabel;
    private JLabel rtLabel;
    private JLabel rtResultLabel;
    private JComboBox<String> option;
    private DefaultTableModel model;
    private int processCounter = 1;  // Add this class variable to track process numbers
    
    public GUI()
    {
        model = new DefaultTableModel(new String[]{"Process", "Arrival Time", "Burst Time", "Priority", "Queue", "Waiting time", "Turn around time", "Response time"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                // Make metrics columns non-editable
                return column != 5 && column != 6 && column != 7;
            }
        };
        
        table = new JTable(model);
        table.setFillsViewportHeight(true);
        // Adjust column widths to ensure headers are visible
        table.getColumnModel().getColumn(0).setPreferredWidth(60);  // Process
        table.getColumnModel().getColumn(1).setPreferredWidth(80);  // Arrival Time
        table.getColumnModel().getColumn(2).setPreferredWidth(80);  // Burst Time
        table.getColumnModel().getColumn(3).setPreferredWidth(60);  // Priority
        table.getColumnModel().getColumn(4).setPreferredWidth(60);  // Queue
        table.getColumnModel().getColumn(5).setPreferredWidth(80);  // Waiting Time
        table.getColumnModel().getColumn(6).setPreferredWidth(100); // Turn Around Time
        table.getColumnModel().getColumn(7).setPreferredWidth(90);  // Response Time - increased from 80 to 90
        
        tablePane = new JScrollPane(table);
        tablePane.setBounds(25, 25, 550, 250);  // Increased width from 450 to 550
        
        addBtn = new JButton("Add");
        addBtn.setBounds(400, 280, 85, 25);  // Changed x from 300 to 400
        addBtn.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        addBtn.addActionListener(new ActionListener(){
            @Override
            public void actionPerformed(ActionEvent e) {
                model.addRow(new String[]{"P" + processCounter++, "", "", "", "", "", "", ""});
            } 
        });
        
        removeBtn = new JButton("Remove");
        removeBtn.setBounds(490, 280, 85, 25);  // Changed x from 390 to 490
        removeBtn.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        removeBtn.addActionListener(new ActionListener(){
            @Override
            public void actionPerformed(ActionEvent e) {
                int row = table.getSelectedRow();
                
                if (row > -1) {
                    model.removeRow(row);
                    // Renumber remaining processes
                    for (int i = 0; i < model.getRowCount(); i++) {
                        model.setValueAt("P" + (i + 1), i, 0);
                    }
                    processCounter = model.getRowCount() + 1;
                }
            }
        });
        
        chartPanel = new CustomPanel();
        chartPanel.setBackground(Color.WHITE);
        chartPane = new JScrollPane(chartPanel);
        chartPane.setBounds(25, 310, 550, 100);
        
        // Configure scroll bars to only show when needed
        chartPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        chartPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_NEVER);
        
        wtLabel = new JLabel("Average Waiting Time:");
        wtLabel.setBounds(25, 425, 180, 25);
        tatLabel = new JLabel("Average Turn Around Time:");
        tatLabel.setBounds(25, 450, 180, 25);
        rtLabel = new JLabel("Average Response Time:");
        rtLabel.setBounds(25, 475, 180, 25);  // New label below TAT
        
        wtResultLabel = new JLabel();
        wtResultLabel.setBounds(215, 425, 180, 25);
        tatResultLabel = new JLabel();
        tatResultLabel.setBounds(215, 450, 180, 25);
        rtResultLabel = new JLabel();
        rtResultLabel.setBounds(215, 475, 180, 25);  // New result label
        
        option = new JComboBox<>(new String[]{
            "First Come First Serve", 
            "Shortest Job First (Non-preemptive)",
            "Shortest Job First (Preemptive)",     // Add this option
            "Priority (Non-Preemptive)",
            "Priority (Preemptive)",
            "Round Robin",
            "Multilevel Queue"  // Add new option
        });
        option.setBounds(325, 420, 250, 20);  // Adjusted position and width
        option.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String selected = (String) option.getSelectedItem();
                toggleColumns(selected);
            }
        });
        
        // Initialize columns state
        toggleColumns("First Come First Serve");
        
        computeBtn = new JButton("Compute");
        computeBtn.setBounds(490, 450, 85, 25);  // Changed x from 390 to 490
        computeBtn.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        computeBtn.addActionListener(new ActionListener(){
            @Override
            public void actionPerformed(ActionEvent e) {
                String selected = (String) option.getSelectedItem();
                CPUScheduler scheduler;
                boolean isPriorityBiggerFirst = false;

                switch (selected) {
                    case "First Come First Serve":
                        scheduler = new FirstComeFirstServe();
                        break;
                    case "Shortest Job First (Non-preemptive)":
                        scheduler = new ShortestJobFirstNonPreemptive();
                        break;
                    case "Shortest Job First (Preemptive)":    // Add this case
                        scheduler = new ShortestJobFirstPreemptive();
                        break;
                    case "Priority (Non-Preemptive)":
                    case "Priority (Preemptive)":
                        // Ask user for priority mode
                        int mode = JOptionPane.showConfirmDialog(frame,
                            "Do you want higher priority numbers to have higher priority?",
                            "Priority Mode",
                            JOptionPane.YES_NO_OPTION);
                        if (mode == JOptionPane.CLOSED_OPTION) {
                            return;
                        }
                        isPriorityBiggerFirst = (mode == JOptionPane.YES_OPTION);
                        
                        scheduler = selected.equals("Priority (Non-Preemptive)") ? 
                            new PriorityNonPreemptive() : new PriorityPreemptive();
                        break;
                    case "Round Robin":
                        String tq = JOptionPane.showInputDialog("Time Quantum");
                        if (tq == null) {
                            return;
                        }
                        scheduler = new RoundRobin();
                        scheduler.setTimeQuantum(Integer.parseInt(tq)); 
                        break;
                    case "Multilevel Queue":
                        MultilevelQueueScheduler mlq = new MultilevelQueueScheduler();
                        
                        // Configure Queue 1
                        String[] algorithms = {
                            "First Come First Serve",
                            "Shortest Job First (Non-preemptive)",
                            "Shortest Job First (Preemptive)",
                            "Priority (Non-Preemptive)",
                            "Priority (Preemptive)",
                            "Round Robin"
                        };
                        
                        // Queue 1 configuration
                        String q1Algo = (String) JOptionPane.showInputDialog(
                            frame,
                            "Select algorithm for Queue 1:",
                            "Queue 1 Configuration",
                            JOptionPane.QUESTION_MESSAGE,
                            null,
                            algorithms,
                            algorithms[0]
                        );
                        if (q1Algo == null) return;
                        
                        int q1TimeQuantum = 0;
                        boolean q1PriorityMode = false;
                        if (q1Algo.equals("Round Robin")) {
                            String tq1 = JOptionPane.showInputDialog("Time Quantum for Queue 1");
                            if (tq1 == null) return;
                            q1TimeQuantum = Integer.parseInt(tq1);
                        } else if (q1Algo.contains("Priority")) {
                            int mode1 = JOptionPane.showConfirmDialog(frame,
                                "For Queue 1: Do you want higher priority numbers to have higher priority?",
                                "Queue 1 Priority Mode",
                                JOptionPane.YES_NO_OPTION);
                            if (mode1 == JOptionPane.CLOSED_OPTION) return;
                            q1PriorityMode = (mode1 == JOptionPane.YES_OPTION);
                        }
                        
                        // Queue 2 configuration
                        String q2Algo = (String) JOptionPane.showInputDialog(
                            frame,
                            "Select algorithm for Queue 2:",
                            "Queue 2 Configuration",
                            JOptionPane.QUESTION_MESSAGE,
                            null,
                            algorithms,
                            algorithms[0]
                        );
                        if (q2Algo == null) return;
                        
                        int q2TimeQuantum = 0;
                        boolean q2PriorityMode = false;
                        if (q2Algo.equals("Round Robin")) {
                            String tq2 = JOptionPane.showInputDialog("Time Quantum for Queue 2");
                            if (tq2 == null) return;
                            q2TimeQuantum = Integer.parseInt(tq2);
                        } else if (q2Algo.contains("Priority")) {
                            int mode2 = JOptionPane.showConfirmDialog(frame,
                                "For Queue 2: Do you want higher priority numbers to have higher priority?",
                                "Queue 2 Priority Mode",
                                JOptionPane.YES_NO_OPTION);
                            if (mode2 == JOptionPane.CLOSED_OPTION) return;
                            q2PriorityMode = (mode2 == JOptionPane.YES_OPTION);
                        }
                        
                        mlq.addQueueConfig(new QueueConfig(1, q1Algo, q1PriorityMode, q1TimeQuantum));
                        mlq.addQueueConfig(new QueueConfig(2, q2Algo, q2PriorityMode, q2TimeQuantum));
                        
                        scheduler = mlq;
                        break;
                    default:
                        return;
                }
                
                try {
                    for (int i = 0; i < model.getRowCount(); i++) {
                        String process = (String) model.getValueAt(i, 0);
                        
                        // Check for empty process name
                        if (process == null || process.trim().isEmpty()) {
                            throw new IllegalArgumentException("Process name cannot be empty");
                        }
                        
                        // Arrival time is always required now
                        String atStr = (String) model.getValueAt(i, 1);
                        int at = 0;
                        if (atStr != null && !atStr.trim().isEmpty()) {
                            at = Integer.parseInt(atStr.trim());
                            if (at < 0) {
                                throw new IllegalArgumentException("Arrival Time cannot be negative");
                            }
                        }
                        
                        // Burst time is always required
                        String btStr = (String) model.getValueAt(i, 2);
                        if (btStr == null || btStr.trim().isEmpty()) {
                            throw new IllegalArgumentException("Burst Time cannot be empty");
                        }
                        int bt = Integer.parseInt(btStr.trim());
                        if (bt <= 0) {
                            throw new IllegalArgumentException("Burst Time must be greater than 0");
                        }
                        
                        // Modified priority handling
                        int pl = 1;
                        if (selected.equals("Priority (Non-Preemptive)") || selected.equals("Priority (Preemptive)")) {
                            String plStr = (String) model.getValueAt(i, 3);
                            if (plStr != null && !plStr.trim().isEmpty()) {
                                pl = Integer.parseInt(plStr.trim());
                                if (isPriorityBiggerFirst) {
                                    // For bigger-first mode, make priority negative to maintain algorithm logic
                                    pl = -pl;
                                }
                            }
                        }
                        
                        // Get queue number for multilevel queue
                        int queueNum = 1; // Default to queue 1
                        if (selected.equals("Multilevel Queue")) {
                            String qStr = (String) model.getValueAt(i, 4);
                            if (qStr != null && !qStr.trim().isEmpty()) {
                                queueNum = Integer.parseInt(qStr.trim());
                                if (queueNum < 1 || queueNum > 2) {
                                    throw new IllegalArgumentException("Queue number must be 1 or 2");
                                }
                            }
                        }
                        
                        scheduler.add(new Row(process, at, bt, pl, queueNum));
                    }
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(frame, 
                        "Please enter valid numbers only",
                        "Invalid Input",
                        JOptionPane.ERROR_MESSAGE);
                    return;
                } catch (IllegalArgumentException ex) {
                    JOptionPane.showMessageDialog(frame, 
                        ex.getMessage(),
                        "Invalid Input",
                        JOptionPane.ERROR_MESSAGE);
                    return;
                }
                
                scheduler.process();
                
                for (int i = 0; i < model.getRowCount(); i++)
                {
                    String process = (String) model.getValueAt(i, 0);
                    Row row = scheduler.getRow(process);
                    model.setValueAt(row.getWaitingTime(), i, 5);
                    model.setValueAt(row.getTurnaroundTime(), i, 6);
                    model.setValueAt(row.getResponseTime(), i, 7);  // Add this line
                    
                    // If showing priority column, convert negative priorities back to positive for display
                    if (isPriorityBiggerFirst && table.getColumnModel().getColumn(3).getMaxWidth() != 0) {
                        String plStr = (String) model.getValueAt(i, 3);
                        if (plStr != null && !plStr.trim().isEmpty()) {
                            int pl = Integer.parseInt(plStr.trim());
                            model.setValueAt(Integer.toString(pl), i, 3);
                        }
                    }
                }
                
                wtResultLabel.setText(Double.toString(scheduler.getAverageWaitingTime()));
                tatResultLabel.setText(Double.toString(scheduler.getAverageTurnAroundTime()));
                rtResultLabel.setText(Double.toString(scheduler.getAverageResponseTime()));  // Add this line
                
                chartPanel.setTimeline(scheduler.getTimeline());
            }
        });
        
        mainPanel = new JPanel(null);
        mainPanel.setPreferredSize(new Dimension(600, 525));  // Increased height to accommodate new label
        mainPanel.add(tablePane);
        mainPanel.add(addBtn);
        mainPanel.add(removeBtn);
        mainPanel.add(chartPane);
        mainPanel.add(wtLabel);
        mainPanel.add(tatLabel);
        mainPanel.add(rtLabel);  // Add new label
        mainPanel.add(wtResultLabel);
        mainPanel.add(tatResultLabel);
        mainPanel.add(rtResultLabel);  // Add new result label
        mainPanel.add(option);
        mainPanel.add(computeBtn);
        
        frame = new JFrame("CPU Scheduler Simulator");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);
        frame.setResizable(false);
        frame.add(mainPanel);
        frame.pack();
    }
    
    private void toggleColumns(String selectedAlgorithm) {
        int priorityColumn = 3;
        int arrivalTimeColumn = 1;
        int queueColumn = 4;

        // Show/hide queue column
        if (selectedAlgorithm.equals("Multilevel Queue")) {
            showColumn(queueColumn, 60);
        } else {
            hideColumn(queueColumn);
        }

        // Handle Priority Column
        if (selectedAlgorithm.equals("First Come First Serve") || 
            selectedAlgorithm.equals("Shortest Job First (Non-preemptive)") ||
            selectedAlgorithm.equals("Shortest Job First (Preemptive)") ||  // Add this condition
            selectedAlgorithm.equals("Round Robin")) {
            hideColumn(priorityColumn);
        } else {
            showColumn(priorityColumn, 60);
        }

        // Always show arrival time column
        showColumn(arrivalTimeColumn, 80);
    }

    private void hideColumn(int columnIndex) {
        // Clear values and hide column
        for (int i = 0; i < model.getRowCount(); i++) {
            model.setValueAt("", i, columnIndex);
        }
        table.getColumnModel().getColumn(columnIndex).setMinWidth(0);
        table.getColumnModel().getColumn(columnIndex).setMaxWidth(0);
        table.getColumnModel().getColumn(columnIndex).setWidth(0);
    }

    private void showColumn(int columnIndex, int preferredWidth) {
        table.getColumnModel().getColumn(columnIndex).setMinWidth(60);
        table.getColumnModel().getColumn(columnIndex).setMaxWidth(250);  // Increased from 200 to 250
        table.getColumnModel().getColumn(columnIndex).setPreferredWidth(preferredWidth);
    }
    
    public static void main(String[] args)
    {
        new GUI();
    }
    
    class CustomPanel extends JPanel
    {   
        private List<Event> timeline;
        private final int SCALE_FACTOR = 20;  // pixels per time unit
        private final int HEIGHT = 30;        // height of rectangles
        private final int MARGIN_TOP = 20;    // top margin
        private final int MARGIN_X = 50;      // horizontal margins
        
        @Override
        public Dimension getPreferredSize() {
            if (timeline == null || timeline.isEmpty()) {
                return new Dimension(chartPane.getWidth() - 10, 75);
            }
            
            // Calculate total width needed
            int totalWidth = MARGIN_X * 2; // Left and right margins
            for (Event event : timeline) {
                totalWidth += (event.getFinishTime() - event.getStartTime()) * SCALE_FACTOR;
            }
            
            // If total width is less than the visible area, use the visible area width
            return new Dimension(
                Math.max(totalWidth, chartPane.getWidth() - 10), 
                75
            );
        }
        
        @Override
        protected void paintComponent(Graphics g)
        {
            super.paintComponent(g);
            
            if (timeline != null)
            {
                int x = MARGIN_X;  // Start with left margin
                
                for (int i = 0; i < timeline.size(); i++)
                {
                    Event event = timeline.get(i);
                    int duration = event.getFinishTime() - event.getStartTime();
                    int width = duration * SCALE_FACTOR;
                    
                    // Draw the rectangle
                    g.drawRect(x, MARGIN_TOP, width, HEIGHT);
                    
                    // Center the process name in the rectangle
                    g.setFont(new Font("Segoe UI", Font.BOLD, 13));
                    String processName = event.getProcessName();
                    int nameWidth = g.getFontMetrics().stringWidth(processName);
                    g.drawString(processName, x + (width - nameWidth)/2, MARGIN_TOP + 20);
                    
                    // Draw start time
                    g.setFont(new Font("Segoe UI", Font.PLAIN, 11));
                    g.drawString(Integer.toString(event.getStartTime()), x, MARGIN_TOP + 45);
                    
                    // Draw finish time (only if it's not the start time of the next event)
                    if (i == timeline.size() - 1 || 
                        event.getFinishTime() != timeline.get(i + 1).getStartTime()) {
                        String finishTime = Integer.toString(event.getFinishTime());
                        int timeWidth = g.getFontMetrics().stringWidth(finishTime);
                        g.drawString(finishTime, x + width - timeWidth/2, MARGIN_TOP + 45);
                    }
                    
                    x += width;
                }
            }
        }
        
        public void setTimeline(List<Event> timeline)
        {
            this.timeline = timeline;
            revalidate();  // Add this to ensure scroll pane updates
            repaint();
        }
    }
}
