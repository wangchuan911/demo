package org.welisdoon.metadata.prototype.handle;

import java.util.LinkedList;
import java.util.Queue;

/**
 * @Classname HandleParamater
 * @Description TODO
 * @Author Septem
 * @Date 18:15
 */
public class HandleParameter {
    Queue<Object> deque = new LinkedList<>();
    Object current;

    public synchronized <T> T getCurrentInstance() {
        return (T) current;
    }

    public synchronized <T> T getParentInstance() {
        return (T) deque.peek();
    }

    public synchronized void setCurrentInstance(Object currentInstance) {
        if (current != null)
            deque.offer(current);
        current = currentInstance;
    }

    public synchronized void delCurrentInstance() {
        current = deque.poll();
    }
}
