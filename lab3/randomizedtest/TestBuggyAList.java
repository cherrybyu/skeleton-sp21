package randomizedtest;

import edu.princeton.cs.algs4.StdRandom;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Created by hug.
 */
public class TestBuggyAList {
  // YOUR TESTS HERE
  @Test
  public void testThreeAddThreeRemove() {
      AListNoResizing<Integer> correct = new AListNoResizing<>();
      BuggyAList<Integer> broken = new BuggyAList<>();

      correct.addLast(5);
      correct.addLast(10);
      correct.addLast(15);

      broken.addLast(5);
      broken.addLast(10);
      broken.addLast(15);

      assertEquals(correct.size(), broken.size());

      assertEquals(correct.removeLast(), broken.removeLast());
      assertEquals(correct.removeLast(), broken.removeLast());
      assertEquals(correct.removeLast(), broken.removeLast());
  }

  @Test
    public void randomizedTest() {
      AListNoResizing<Integer> L = new AListNoResizing<>();
      BuggyAList<Integer> bugg = new BuggyAList<>();
      int N = 5000;
      for (int i = 0; i < N; i += 1) {
          int operationNumber = StdRandom.uniform(0, 4);
          int size = L.size();
          int bugSize = bugg.size();
          if (operationNumber == 0) {
              // addLast
              int randVal = StdRandom.uniform(0, 100);
              L.addLast(randVal);
              bugg.addLast(randVal);

              System.out.println("addLast(" + randVal + ")");
          } else if (operationNumber == 1) {
              System.out.println("size: " + size);
              System.out.println("size: " + bugSize);
          } else if (size > 0 && operationNumber == 2) {
              L.getLast();
              bugg.getLast();
          } else if (size > 0 && operationNumber == 3) {
              L.removeLast();
              bugg.removeLast();
          }
      }
  }
}
