import java.io.File;
import java.io.FileNotFoundException;
import java.util.LinkedList;
import java.util.Scanner;

public class Schudular {
    public static void main(String[] args) {
        LinkedList<Process> processes = new LinkedList<>();
        String algorithms[] = {
                "FCFS", "SJF", "SRT",
                "priority preemptive",
                "priority nonPreemptive",
                "round robin",
                "multi level queue",
                "multi level queue feedback"
        };

        try {
            // ---- Read from file ----
            File file = new File("input.txt");
            Scanner cin = new Scanner(file);

            int numberOfProcesses = cin.nextInt();
            System.out.println("Reading processes from file...");
            System.out.println("---------------------------------------------------------------");

            for (int i = 0, processID = 0; i < numberOfProcesses; i++) {
                String name = cin.next();
                int arrivalTime = cin.nextInt();
                int burstTime = cin.nextInt();
                int priorityTime = cin.nextInt();
                String type = cin.next();

                processes.add(new Process(
                        name,
                        arrivalTime,
                        burstTime,
                        priorityTime,
                        ProcessType.from(type)
                ));
            }

            cin.close();

        } catch (FileNotFoundException e) {
            System.out.println("ERROR: input.txt not found!");
            return;
        }

        // Debug: print loaded processes
        System.out.println("Processes loaded:");
        for (Process p : processes) {
            System.out.println(p);
        }
    }
}
