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
    volatile int threadSign = 0;
    int size = 0;

    public void del(T... ts) {
        for (T t : ts) {
            this._del(t);
        }
    }

    synchronized void _del(T t) {
        Node<T> pre = this.foot;
        Node<T> node = this.head;
        for (int i = 0; i < size; i++) {
            if (node.value.equals(t)) {
                if (pre == node) {
                    this.foot = (this.head = (this.index = null));
                } else {
                    pre.next = node.next;
                    if (index == node) {
                        index = node.next;
                    }
                    node.next = null;
                }
                break;
            }
            pre = node;
            node = node.next;
        }
    }

    public synchronized void add(T... ts) {
        new ArrayList<>();
        assert ts != null && ts.length > 0;
        for (T t : ts) {
            assert t != null;
            if (head == null) {
                this.index = (this.foot = (this.head = new Node<>(t)));
            } else {
                this.foot = (this.foot.next = new Node(t));
            }
            this.foot.next = this.head;
        }
        size += ts.length;
    }

    public T getOne() {
        int sign = this.threadSign;
        try {
            return index.value;
        } finally {
            if (sign == this.threadSign) {
                synchronized (this) {
                    if (sign == this.threadSign) {
                        this.index = this.index.next;
                        this.threadSign += 1;
                    }
                }
            }

        }
    }

    public T[] getAll() {
        T[] ts = (T[]) new Object[size];
        Node<T> node = this.head;
        for (int i = 0; i < ts.length; i++) {
            ts[i] = node.value;
            node = node.next;
        }
        return ts;
    }

    class Node<T> {
        T value;
        Node next;

        public Node(T value) {
            this.value = value;
        }
    }
}
