package deque;

import java.util.Iterator;

public class ArrayDeque<T> implements Iterable<T> {
    private T[] items;
    private int size;
    private Integer tail;
    private Integer head;

    public ArrayDeque() {
        items = (T[]) new Object[8];
        size = 0;
        tail = null;
        head = null;
    }

    private void resize(int capacity) {
        T[] temp = (T[]) new Object[capacity];
        int i = 0;
        for (T item: this) {
            temp[i] = item;
            i++;
        }
        items = temp;
        head = 0;
        tail = size - 1;
    }

    private int getCircularIndex(int pos) {
        if (pos < 0) {
            return pos + items.length;
        } else {
            return pos % items.length;
        }
    }

    private void initalPos() {
        head = 0;
        tail = 0;
    }


    public void addFirst(T item) {
        if (size == items.length) {
            resize(items.length * 2);
        }

        if (head == null && tail == null) {
            initalPos();
        } else {
            head = getCircularIndex(head - 1);
        }

        items[head] = item;
        size ++;
    }

    public void addLast(T item) {
        if (size == items.length) {
            resize(items.length * 2);
        }

        if (head == null && tail == null) {
            initalPos();
        } else {
            tail++;
        }

        items[tail] = item;
        size++;
    }

    public boolean isEmpty() {
        return size == 0;
    }

    public int size() {
        return size;
    }

    public void printDeque() {
        for (T item: this) {
            System.out.println(item);
        }
    }

    public T removeFirst() {
        if (size == 0) {return null;}

        T value = items[head];
        items[head] = null;
        head++;
        size--;

        return value;
    }

    public T removeLast() {
        if (size == 0) {return null;}

        T value = items[tail];
        items[tail] = null;
        tail--;
        size--;

        return value;
    }

    public T get(int index) {
        if (index > size) {
            return null;
        }
        return items[(head + index) % items.length];
    }

    public Iterator<T> iterator() {
        return new ArrayDequeIterator();
    }

    private class ArrayDequeIterator implements Iterator<T> {
        private int currentPosition;
        private int count = 0;
        public ArrayDequeIterator() {
            currentPosition = head;
        }

        public boolean hasNext() {
            return count < size;
        }

        public T next() {
            T item = items[currentPosition];
            currentPosition = (currentPosition + 1) % items.length;
            count ++;
            return item;
        }
    }
}
