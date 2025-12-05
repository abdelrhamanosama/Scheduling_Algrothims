
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

public class MLFQScheduler {
    private Queue<Process>[] queues;
    private LinkedList<Process> allProcesses;
    private int currentTime;
    private int contextSwitches;
    private int lastBoostTime;
    private Process currentProcess;

    private static final int BOOST_INTERVAL = 20;

    public MLFQScheduler(List<Process> processes) {
        Queue<Process>[] q = (Queue<Process>[]) new LinkedList[3];
        this.queues = q;
        this.queues[0] = new LinkedList<>();
        this.queues[1] = new LinkedList<>();
        this.queues[2] = new LinkedList<>();

        this.allProcesses = new LinkedList<>(processes);
        this.currentTime = 0;
        this.contextSwitches = 0;
        this.lastBoostTime = 0;
        this.currentProcess = null;
    }

    public void run() {
        System.out.println("=== MLFQ Scheduler Started ===\n");

        while (!isComplete()) {

            if (currentTime - lastBoostTime >= BOOST_INTERVAL && currentTime > 0) {
                performPriorityBoost();
                lastBoostTime = currentTime;
            }

            handleArrivals();

            if (currentProcess == null) {
                currentProcess = selectNextProcess();
                if (currentProcess != null) {
                    contextSwitches++;
                }
            }

            if (currentProcess != null) {
                executeProcess(currentProcess);
            } else {

                currentTime++;
            }

            updateWaitingTimes();
        }

        calculateMetrics();
    }

    private void handleArrivals() {
        for (Process p : allProcesses) {
            if (p.getArrivalTime() == currentTime) {
                queues[0].offer(p);
                p.currentQueue = 0;

                if (currentProcess != null && currentProcess.currentQueue > 0) {
                    queues[currentProcess.currentQueue].offer(currentProcess);
                    currentProcess = null;
                }
            }
        }
    }

    private Process selectNextProcess() {

        for (int i = 0; i < 3; i++) {
            if (!queues[i].isEmpty()) {
                return queues[i].poll();
            }
        }
        return null;
    }

    private void executeProcess(Process p) {
        boolean completed = p.execute(currentTime);
        currentTime++;

        if (completed) {

            p.setFinishedAt(currentTime);
            currentProcess = null;
            printProcessStatuses(p);
        } else if (p.isQuantumExhausted()) {

            p.demote();
            queues[p.currentQueue].offer(p);
            currentProcess = null;
        }
    }
    private void printProcessStatuses(Process completedProcess) {
        System.out.println("[Time " + currentTime + "] Process '" + completedProcess.getName() + "' completed. Statuses:");
        for (Process p : allProcesses) {
            String status;
            if (p.getRemainingTime() == 0) {
                status = "terminated";
            } else if (p == currentProcess) {
                status = "running";
            } else if (isInQueues(p)) {
                status = "ready";
            } else {
                status = "waiting";
            }

            System.out.println(String.format("  %s : %s", p.getName(), status));
        }
        System.out.println();
    }
        private boolean isInQueues(Process p) {
        for (Queue<Process> queue : queues) {
            if (queue.contains(p))
                return true;
        }
        return false;
    }

    private void performPriorityBoost() {

        LinkedList<Process> toBoost = new LinkedList<>();

        for (int i = 1; i < 3; i++) {
            toBoost.addAll(queues[i]);
            queues[i].clear();
        }

        if (currentProcess != null && currentProcess.currentQueue > 0) {
            toBoost.add(currentProcess);
            currentProcess = null;
        }

        for (Process p : toBoost) {
            p.boost();
            queues[0].offer(p);
        }

        if (!toBoost.isEmpty()) {
            System.out.println("[Time " + currentTime + "] Priority boost: " +
                    toBoost.size() + " processes moved to Q0");
        }
    }

    private void updateWaitingTimes() {
        for (Process p : allProcesses) {
            if (p.getArrivalTime() <= currentTime && p.getRemainingTime() > 0 && p != currentProcess) {
                p.setWaitingTime(p.getWaitingTime()+1);
            }
        }
    }

    private boolean isComplete() {

        for (Process p : allProcesses) {
            if (p.getRemainingTime() > 0) {
                return false;
            }
        }
        return true;
    }

    private boolean hasRemainingProcesses() {
        for (Process p : allProcesses) {
            if (p.getArrivalTime() > currentTime) {
                return true;
            }
        }
        return false;
    }

    private void calculateMetrics() {
        for (Process p : allProcesses) {
            p.setTurnaroundTime(  p.getFinishedAt() - p.getArrivalTime() );
        }
    }

    public void displayResults() {

        System.out.println("=== PROCESS METRICS ===");
        System.out.println(String.format("%-10s %-8s %-8s %-10s %-10s %-10s %-10s",
                "Process", "Arrival", "Burst", "Completion", "Waiting", "Turnaround", "Response"));
        System.out.println("-".repeat(78));

        int totalWaiting = 0;
        int totalTurnaround = 0;
        int totalResponse = 0;

        for (Process p : allProcesses) {
            System.out.println(p.toString());

            totalWaiting += p.getWaitingTime();
            totalTurnaround += p.getTurnaroundTime();
            totalResponse += p.getResponseTime();
        }

        int n = allProcesses.size();
        System.out.println("-".repeat(78));
        System.out.println(String.format("Average: %46s %-10.2f %-10.2f %-10.2f",
                "", (double) totalWaiting / n, (double) totalTurnaround / n, (double) totalResponse / n));

        System.out.println("\n=== SCHEDULER METRICS ===");
        System.out.println("Context Switches: " + contextSwitches);
        System.out.println("Total Time: " + currentTime);
        System.out.println();
    }
}
