package deque;
import java.util.Iterator;
import java.util.NoSuchElementException;

public class LinkedListDeque <T> {
    private Node sentinel;
    private int size;
    private class Node {
        private T value;
        private Node next, prev;

        public Node(T value, Node prev, Node next) {
            this.value = value;
            this.prev = prev;
            this.next = next;
        }
    }

    public LinkedListDeque () {
        sentinel = new Node (null, null, null);
        sentinel.next = sentinel;
        sentinel.prev = sentinel;
        size = 0;

    }

    public void addFirst(T value) {
        Node newNode = new Node(value, sentinel, sentinel.next);
        sentinel.next.prev = newNode;
        sentinel.next = newNode;
        size++;
    }

    public void addLast(T value) {
        Node newNode = new Node(value, sentinel.prev, sentinel);
        sentinel.prev.next = newNode;
        sentinel.prev = newNode;
        size++;
    }

    public boolean isEmpty() {
        return size == 0;
    }

    public int size() {
        return size;
    }

    public void printDeque() {
        Node p = sentinel.next;

        while (p != sentinel) {
            System.out.print(p.value + " ");
            p = p.next;
        }

        System.out.println();
    }

    public T removeFirst() {
        if (isEmpty()) {
            return null;
        }

        Node first = sentinel.next;
        sentinel.next = first.next;
        first.next.prev = sentinel;
        size--;

        return first.value;
    }

    public T removeLast() {
        if (isEmpty()) {
            return null;
        }

        Node last = sentinel.prev;
        sentinel.prev = last.prev;
        last.prev.next = sentinel;
        size--;

        return last.value;
    }

    public T get(int index) {
        if (index >= size) {
            return null;
        }

        Node p = sentinel.next;
        int count = 0;

        while (count < index) {
            p = p.next;
            count++;
        }

        return p.value;
    }

    public Iterator<T> iterator() {
        return new DequeIterator();
    }

    private class DequeIterator implements Iterator<T> {
        private Node current = sentinel.next;

        public boolean hasNext() {
            return current != sentinel;
        }

        public T next() {
            if (!hasNext()) {
                throw new NoSuchElementException();
            }
            T value = current.value;
            current = current.next;
            return value;
        }

        public void remove() {
            throw new UnsupportedOperationException();
        }
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        LinkedListDeque<?> other = (LinkedListDeque<?>) o;

        if (size != other.size) {
            return false;
        }

        Iterator<T> thisIterator = iterator();
        Iterator<?> otherIterator = other.iterator();

        while (thisIterator.hasNext()) {
            T thisValue = thisIterator.next();
            Object otherValue = otherIterator.next();
            if (thisValue == null) {
                if (otherValue != null) {
                    return false;
                }
            } else if (!thisValue.equals(otherValue)) {
                return false;
            }
        }

        return true;
    }
}


