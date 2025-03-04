import java.util.Collections;
import java.util.List;

public class FirstComeFirstServe extends CPUScheduler {
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

        List<Event> timeline = this.getTimeline();
        int currentTime = this.getRows().get(0).getArrivalTime();

        for (Row row : this.getRows()) {
            if (row.getArrivalTime() > currentTime) {
                timeline.add(new Event("IDLE", currentTime, row.getArrivalTime(), true));
                currentTime = row.getArrivalTime();
            }

            timeline.add(new Event(row.getProcessName(), currentTime,
                    currentTime + row.getBurstTime()));
            currentTime += row.getBurstTime();
        }

        for (Row row : this.getRows()) {
            row.setWaitingTime(this.getEvent(row).getStartTime() - row.getArrivalTime());
            row.setTurnaroundTime(row.getWaitingTime() + row.getBurstTime());
            row.setResponseTime(row.getWaitingTime());
        }
    }
}
