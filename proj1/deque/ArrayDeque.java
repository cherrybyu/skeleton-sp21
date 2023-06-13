package deque;

import java.util.Iterator;
import java.util.Objects;

public class ArrayDeque<T> {
    private T[] items;
    private int size;
    private int front;
    private int back;

    public ArrayDeque() {
        items = (T[]) new Object[8];
        size = 0;
        front = 0;
        back = 0;
    }

    private void resize(int capacity) {
        T[] a = (T[]) new Object[capacity];
        System.arraycopy(items, 0, a, 0, size);
        items = a;
    }

    public void addFirst(T item) {
        if (size == items.length) {
            resize(size * 2);
        }

        back = (back - 1 + items.length) % items.length;
        items[back] = item;
        size++;
    }

    public void addLast(T item) {
        if (size == items.length) {
            resize(size * 2);
        }

        front = (front + 1) % items.length;
        items[front] = item;
        size++;
    }

    public boolean isEmpty() {
        return size == 0;
    }

    public int size() {
        return size;
    }

    public void printDeque() {
        int index = back;
        for (int i = 0; i < size; i++) {
            System.out.print(items[index] + " ");
            index = (index + 1) % items.length;
        }
        System.out.println();
    }

    public T removeFirst() {
        if (isEmpty()) {
            return null;
        }

        T item = items[back];
        items[back] = null;
        back = (back + 1) % items.length;
        size--;

        if (items.length >= 16 && size < items.length / 4) {
            resize(items.length / 2);
        }

        return item;
    }

    public T removeLast() {
        if (isEmpty()) {
            return null;
        }

        T item = items[front];
        items[front] = null;
        front = (front - 1 + items.length) % items.length;
        size--;

        if (items.length >= 16 && size < items.length / 4) {
            resize(items.length / 2);
        }

        return item;
    }

    public T get(int index) {
        if (index >= 0 && index < size) {
            int position = (back + index) % items.length;
            return items[position];
        }

        return null;
    }

    public Iterator<T> iterator() {
        return new ArrayDequeIterator();
    }

    private class ArrayDequeIterator implements Iterator<T> {
        private int currentPosition;

        public ArrayDequeIterator() {
            currentPosition = back;
        }

        public boolean hasNext() {
            return currentPosition != front;
        }

        public T next() {
            T item = items[currentPosition];
            currentPosition = (currentPosition + 1) % items.length;
            return item;
        }
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ArrayDeque<?> other = (ArrayDeque<?>) o;
        if (size() != other.size()) {
            return false;
        }
        Iterator<T> iterator = iterator();
        Iterator<?> otherIterator = other.iterator();
        while (iterator.hasNext()) {
            T item = iterator.next();
            Object otherItem = otherIterator.next();
            if (!Objects.equals(item, otherItem)) {
                return false;
            }
        }
        return true;
    }
}
