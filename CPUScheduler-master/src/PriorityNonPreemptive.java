import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class PriorityNonPreemptive extends CPUScheduler {
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

        while (!rows.isEmpty()) {
            List<Row> availableRows = new ArrayList<>();

            for (Row row : rows) {
                if (row.getArrivalTime() <= time) {
                    availableRows.add(row);
                }
            }
            
            if (availableRows.isEmpty()) {
                int nextArrival = Integer.MAX_VALUE;
                for (Row row : rows) {
                    if (row.getArrivalTime() > time && row.getArrivalTime() < nextArrival) {
                        nextArrival = row.getArrivalTime();
                    }
                }
                this.getTimeline().add(new Event("IDLE", time, nextArrival, true));
                time = nextArrival;
                continue;
            }

            Collections.sort(availableRows, (Object o1, Object o2) -> {
                if (((Row) o1).getPriorityLevel() == ((Row) o2).getPriorityLevel()) {
                    return 0;
                } else if (((Row) o1).getPriorityLevel() < ((Row) o2).getPriorityLevel()) {
                    return -1;
                } else {
                    return 1;
                }
            });

            Row row = availableRows.get(0);
            this.getTimeline().add(new Event(row.getProcessName(), time, time + row.getBurstTime()));
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
