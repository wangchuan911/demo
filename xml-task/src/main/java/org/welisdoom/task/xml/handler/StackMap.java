package org.welisdoom.task.xml.handler;

import java.util.*;

/**
 * @Classname StackMap
 * @Description TODO
 * @Author Septem
 * @Date 10:35
 */
public class StackMap<K, V> implements Map<K, V> {
    LinkedList<Map<K, V>> deque = new LinkedList<>();

    public StackMap() {
        deque.add(new HashMap<>());
    }

    @Override
    public int size() {
        return 0;
    }

    @Override
    public boolean isEmpty() {
        return false;
    }

    @Override
    public boolean containsKey(Object key) {
        return false;
    }

    @Override
    public boolean containsValue(Object value) {
        return false;
    }

    @Override
    public V get(Object key) {
        V v = null, v2;
        for (Map<K, V> kvMap : deque) {
            v2 = kvMap.get(key);
            if (v2 != null) {
                v = v2;
            }
        }
        return v;
    }

    @Override
    public V put(K key, V value) {
        return deque.peekLast().put(key, value);
    }

    @Override
    public V remove(Object key) {
        return null;
    }

    @Override
    public void putAll(Map<? extends K, ? extends V> m) {

    }

    @Override
    public void clear() {

    }

    @Override
    public Set<K> keySet() {
        return null;
    }

    @Override
    public Collection<V> values() {
        return null;
    }

    @Override
    public Set<Entry<K, V>> entrySet() {
        return null;
    }
}
