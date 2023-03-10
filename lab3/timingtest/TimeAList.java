package timingtest;
import edu.princeton.cs.algs4.Stopwatch;

/**
 * Created by hug.
 */
public class TimeAList {
    private static void printTimingTable(AList<Integer> Ns, AList<Double> times, AList<Integer> opCounts) {
        System.out.printf("%12s %12s %12s %12s\n", "N", "time (s)", "# ops", "microsec/op");
        System.out.printf("------------------------------------------------------------\n");
        for (int i = 0; i < Ns.size(); i += 1) {
            int N = Ns.get(i);
            double time = times.get(i);
            int opCount = opCounts.get(i);
            double timePerOp = time / opCount * 1e6;
            System.out.printf("%12d %12.2f %12d %12.2f\n", N, time, opCount, timePerOp);
        }
    }

    public static void main(String[] args) {
        timeAListConstruction();
    }

    public static void timeAListConstruction() {
        // TODO: YOUR CODE HERE
        AList<Integer> Ns = new AList<>();
        AList<Double> times = new AList<>();
        AList<Integer> opCount = new AList<>();
        Stopwatch sw = new Stopwatch();

        AList<Integer> arrayList = new AList<>();
        int x = 1000;
        while (x != 128000) {
            arrayList.addLast(x);
            x = x * 2;
        }
        int aLSize = arrayList.size();
        int i = 0;
        while (i < aLSize) {
            Ns.addLast(arrayList.get(i));
            double timeInSeconds = sw.elapsedTime();

            opCount.addLast(arrayList.get(i));
            AList<Integer> L = new AList<>();
            int j = 0;
            while (j != arrayList.get(i)) {
                L.addLast(j);
                j += 1;
            }
            times.addLast(timeInSeconds);
            i += 1;
        }


        printTimingTable(Ns, times, opCount);
    }
}
