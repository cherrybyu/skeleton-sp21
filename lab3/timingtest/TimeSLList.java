package timingtest;
import edu.princeton.cs.algs4.Stopwatch;

/**
 * Created by hug.
 */
public class TimeSLList {
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
        timeGetLast();
    }

    public static void timeGetLast() {
        // TODO: YOUR CODE HERE
        AList<Integer> Ns = new AList<>();
        AList<Double> times = new AList<>();
        AList<Integer> opCount = new AList<>();
        Stopwatch sw = new Stopwatch();

        AList<Integer> L = new AList<>();
        int x = 1000;
        while (x != 128000) {
            L.addLast(x);
            x = x * 2;
        }
        int i = 0;
        while (i < L.size()) {
            Ns.addLast(L.get(i));
            SLList <Integer> list = new SLList<>();
            int j = 0;
            while (j != L.get(i)) {
                list.addFirst(j);
                j += 1;
            }
            double timeInSeconds = sw.elapsedTime();
            int m = 0;
            while (m != 10000) {
                list.getLast();
                m += 1;
            }times.addLast(timeInSeconds);

            opCount.addLast(m);
            i += 1;
        }
        printTimingTable(Ns, times, opCount);

    }

}
