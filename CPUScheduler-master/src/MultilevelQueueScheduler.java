import java.util.*;

public class MultilevelQueueScheduler extends CPUScheduler {
    private List<QueueConfig> queueConfigs;
    private Map<Integer, CPUScheduler> schedulers;

    public MultilevelQueueScheduler() {
        queueConfigs = new ArrayList<>();
        schedulers = new HashMap<>();
    }

    public CPUScheduler createSchedulerForQueue(QueueConfig config) {
        CPUScheduler scheduler = null;
        switch (config.getAlgorithm()) {
            case "First Come First Serve":
                scheduler = new FirstComeFirstServe();
                break;
            case "Shortest Job First (Non-preemptive)":
                scheduler = new ShortestJobFirstNonPreemptive();
                break;
            case "Shortest Job First (Preemptive)":
                scheduler = new ShortestJobFirstPreemptive();
                break;
            case "Priority (Non-Preemptive)":
                scheduler = new PriorityNonPreemptive();
                break;
            case "Priority (Preemptive)":
                scheduler = new PriorityPreemptive();
                break;
            case "Round Robin":
                scheduler = new RoundRobin();
                ((RoundRobin)scheduler).setTimeQuantum(config.getTimeQuantum());
                break;
        }
        return scheduler;
    }

    public void addQueueConfig(QueueConfig config) {
        queueConfigs.add(config);
        schedulers.put(config.getQueueNumber(), createSchedulerForQueue(config));
    }

    @Override
    public void process() {
        // Sort queues by priority (queue number)
        Collections.sort(queueConfigs, (q1, q2) -> 
            Integer.compare(q1.getQueueNumber(), q2.getQueueNumber()));

        // Group processes by queue
        Map<Integer, List<Row>> queueProcesses = new HashMap<>();
        for (Row row : this.getRows()) {
            Row rowCopy = new Row(row.getProcessName(), row.getArrivalTime(), row.getBurstTime(),
                row.getPriorityLevel(), row.getQueueNumber());
            queueProcesses.computeIfAbsent(row.getQueueNumber(), k -> new ArrayList<>()).add(rowCopy);
        }

        // Find earliest arrival time
        int currentTime = this.getRows().stream()
            .mapToInt(Row::getArrivalTime)
            .min()
            .orElse(0);

        Map<String, Integer> remainingBurstTime = new HashMap<>();
        Map<String, Integer> processStartTime = new HashMap<>();
        Map<String, Boolean> firstResponse = new HashMap<>();
            
        // Initialize remaining burst times
        for (Row row : this.getRows()) {
            remainingBurstTime.put(row.getProcessName(), row.getBurstTime());
            firstResponse.put(row.getProcessName(), false);
        }
            
        // Main scheduling loop - continue until all processes are completed
        while (!remainingBurstTime.isEmpty()) {
            // Find next process to run
            Row selectedProcess = null;
            int selectedQueue = Integer.MAX_VALUE;
            
            // Check each queue from highest to lowest priority
            for (QueueConfig config : queueConfigs) {
                int queueNum = config.getQueueNumber();
                List<Row> queueRows = queueProcesses.get(queueNum);
                if (queueRows == null) continue;
                
                // Find ready processes in this queue
                List<Row> readyProcesses = new ArrayList<>();
                for (Row row : queueRows) {
                    if (row.getArrivalTime() <= currentTime && 
                        remainingBurstTime.containsKey(row.getProcessName())) {
                        readyProcesses.add(row);
                    }
                }
                
                if (!readyProcesses.isEmpty()) {
                    // Use appropriate scheduling algorithm for this queue
                    CPUScheduler scheduler = schedulers.get(queueNum);
                    scheduler.getRows().clear();
                    
                    // Create temporary copies with correct remaining burst times
                    for (Row process : readyProcesses) {
                        Row tempRow = new Row(
                            process.getProcessName(),
                            process.getArrivalTime(),
                            remainingBurstTime.get(process.getProcessName()),
                            process.getPriorityLevel(),
                            process.getQueueNumber()
                        );
                        scheduler.add(tempRow);
                    }
                    
                    // Get next process using this queue's algorithm
                    Row nextProcess = scheduler.pickNextProcess(currentTime);
                    if (nextProcess != null) {
                        selectedProcess = nextProcess;
                        selectedQueue = queueNum;
                        break;  // Found a process in higher priority queue
                    }
                }
            }
            
            if (selectedProcess == null) {
                // No ready processes, advance time to next arrival
                int nextArrival = Integer.MAX_VALUE;
                for (Row row : this.getRows()) {
                    if (remainingBurstTime.containsKey(row.getProcessName()) && 
                        row.getArrivalTime() > currentTime && 
                        row.getArrivalTime() < nextArrival) {
                        nextArrival = row.getArrivalTime();
                    }
                }
                
                if (nextArrival == Integer.MAX_VALUE) break; // No more processes
                currentTime = nextArrival;
                continue;
            }
            
            // Execute the selected process for one time unit
            String processName = selectedProcess.getProcessName();
            
            // Record first response time
            if (!firstResponse.get(processName)) {
                Row originalRow = this.getRow(processName);
                originalRow.setResponseTime(currentTime - originalRow.getArrivalTime());
                firstResponse.put(processName, true);
            }
            
            // If this is a new process start, record its start time
            if (!processStartTime.containsKey(processName)) {
                processStartTime.put(processName, currentTime);
            }
            
            // Check how long we can run before next higher priority process arrives
            int runUntil = currentTime + remainingBurstTime.get(processName);
            boolean preempted = false;
            
            // Check for higher priority arrivals
            for (int q = 1; q < selectedQueue; q++) {
                List<Row> higherQueueRows = queueProcesses.get(q);
                if (higherQueueRows == null) continue;
                
                for (Row higherProcess : higherQueueRows) {
                    if (remainingBurstTime.containsKey(higherProcess.getProcessName()) && 
                        higherProcess.getArrivalTime() > currentTime &&
                        higherProcess.getArrivalTime() < runUntil) {
                        runUntil = higherProcess.getArrivalTime();
                        preempted = true;
                        break;
                    }
                }
                if (preempted) break;
            }
            
            // Execute process until completion or preemption
            int executionTime = runUntil - currentTime;
            remainingBurstTime.put(processName, remainingBurstTime.get(processName) - executionTime);
            
            // Add event to timeline
            this.getTimeline().add(new Event(processName, currentTime, runUntil));
            currentTime = runUntil;
            
            // If process is complete, calculate its statistics
            if (remainingBurstTime.get(processName) <= 0) {
                remainingBurstTime.remove(processName);
                Row originalRow = this.getRow(processName);
                
                // Calculate turnaround and waiting times
                int turnaroundTime = currentTime - originalRow.getArrivalTime();
                int waitingTime = turnaroundTime - originalRow.getBurstTime();
                
                originalRow.setTurnaroundTime(turnaroundTime);
                originalRow.setWaitingTime(waitingTime);
            }
        }
        
        // Merge consecutive events for the same process
        for (int i = this.getTimeline().size() - 1; i > 0; i--) {
            List<Event> timeline = this.getTimeline();
            if (timeline.get(i - 1).getProcessName().equals(timeline.get(i).getProcessName())) {
                timeline.get(i - 1).setFinishTime(timeline.get(i).getFinishTime());
                timeline.remove(i);
            }
        }
    }
    
    // Add helper method to pick next process for each queue's scheduler
    private Row pickNextReadyProcess(List<Row> rows, int currentTime) {
        if (rows.isEmpty()) return null;
        
        // Default to first-come first-served
        return rows.get(0);
    }
}
