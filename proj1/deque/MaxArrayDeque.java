package deque;

import java.util.Comparator;

public class MaxArrayDeque<T> extends ArrayDeque<T> {
    private Comparator<T> comparator;

    public MaxArrayDeque(Comparator<T> c) {
        super();
        comparator = c;
    }

    public T max() {
        if (isEmpty()) {
            return null;
        }

        T maxElement = get(0);
        for (int i = 1; i < size(); i++) {
            T currentElement = get(i);
            if (currentElement != null && (maxElement == null || comparator.compare(currentElement, maxElement) > 0)) {
                maxElement = currentElement;
            }
        }

        return maxElement;
    }

    public T max(Comparator<T> c) {
        if (isEmpty()) {
            return null;
        }

        T maxElement = get(0);
        for (int i = 1; i < size(); i++) {
            T currentElement = get(i);
            if (currentElement != null && (maxElement == null || c.compare(currentElement, maxElement) > 0)) {
                maxElement = currentElement;
            }
        }

        return maxElement;
    }
}
