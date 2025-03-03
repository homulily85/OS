import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ShortestJobFirstPreemptive extends CPUScheduler {
    @Override
    public void process() {
        // Sort by arrival time first
        Collections.sort(this.getRows(), (Object o1, Object o2) -> {
            if (((Row) o1).getArrivalTime() == ((Row) o2).getArrivalTime()) {
                return 0;
            } else if (((Row) o1).getArrivalTime() < ((Row) o2).getArrivalTime()) {
                return -1;
            } else {
                return 1;
            }
        });

        List<Row> rows = Utility.deepCopy(this.getRows());
        int currentTime = rows.isEmpty() ? 0 : rows.get(0).getArrivalTime();
        
        Map<String, Integer> remainingBurstTime = new HashMap<>();
        Map<String, Boolean> firstExecution = new HashMap<>();
        
        // Initialize tracking maps
        for (Row row : rows) {
            remainingBurstTime.put(row.getProcessName(), row.getBurstTime());
            firstExecution.put(row.getProcessName(), false);
        }

        while (!remainingBurstTime.isEmpty()) {
            // Find available processes at current time
            List<Row> availableProcesses = new ArrayList<>();
            for (Row row : rows) {
                if (row.getArrivalTime() <= currentTime && 
                    remainingBurstTime.containsKey(row.getProcessName())) {
                    availableProcesses.add(row);
                }
            }
            
            if (availableProcesses.isEmpty()) {
                // Find next arrival
                final int currentTimeFinal = currentTime;
                int nextArrival = rows.stream()
                    .filter(row -> row.getArrivalTime() > currentTimeFinal && 
                                 remainingBurstTime.containsKey(row.getProcessName()))
                    .mapToInt(Row::getArrivalTime)
                    .min()
                    .orElse(Integer.MAX_VALUE);
                
                if (nextArrival == Integer.MAX_VALUE) break;
                currentTime = nextArrival;
                continue;
            }
            
            // Sort by remaining burst time
            Collections.sort(availableProcesses, (o1, o2) -> {
                int bt1 = remainingBurstTime.get(o1.getProcessName());
                int bt2 = remainingBurstTime.get(o2.getProcessName());
                return Integer.compare(bt1, bt2);
            });
            
            Row selectedProcess = availableProcesses.get(0);
            String processName = selectedProcess.getProcessName();
            
            // Record response time if first execution
            if (!firstExecution.get(processName)) {
                this.getRow(processName).setResponseTime(currentTime - selectedProcess.getArrivalTime());
                firstExecution.put(processName, true);
            }
            
            // Find next arrival that could preempt current process
            int remainingTime = remainingBurstTime.get(processName);
            int nextPreemption = Integer.MAX_VALUE;
            
            for (Row row : rows) {
                if (!row.getProcessName().equals(processName) && 
                    remainingBurstTime.containsKey(row.getProcessName()) && 
                    row.getArrivalTime() > currentTime) {
                    
                    // Changed: Compare with remaining time instead of burst time
                    if (remainingBurstTime.get(processName) > row.getBurstTime()) {
                        nextPreemption = Math.min(nextPreemption, row.getArrivalTime());
                    }
                }
            }
            
            // Execute until either completion or preemption
            int executionTime;
            if (nextPreemption == Integer.MAX_VALUE) {
                // Process can complete
                executionTime = remainingTime;
            } else {
                // Process will be preempted
                executionTime = nextPreemption - currentTime;
            }
            
            // Add event to timeline
            this.getTimeline().add(new Event(processName, currentTime, currentTime + executionTime));
            
            // Update remaining time and current time
            remainingBurstTime.put(processName, remainingTime - executionTime);
            currentTime += executionTime;
            
            // Remove process if completed
            if (remainingBurstTime.get(processName) <= 0) {
                remainingBurstTime.remove(processName);
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
        
        // Calculate waiting and turnaround times
        for (Row row : this.getRows()) {
            // Find completion time (end of last event for this process)
            int completionTime = 0;
            for (Event event : this.getTimeline()) {
                if (event.getProcessName().equals(row.getProcessName())) {
                    completionTime = event.getFinishTime();
                }
            }
            
            row.setTurnaroundTime(completionTime - row.getArrivalTime());
            row.setWaitingTime(row.getTurnaroundTime() - row.getBurstTime());
        }
    }
    
    @Override
    public Row pickNextProcess(int currentTime) {
        if (this.getRows().isEmpty()) return null;
        
        // Get available processes
        List<Row> availableProcesses = new ArrayList<>();
        for (Row row : this.getRows()) {
            if (row.getArrivalTime() <= currentTime) {
                availableProcesses.add(row);
            }
        }
        
        if (availableProcesses.isEmpty()) return null;
        
        // Sort by burst time (shortest first)
        Collections.sort(availableProcesses, (o1, o2) -> {
            return Integer.compare(o1.getBurstTime(), o2.getBurstTime());
        });
        
        return availableProcesses.get(0);
    }
}
