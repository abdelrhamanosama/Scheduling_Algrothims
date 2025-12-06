import java.util.LinkedList;
import java.util.Queue;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Comparator;

class RoundRobinScheduler {
    private Queue<Process> readyQueue;
    private int timeQuantum;
    private LinkedList<Process> processes;
    private List<Process> finishedProcesses;
    private Process currentProcess;
    private Process previousProcess;
    // context switch and CPU accounting
    private int contextSwitch = 2;
    private int ctxSwitchTime = 0;
    private int busyTime = 0; // time CPU spent executing processes
    private int idleTime = 0; // time CPU was idle
    
    public RoundRobinScheduler(LinkedList<Process> rawProcesses, int timeQuantum) {
        this.readyQueue = new LinkedList<>();
        this.timeQuantum = timeQuantum;
        this.processes = rawProcesses;
        this.finishedProcesses = new LinkedList<>();
    }

    public void run() {
        System.out.println("╔════════════════════════════════════════════════╗");
        System.out.println("║      Round Robin Scheduler Simulation          ║");
        System.out.println("╚════════════════════════════════════════════════╝\n");

        // Use a priority queue for future arrivals (ordered by arrival time then id)
        PriorityQueue<Process> arrivals = new PriorityQueue<>(
            Comparator.comparingInt(Process::getArrivalTime)
                      .thenComparingInt(Process::getProcessId)
        );
        arrivals.addAll(processes);

        int currentTime = 0;
        int completedProcesses = 0;

        // move initially available processes (arrival time <= 0)
        while (!arrivals.isEmpty() && arrivals.peek().getArrivalTime() <= currentTime) {
            readyQueue.add(arrivals.poll());
        }

        while (completedProcesses < processes.size()) {
            if (!readyQueue.isEmpty()) {
                Process proc = readyQueue.poll();
                previousProcess = currentProcess;
                currentProcess = proc;

                // context switch if switching between processes
                if (previousProcess != null && currentProcess != null && previousProcess.getProcessId() != currentProcess.getProcessId()) {
                    printContextSwitch(currentTime);
                    currentTime += contextSwitch;
                    ctxSwitchTime += contextSwitch;
                }

                if (proc.getResponseTime() == -1) {
                    proc.setStartedAt(currentTime);
                    proc.setResponseTime(currentTime - proc.getArrivalTime());
                }

                int execTime = Math.min(timeQuantum, proc.getRemainingTime());
                int start = currentTime;
                proc.setRemainingTime(proc.getRemainingTime() - execTime);
                currentTime += execTime;
                busyTime += execTime;

                System.out.printf("Time %d-%d: Executing Process %d for %d units\n", start, currentTime, proc.getProcessId(), execTime);
                System.out.println("===");
                try { Thread.sleep(500); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }

                // move newly arrived processes into ready queue
                while (!arrivals.isEmpty() && arrivals.peek().getArrivalTime() <= currentTime) {
                    readyQueue.add(arrivals.poll());
                }

                if (proc.getRemainingTime() > 0) {
                    readyQueue.add(proc);
                } else {
                    proc.setFinishedAt(currentTime);
                    proc.calculateAllTimes();
                    finishedProcesses.add(proc);
                    completedProcesses++;
                    System.out.printf("Time %d: Process %d completed\n", currentTime, proc.getProcessId());
                    System.out.println("===");
                    try { Thread.sleep(500); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
                }
            } else {
                // no ready processes: advance to next arrival
                if (!arrivals.isEmpty()) {
                    int nextArrival = arrivals.peek().getArrivalTime();
                    int old = currentTime;
                    currentTime = Math.max(currentTime + 1, nextArrival);
                    idleTime += Math.max(0, currentTime - old);

                    while (!arrivals.isEmpty() && arrivals.peek().getArrivalTime() <= currentTime) {
                        readyQueue.add(arrivals.poll());
                    }
                } else {
                    break; // nothing left
                }
            }

            try { Thread.sleep(10); } catch (InterruptedException e) { Thread.currentThread().interrupt(); break; }
        }

        System.out.println("\nSimulation completed.");
        printStatistics();
    }


    private void printContextSwitch(int currentTime) {
        String ctxSwitch = String.format(
            "%-15s %s",
            String.format("time %d-%d:", currentTime, currentTime + contextSwitch),
            "Context Switching"
        );
        System.out.println(ctxSwitch + "\n===");
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }



    public void printStatistics() {
        System.out.println("╔════════════════════════════════════════════════╗");
        System.out.println("║      Round Robin Scheduler Statistics          ║");
        System.out.println("╚════════════════════════════════════════════════╝\n");

        if (finishedProcesses.isEmpty()) {
            System.out.println("No finished processes to report.");
            return;
        }

        double totalTurnaround = 0;
        double totalWaiting = 0;
        double totalResponse = 0;

        finishedProcesses.sort((a, b) -> a.getProcessId() - b.getProcessId());

        System.out.println(
            String.format(
                "%-12s %-13s %-13s %-11s %-12s %-13s %-13s %-16s %-13s",
                "Process_id", "Type", "Arrival_time", "Burst_time", "Started_at", "Finished_at", "Waiting_time", "Turnaround_time", "Response_time"
            )
        );
        System.out.println("-".repeat(120));

        for (Process p : finishedProcesses) {
            totalTurnaround += p.getTurnaroundTime();
            totalWaiting += p.getWaitingTime();
            totalResponse += p.getResponseTime();
            
            System.out.println(
                String.format(
                    "%-12d %-13s %-13d %-11d %-12d %-13d %-13d %-16d %-13d",
                    p.getProcessId(), p.getType(), p.getArrivalTime(), p.getBurstTime(), 
                    p.getStartedAt(), p.getFinishedAt(), p.getWaitingTime(), 
                    p.getTurnaroundTime(), p.getResponseTime()
                )
            );
        }
        System.out.println("-".repeat(120));

        System.out.println("╔════════════════════════════════════════════════╗");
        System.out.println("║                  Average Stats                 ║");
        System.out.println("╚════════════════════════════════════════════════╝\n");

        int n = finishedProcesses.size();
        System.out.println(String.format(
            "%-20s = %.2f\n%-20s = %.2f\n%-20s = %.2f",
            "Average turnaround", totalTurnaround / n,
            "Average waiting", totalWaiting / n,
            "Average response", totalResponse / n
        ));
        
        // CPU Utilization
        System.out.println("\n╔════════════════════════════════════════════════╗");
        System.out.println("║                CPU Utilization                 ║");
        System.out.println("╚════════════════════════════════════════════════╝\n");
        int totalTime = busyTime + idleTime + ctxSwitchTime;
        double utilization = totalTime == 0 ? 0.0 : (busyTime / (double) totalTime) * 100.0;
        System.out.println(String.format("%-25s = %d", "Busy time", busyTime));
        System.out.println(String.format("%-25s = %d", "Context-switch time", ctxSwitchTime));
        System.out.println(String.format("%-25s = %d", "Idle time", idleTime));
        System.out.println(String.format("%-25s = %d", "Total simulated time", totalTime));
        System.out.println(String.format("%-25s = %.2f%%", "Utilization (busy/total)", utilization));
    }
}