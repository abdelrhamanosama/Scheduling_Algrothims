import java.util.Arrays;
import java.util.LinkedList;

public class Main {
   public static void main(String[] args) {
      Process a = new Process(0, 12, ProcessType.BATCH);
      Process b = new Process(3, 7, ProcessType.SYSTEM);
      Process c = new Process(6, 2, ProcessType.BATCH);
      Process d = new Process(8, 5, ProcessType.SYSTEM);
      Process e = new Process(9, 2, ProcessType.BATCH);
      Process f = new Process(12, 10, ProcessType.REAL_TIME);
      Process g = new Process(13, 11, ProcessType.INTERACTIVE);
      Process h = new Process(15, 7, ProcessType.SYSTEM);
      Process i = new Process(18, 8, ProcessType.INTERACTIVE);
      Process j = new Process(21, 15, ProcessType.REAL_TIME);
      Process k = new Process(25, 6, ProcessType.INTERACTIVE);

      LinkedList<Process> rawProcesses = new LinkedList<>();
      rawProcesses.addAll(Arrays.asList(a, b, c, d, e, f, g, h, i, j, k));      

      // MQScheduler mqScheduler = new MQScheduler(rawProcesses);
      // mqScheduler.run();

      var RRS = new RoundRobinScheduler(rawProcesses, 6);
      RRS.run();
   }
}  
