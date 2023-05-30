package deque;

import org.junit.Test;
import static org.junit.Assert.*;
public class ArrayListDequeTest {
    @Test
    /** Adds a few things to the list, checking isEmpty() and size() are correct,
     * finally printing the results.
     *
     * && is the "and" operation. */
    public void addIsEmptySizeTest() {

        System.out.println("Make sure to uncomment the lines below (and delete this print statement).");

        LinkedListDeque<String> ald1 = new ArrayListDeque<String>();

        assertTrue("A newly initialized ALDeque should be empty", ald1.isEmpty());
        ald1.addFirst("front");

        // The && operator is the same as "and" in Python.
        // It's a binary operator that returns true if both arguments true, and false otherwise.
        assertEquals(1, ald1.size());
        assertFalse("lld1 should now contain 1 item", ald1.isEmpty());

        ald1.addLast("middle");
        assertEquals(2, ald1.size());

        ald1.addLast("back");
        assertEquals(3, ald1.size());

        System.out.println("Printing out deque: ");
        ald1.printDeque();
    }

    @Test
    /** Adds an item, then removes an item, and ensures that dll is empty afterwards. */
    public void addRemoveTest() {

        System.out.println("Make sure to uncomment the lines below (and delete this print statement).");

        LinkedListDeque<Integer> ald1 = new ArrayListDeque<Integer>();
        // should be empty
        assertTrue("lld1 should be empty upon initialization", ald1.isEmpty());

        ald1.addFirst(10);
        // should not be empty
        assertFalse("lld1 should contain 1 item", ald1.isEmpty());

        ald1.removeFirst();
        // should be empty
        assertTrue("lld1 should be empty after removal", ald1.isEmpty());
    }

    @Test
    /* Tests removing from an empty deque */
    public void removeEmptyTest() {

        System.out.println("Make sure to uncomment the lines below (and delete this print statement).");

        LinkedListDeque<Integer> ald1 = new ArrayListDeque<>();
        ald1.addFirst(3);

        ald1.removeLast();
        ald1.removeFirst();
        ald1.removeLast();
        ald1.removeFirst();

        int size = ald1.size();
        String errorMsg = "  Bad size returned when removing from empty deque.\n";
        errorMsg += "  student size() returned " + size + "\n";
        errorMsg += "  actual size() returned 0\n";

        assertEquals(errorMsg, 0, size);
    }

    @Test
    /* Check if you can create LinkedListDeques with different parameterized types*/
    public void multipleParamTest() {
        ArrayListDeque<String>  ald1 = new ArrayListDeque<String>();
        ArrayListDeque<Double>  ald2 = new ArrayListDeque<Double>();
        ArrayListDeque<Boolean> ald3 = new ArrayListDeque<Boolean>();

        ald1.addFirst("string");
        ald2.addFirst(3.14159);
        ald3.addFirst(true);

        String s = ald1.removeFirst();
        double d = ald2.removeFirst();
        boolean b = ald3.removeFirst();
    }

    @Test
    /* check if null is return when removing from an empty LinkedListDeque. */
    public void emptyNullReturnTest() {
        System.out.println("Make sure to uncomment the lines below (and delete this print statement).");

        ArrayListDeque<Integer> ald1 = new ArrayListDeque<Integer>();

        boolean passed1 = false;
        boolean passed2 = false;
        assertEquals("Should return null when removeFirst is called on an empty Deque,", null, ald1.removeFirst());
        assertEquals("Should return null when removeLast is called on an empty Deque,", null, ald1.removeLast());
    }

    @Test
    /* Add large number of elements to deque; check if order is correct. */
    public void bigALDequeTest() {
        System.out.println("Make sure to uncomment the lines below (and delete this print statement).");

        ArrayListDeque<Integer> ald1 = new ArrayListDeque<Integer>();
        for (int i = 0; i < 1000000; i++) {
            ald1.addLast(i);
        }

        for (double i = 0; i < 500000; i++) {
            assertEquals("Should have the same value", i, (double) ald1.removeFirst(), 0.0);
        }

        for (double i = 999999; i > 500000; i--) {
            assertEquals("Should have the same value", i, (double) ald1.removeLast(), 0.0);
        }
    }
}
