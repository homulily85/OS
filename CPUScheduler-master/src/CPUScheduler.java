import java.util.ArrayList;
import java.util.List;

public abstract class CPUScheduler
{
    private final List<Row> rows;
    private final List<Event> timeline;
    private int timeQuantum;
    
    public CPUScheduler()
    {
        rows = new ArrayList<>();
        timeline = new ArrayList<>();
        timeQuantum = 1;
    }
    
    public boolean add(Row row)
    {
        return rows.add(row);
    }
    
    public void setTimeQuantum(int timeQuantum)
    {
        this.timeQuantum = timeQuantum;
    }
    
    public int getTimeQuantum()
    {
        return timeQuantum;
    }
    
    public double getAverageWaitingTime()
    {
        double avg = 0.0;
        
        for (Row row : rows)
        {
            avg += row.getWaitingTime();
        }
        
        return avg / rows.size();
    }
    
    public double getAverageTurnAroundTime()
    {
        double avg = 0.0;
        
        for (Row row : rows)
        {
            avg += row.getTurnaroundTime();
        }
        
        return avg / rows.size();
    }
    
    public double getAverageResponseTime()
    {
        double avg = 0.0;
        
        for (Row row : rows)
        {
            avg += row.getResponseTime();
        }
        
        return avg / rows.size();
    }
    
    public Event getEvent(Row row)
    {
        for (Event event : timeline)
        {
            if (row.getProcessName().equals(event.getProcessName()))
            {
                return event;
            }
        }
        
        return null;
    }
    
    public Row getRow(String process)
    {
        for (Row row : rows)
        {
            if (row.getProcessName().equals(process))
            {
                return row;
            }
        }
        
        return null;
    }
    
    public List<Row> getRows()
    {
        return rows;
    }
    
    public List<Event> getTimeline()
    {
        return timeline;
    }
    
    /**
     * Picks the next process to execute based on the scheduler's algorithm
     * This is used by the multilevel queue scheduler for preemptive scheduling
     * Default behavior is FCFS - override in specific schedulers
     */
    public Row pickNextProcess(int currentTime) {
        if (this.rows.isEmpty()) return null;
        
        // By default, return the first ready process (FCFS)
        for (Row row : this.rows) {
            if (row.getArrivalTime() <= currentTime) {
                return row;
            }
        }
        return null;
    }
    public abstract void process();
}
