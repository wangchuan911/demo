package org.welisdoon.web.common;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @Classname Pool
 * @Description TODO
 * @Author wang.zhidong
 * @Date 2021/12/23 19:46
 */
public class Pool<T> {
    Node<T> index;
    Node<T> head;
    Node<T> foot;
    AtomicInteger timer = new AtomicInteger(4);
    int timerCount;

    public Pool(int timerCount) {
        assert timerCount > 1;
        this.timerCount = timerCount;
    }

    public Pool() {
        this(4);
    }

    public synchronized void add(T... ts) {
        new ArrayList<>();
        assert ts != null && ts.length > 0;
        for (T t : ts) {
            if (head == null) {
                this.head = new Node<>(t);
                this.foot = this.head;
                continue;
            }
            this.foot = (this.foot.next = new Node(t));
        }
    }

    public T getOneWithLazy() {
        if (timer.getAndDecrement() <= 0 || index == null || this.head != this.foot) {
            timer.set(this.timerCount);
            return getOne();
        }
        return index.value;
    }

    public synchronized T getOne() {
        if (index == null || index.next == null)
            index = head;
        else {
            index = index.next;
        }
        return index.value;
    }

    public T[] getAll() {
        List<T> list = new LinkedList();
        Node<T> n = this.head;
        while (n != null) {
            list.add(n.value);
            n = n.next;
        }
        return (T[]) list.stream().toArray();
    }

    class Node<T> {
        T value;
        Node next;

        public Node(T value) {
            this.value = value;
        }
    }
}
