import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ShortestJobFirstPreemptive extends CPUScheduler
{
    @Override
    public void process()
    {
        Collections.sort(this.getRows(), (Object o1, Object o2) -> {
            if (((Row) o1).getArrivalTime() == ((Row) o2).getArrivalTime())
            {
                return 0;
            }
            else if (((Row) o1).getArrivalTime() < ((Row) o2).getArrivalTime())
            {
                return -1;
            }
            else
            {
                return 1;
            }
        });
        
        List<Row> rows = Utility.deepCopy(this.getRows());
        int time = rows.get(0).getArrivalTime();
        
        while (!rows.isEmpty())
        {
            List<Row> availableRows = new ArrayList();
            
            for (Row row : rows)
            {
                if (row.getArrivalTime() <= time)
                {
                    availableRows.add(row);
                }
            }
            
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
            
            Collections.sort(availableRows, (Object o1, Object o2) -> {
                if (((Row) o1).getBurstTime() == ((Row) o2).getBurstTime())
                {
                    return 0;
                }
                else if (((Row) o1).getBurstTime() < ((Row) o2).getBurstTime())
                {
                    return -1;
                }
                else
                {
                    return 1;
                }
            });
            
            Row row = availableRows.get(0);
            this.getTimeline().add(new Event(row.getProcessName(), time, ++time));
            row.setBurstTime(row.getBurstTime() - 1);
            
            if (row.getBurstTime() == 0)
            {
                for (int i = 0; i < rows.size(); i++)
                {
                    if (rows.get(i).getProcessName().equals(row.getProcessName()))
                    {
                        rows.remove(i);
                        break;
                    }
                }
            }
        }
        
        for (int i = this.getTimeline().size() - 1; i > 0; i--)
        {
            List<Event> timeline = this.getTimeline();
            
            if (timeline.get(i - 1).getProcessName().equals(timeline.get(i).getProcessName()))
            {
                timeline.get(i - 1).setFinishTime(timeline.get(i).getFinishTime());
                timeline.remove(i);
            }
        }
        
        Map map = new HashMap();
        Map<String, Integer> firstExecution = new HashMap<>();  // Track first execution time
        
        for (Row row : this.getRows())
        {
            map.clear();
            
            for (Event event : this.getTimeline())
            {
                if (event.getProcessName().equals(row.getProcessName()))
                {
                    // Track first execution time for response time
                    if (!firstExecution.containsKey(event.getProcessName())) {
                        firstExecution.put(event.getProcessName(), event.getStartTime());
                        row.setResponseTime(event.getStartTime() - row.getArrivalTime());
                    }
                    
                    if (map.containsKey(event.getProcessName()))
                    {
                        int w = event.getStartTime() - (int) map.get(event.getProcessName());
                        row.setWaitingTime(row.getWaitingTime() + w);
                    }
                    else
                    {
                        row.setWaitingTime(event.getStartTime() - row.getArrivalTime());
                    }
                    
                    map.put(event.getProcessName(), event.getFinishTime());
                }
            }
            
            row.setTurnaroundTime(row.getWaitingTime() + row.getBurstTime());
        }
    }
}
