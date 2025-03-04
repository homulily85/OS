import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ShortestJobFirstNonPreemptive extends CPUScheduler {
    @Override
    public void process() {
        // Sort by arrival time first to choose which to start first
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
        // Time variable indicate the time at which a process finishes using CPU. 
        // Here we initialize it by the arrival time of the first process.
        int time = rows.get(0).getArrivalTime();

        while (!rows.isEmpty()) {
            List<Row> availableRows = new ArrayList<>();

            for (Row row : rows) {
                if (row.getArrivalTime() <= time) {
                    availableRows.add(row);
                }
            }

            // Handle empty ready queue
            if (availableRows.isEmpty()) {
                // Find next arrival time
                int nextArrival = Integer.MAX_VALUE;
                for (Row row : rows) {
                    if (row.getArrivalTime() > time && row.getArrivalTime() < nextArrival) {
                        nextArrival = row.getArrivalTime();
                    }
                }
                // Add IDLE event
                this.getTimeline().add(new Event("IDLE", time, nextArrival, true));
                time = nextArrival;
                continue;
            }

            // Sort them to determine the order
            Collections.sort(availableRows, (Object o1, Object o2) -> {
                if (((Row) o1).getBurstTime() == ((Row) o2).getBurstTime()) {
                    return 0;
                } else if (((Row) o1).getBurstTime() < ((Row) o2).getBurstTime()) {
                    return -1;
                } else {
                    return 1;
                }
            });

            Row row = availableRows.get(0);
            this.getTimeline().add(new Event(row.getProcessName(), time, time + row.getBurstTime()));
            
            // Update time
            time += row.getBurstTime();

            for (int i = 0; i < rows.size(); i++) {
                if (rows.get(i).getProcessName().equals(row.getProcessName())) {
                    rows.remove(i);
                    break;
                }
            }
        }

        for (Row row : this.getRows()) {
            row.setWaitingTime(this.getEvent(row).getStartTime() - row.getArrivalTime());
            row.setTurnaroundTime(row.getWaitingTime() + row.getBurstTime());
            row.setResponseTime(row.getWaitingTime());
        }
    }
}
