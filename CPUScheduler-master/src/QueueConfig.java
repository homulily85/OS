public class QueueConfig {
    private int queueNumber;
    private String algorithm;
    private boolean isHigherPriorityBetter;
    private int timeQuantum;  // For RR

    public QueueConfig(int queueNumber, String algorithm, boolean isHigherPriorityBetter, int timeQuantum) {
        this.queueNumber = queueNumber;
        this.algorithm = algorithm;
        this.isHigherPriorityBetter = isHigherPriorityBetter;
        this.timeQuantum = timeQuantum;
    }

    public int getQueueNumber() { return queueNumber; }
    public String getAlgorithm() { return algorithm; }
    public boolean isHigherPriorityBetter() { return isHigherPriorityBetter; }
    public int getTimeQuantum() { return timeQuantum; }
}
