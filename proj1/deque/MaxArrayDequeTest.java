package deque;

import org.junit.Test;

import java.util.Comparator;

public class MaxArrayDequeTest {
    @Test
    public static void main(String[] args) {
        // Test using natural ordering (Comparable)

        // Create a MaxArrayDeque of integers using natural ordering
        MaxArrayDeque<Integer> deque = new MaxArrayDeque<Integer>(Comparator.naturalOrder());

        // Add elements to the deque
        deque.addLast(10);
        deque.addLast(5);
        deque.addLast(8);
        deque.addLast(2);
        deque.addLast(15);

        // Test max() method
        Integer maxElement = deque.max();
        System.out.println("Max element: " + maxElement);  // Expected output: 15

        // Test max(Comparator<T> c) method
        Integer maxElementWithComparator = deque.max(Comparator.reverseOrder());
        System.out.println("Max element with reverse order comparator: " + maxElementWithComparator);
        // Expected output: 2 (since reverse order will consider 2 as the maximum element)

        // Test using a custom comparator

        // Create a MaxArrayDeque of strings using a custom length-based comparator
        MaxArrayDeque<String> stringDeque = new MaxArrayDeque<>(Comparator.comparingInt(String::length));

        // Add elements to the deque
        stringDeque.addLast("apple");
        stringDeque.addLast("banana");
        stringDeque.addLast("pear");
        stringDeque.addLast("kiwi");

        // Test max() method with custom comparator
        String maxLengthString = stringDeque.max();
        System.out.println("Max length string: " + maxLengthString);  // Expected output: "banana"

        // Test max(Comparator<T> c) method with custom comparator
        String maxLengthStringWithComparator = stringDeque.max(Comparator.reverseOrder());
        System.out.println("Max length string with reverse order comparator: " + maxLengthStringWithComparator);
        // Expected output: "kiwi" (since reverse order will consider "kiwi" as the maximum length string)
    }
}
