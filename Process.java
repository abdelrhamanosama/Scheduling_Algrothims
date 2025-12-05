public class Process {
    private static int counter = 0;
    private int processId;
    private int arrivalTime;
    private int burstTime;
    private int remainingTime;
    private int turnaroundTime;
    private int waitingTime;
    private int priority;
    private String name;
    private final ProcessType type;
    // New fields from second class
    private int startedAt = -1;
    private int finishedAt;
    private int responseTime = -1;

    public int currentQueue = 0;
    public int quantumUsed = 0;

    public Process(int arrivalTime, int burstTime, ProcessType type) {
        processId = counter++;
        this.arrivalTime = arrivalTime;
        this.finishedAt = arrivalTime;
        this.burstTime = burstTime;
        this.remainingTime = burstTime;
        this.type = type;

        this.turnaroundTime = 0;        
        this.startedAt = 0;
        this.waitingTime = 0;
        this.responseTime = -1;
    }

    public Process(String name, int arrivalTime, int burstTime, int priority, ProcessType processType) {
        processId = counter++;
        this.name = name;
        this.arrivalTime = arrivalTime;
        this.burstTime = burstTime;
        this.remainingTime = burstTime;
        this.priority = priority;
        this.finishedAt = 0;
        this.turnaroundTime = 0;
        this.waitingTime = 0;
        this.type = processType;
    }

    // Getters
    public int getProcessId() {
        return processId;
    }

    public String getName() {
        return name;
    }

    public int getArrivalTime() {
        return arrivalTime;
    }

    public int getBurstTime() {
        return burstTime;
    }

    public int getRemainingTime() {
        return remainingTime;
    }

    public int getTurnaroundTime() {
        return turnaroundTime;
    }

    public int getWaitingTime() {
        return waitingTime;
    }

    public int getPriority() {
        return priority;
    }

    public int getStartedAt() {
        return startedAt;
    }

    public int getResponseTime() {
        return responseTime;
    }

     public ProcessType getType() {
        return type;
    }

    public int getFinishedAt() {
        return finishedAt;
    }

    // Setters
    public void setRemainingTime(int remainingTime) {
        this.remainingTime = remainingTime;
    }

    public void setTurnaroundTime(int turnaroundTime) {
        this.turnaroundTime = turnaroundTime;
    }

    public void setWaitingTime(int waitingTime) {
        this.waitingTime = waitingTime;
    }

    public void setFinishedAt(int finishedAt) {
        this.finishedAt = finishedAt;
    }

    public void setResponseTime(int responseTime) {
        this.responseTime = responseTime;
    }

    public void setStartedAt(int startedAt) {
        this.startedAt = startedAt;
    }

    
    public void decrement() {
        remainingTime--;
    }

    public boolean end() {
        return remainingTime <= 0;
    }

    public boolean isAvailable(int currentTime) {
        return arrivalTime <= currentTime && remainingTime > 0;
    }

    public void calculateAllTimes() {
        calculateTurnaroundTime();
        calculateWaitingTime();
    }

     public void calculateTurnaroundTime() {
        this.turnaroundTime = this.finishedAt - this.arrivalTime;
    }

    public void calculateWaitingTime() {
        this.waitingTime = this.turnaroundTime - this.burstTime;
    }
    
    public boolean isQuantumExhausted() {
        if (currentQueue == 0)
            return quantumUsed >= 4;
        if (currentQueue == 1)
            return quantumUsed >= 8;
        return false;
    }

    public boolean execute(int currentTime) {
        if (this.responseTime == -1) {
            this.responseTime = currentTime - this.arrivalTime;
        }
        this.remainingTime--;
        quantumUsed++;
        return this.remainingTime == 0;
    }

    public void resetQuantum() {
        quantumUsed = 0;
    }

    public void boost() {
        currentQueue = 0;
        resetQuantum();
    }

    public void demote() {
        if (currentQueue < 2) {
            currentQueue++;
            resetQuantum();
        }
    }

    public String trace() {
        return String.format(
            "Process [id=%-3d, name=%-6s, arrival=%-3d, burst=%-3d]",
            processId, name, arrivalTime, burstTime
        );
    }

    @Override
    public String toString() {
        return String.format(
            "[id=%-3d, name=%-6s, arrival=%-3d, burst=%-3d, start=%-3d, finish=%-3d, wait=%-3d, TAT=%-3d, resp=%-3d]",
            processId, name, arrivalTime, burstTime, startedAt, finishedAt,
            waitingTime, turnaroundTime, responseTime
        );
    }
}
