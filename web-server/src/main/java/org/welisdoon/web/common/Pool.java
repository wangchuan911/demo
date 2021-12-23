package org.welisdoon.web.common;

import java.util.Deque;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
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

    public synchronized void add(T t) {
        assert t != null;
        if (head == null) {
            this.head = new Node<>(t);
            this.foot = this.head;
            return;
        }
        this.foot = (this.foot.next = new Node(t));
    }

    public T getCacheOne() {
        if (index == null || timer.get() <= 0)
            return getOne();
        timer.decrementAndGet();
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

    class Node<T> {
        T value;
        Node next;

        public Node(T value) {
            this.value = value;
        }
    }
}
