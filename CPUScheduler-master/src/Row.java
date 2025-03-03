public class Row {
    private String processName;
    private int arrivalTime;
    private int burstTime;
    private int priorityLevel;
    private int waitingTime;
    private int turnaroundTime;
    private int responseTime;
    private int queueNumber;

    private Row(String processName, int arrivalTime, int burstTime, int priorityLevel, int waitingTime,
            int turnaroundTime, int responseTime, int queueNumber) {
        this.processName = processName;
        this.arrivalTime = arrivalTime;
        this.burstTime = burstTime;
        this.priorityLevel = priorityLevel;
        this.waitingTime = waitingTime;
        this.turnaroundTime = turnaroundTime;
        this.responseTime = responseTime;
        this.queueNumber = queueNumber;
    }

    public Row(String processName, int arrivalTime, int burstTime, int priorityLevel, int queueNumber) {
        this(processName, arrivalTime, burstTime, priorityLevel, 0, 0, 0, queueNumber);
    }

    public Row(String processName, int arrivalTime, int burstTime) {
        this(processName, arrivalTime, burstTime, 0, 0, 0, 0, 0);
    }

    public void setBurstTime(int burstTime) {
        this.burstTime = burstTime;
    }

    public void setWaitingTime(int waitingTime) {
        this.waitingTime = waitingTime;
    }

    public void setTurnaroundTime(int turnaroundTime) {
        this.turnaroundTime = turnaroundTime;
    }

    public String getProcessName() {
        return this.processName;
    }

    public int getArrivalTime() {
        return this.arrivalTime;
    }

    public int getBurstTime() {
        return this.burstTime;
    }

    public int getPriorityLevel() {
        return this.priorityLevel;
    }

    public int getWaitingTime() {
        return this.waitingTime;
    }

    public int getTurnaroundTime() {
        return this.turnaroundTime;
    }

    public int getResponseTime() {
        return responseTime;
    }

    public void setResponseTime(int responseTime) {
        this.responseTime = responseTime;
    }

    public int getQueueNumber() {
        return queueNumber;
    }

    public void setQueueNumber(int queueNumber) {
        this.queueNumber = queueNumber;
    }

    public void setProcessName(String processName) {
        this.processName = processName;
    }

    public void setArrivalTime(int arrivalTime) {
        this.arrivalTime = arrivalTime;
    }

    public void setPriorityLevel(int priorityLevel) {
        this.priorityLevel = priorityLevel;
    }

}
