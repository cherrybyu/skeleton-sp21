package deque;
import java.util.Iterator;

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

        size = 0;
    }

    public LinkedListDeque (T value, Node prev, Node next) {
        sentinel = new Node (null, null, null);
        sentinel.next = new Node(value, sentinel, null);

        size = 1;
    }

    public void addFirst(T value) {
        if (size == 0) {
            Node p = new Node(value, sentinel, sentinel);
            sentinel.next = p;
            sentinel.prev = p;
        } else {
            sentinel.next = new Node(value, sentinel, sentinel.next);
            sentinel.next.next.prev = sentinel.next;
        }

        size += 1;
    }

    public void addLast(T value) {
        if (size == 0) {
            Node p = new Node(value, sentinel, sentinel);
            sentinel.next = p;
            sentinel.prev = p;
        } else {
            sentinel.prev = new Node(value, sentinel.prev, sentinel);
            sentinel.prev.prev.next = sentinel.prev;
        }

        size += 1;
    }

    public boolean isEmpty() {
        return sentinel.next == null || sentinel.next == sentinel;
    }

    public int size() {
        return size;
    }

    public void printDeque() {
        Node p = sentinel.next;

        while (p != sentinel) {
            System.out.print(p.value);
            p = p.next;
        }

        System.out.println();
    }

    public T removeFirst() {
        if (sentinel.prev == null || sentinel.prev == sentinel) {
            return null;
        }

        T temp = sentinel.next.value;
        sentinel.next = sentinel.next.next;
        sentinel.next.prev = sentinel;
        size -= 1;

        return temp;
    }

    public T removeLast() {
        if (sentinel.prev == null || sentinel.prev == sentinel) {
            return null;
        }

        T temp = sentinel.prev.value;
        sentinel.prev = sentinel.prev.prev;
        sentinel.prev.next = sentinel;
        size -= 1;

        return temp;
    }

    public T get(int index) {
        if (index > size) {
            return null;
        }

        Node p = sentinel.next;
        int count = 0;

        while (p != sentinel) {
            if (count == index) {
                return p.value;
            } else {
                p = p.next;
                count += 1;
            }
        }
        return null;
    }

//    public Iterator<T> iterator() {
//
//    }
//
//    public boolean equals(Object o) {
//
//    }
}

