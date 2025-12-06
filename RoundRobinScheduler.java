import java.util.LinkedList;
import java.util.Queue;
import java.util.List;

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
        
        // Sort processes by arrival time
        processes.sort((p1, p2) -> Integer.compare(p1.getArrivalTime(), p2.getArrivalTime()));
        
        // Start two threads: one for adding processes, one for simulation
        Thread addingThread = new Thread(this::addProcessesThread, "Process-Adding-Thread");
        Thread simulationThread = new Thread(this::simulationThread, "Simulation-Thread");
        
        addingThread.start();
        simulationThread.start();
        
        try {
            addingThread.join();
            simulationThread.join();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        printStatistics();
    }

    private void addProcessesThread() {
        // This thread manages adding processes to the ready queue based on arrival time
        int index = 0;
        int currentTime = 0;
        
        while (index < processes.size()) {
            Process process = processes.get(index);
            if (process.getArrivalTime() <= currentTime) {
                synchronized (readyQueue) {
                    readyQueue.add(process);
                    index++;
                }
            }
            currentTime++;
            
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
    }

    private void simulationThread() {
        // This thread handles the actual scheduling simulation
        int currentTime = 0;
        int completedProcesses = 0;
        int processIndex = 0;

        while (completedProcesses < processes.size()) {
            synchronized (readyQueue) {
                // Add any processes that have arrived by current time
                while (processIndex < processes.size() && 
                       processes.get(processIndex).getArrivalTime() <= currentTime) {
                    readyQueue.add(processes.get(processIndex));
                    processIndex++;
                }

                if (!readyQueue.isEmpty()) {
                    Process currentProc = readyQueue.poll();
                    previousProcess = currentProcess;
                    currentProcess = currentProc;

                    // account for context switch if switching between different processes
                    if (previousProcess != null && currentProcess != null &&
                        previousProcess.getProcessId() != currentProcess.getProcessId()) {
                        printContextSwitch(currentTime);
                        currentTime += contextSwitch;
                        ctxSwitchTime += contextSwitch;
                    }

                    int execTime = Math.min(timeQuantum, currentProc.getRemainingTime());
                    int startTime = currentTime;
                    
                    // Set response time if first execution
                    if (currentProc.getResponseTime() == -1) {
                        currentProc.setStartedAt(currentTime);
                        currentProc.setResponseTime(currentTime - currentProc.getArrivalTime());
                    }

                    // Execute for execTime
                    currentProc.setRemainingTime(currentProc.getRemainingTime() - execTime);
                    currentTime += execTime;
                    // CPU busy time increases by execTime
                    busyTime += execTime;

                    System.out.printf("Time %d-%d: Executing Process %d for %d units\n", 
                                      startTime, currentTime, currentProc.getProcessId(), execTime);
                    System.out.println("===");
                    
                    try {
                        Thread.sleep(500); // pause 500 milliseconds for visualization
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }

                    if (currentProc.getRemainingTime() > 0) {
                        readyQueue.add(currentProc);
                    } else {
                        currentProc.setFinishedAt(currentTime);
                        // Use Process methods to calculate times
                        currentProc.calculateAllTimes();
                        completedProcesses++;
                        finishedProcesses.add(currentProc);
                        System.out.printf("Time %d: Process %d completed\n", currentTime, currentProc.getProcessId());
                        System.out.println("===");
                        try {
                            Thread.sleep(500); // pause 500 milliseconds for visualization
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                        }
                    }
                } else {
                    // Ready queue is empty, advance time to next arrival
                    if (processIndex < processes.size()) {
                        int old = currentTime;
                        currentTime = processes.get(processIndex).getArrivalTime();
                        // account for idle time (fast-forward)
                        idleTime += Math.max(0, currentTime - old);
                    }
                }
            }

            try {
                Thread.sleep(10); // Small delay to allow adding thread to work
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
        
        System.out.println("\nSimulation completed.");
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