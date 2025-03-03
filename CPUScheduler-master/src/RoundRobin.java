import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RoundRobin extends CPUScheduler {
    @Override
    public void process() {
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
        int time = rows.get(0).getArrivalTime();
        int timeQuantum = this.getTimeQuantum();
        
        // Store original burst times
        Map<String, Integer> originalBurstTime = new HashMap<>();
        for (Row row : this.getRows()) {
            originalBurstTime.put(row.getProcessName(), row.getBurstTime());
        }

        Map<String, Boolean> firstResponse = new HashMap<>();
        // Initialize response tracking
        for (Row row : this.getRows()) {
            firstResponse.put(row.getProcessName(), false);
        }

        while (!rows.isEmpty()) {
            Row row = rows.get(0);
            int bt = Math.min(row.getBurstTime(), timeQuantum);
            
            // Calculate response time for first execution
            if (!firstResponse.get(row.getProcessName())) {
                this.getRow(row.getProcessName()).setResponseTime(time - row.getArrivalTime());
                firstResponse.put(row.getProcessName(), true);
            }

            this.getTimeline().add(new Event(row.getProcessName(), time, time + bt));
            time += bt;
            rows.remove(0);

            if (row.getBurstTime() > timeQuantum) {
                row.setBurstTime(row.getBurstTime() - timeQuantum);

                // Add back to queue if there's remaining time
                boolean added = false;
                for (int i = 0; i < rows.size(); i++) {
                    if (rows.get(i).getArrivalTime() > time) {
                        rows.add(i, row);
                        added = true;
                        break;
                    }
                }
                if (!added) {
                    rows.add(row);
                }
            }
        }

        // Calculate waiting and turnaround times
        for (Row row : this.getRows()) {
            int lastFinishTime = 0;
            int waitingTime = 0;
            
            // Find the last finish time and calculate waiting time
            for (Event event : this.getTimeline()) {
                if (event.getProcessName().equals(row.getProcessName())) {
                    if (lastFinishTime == 0) {
                        waitingTime = event.getStartTime() - row.getArrivalTime();
                    } else {
                        waitingTime += event.getStartTime() - lastFinishTime;
                    }
                    lastFinishTime = event.getFinishTime();
                }
            }
            
            row.setWaitingTime(waitingTime);
            row.setTurnaroundTime(lastFinishTime - row.getArrivalTime());
            // Restore original burst time
            row.setBurstTime(originalBurstTime.get(row.getProcessName()));
        }
    }
}
