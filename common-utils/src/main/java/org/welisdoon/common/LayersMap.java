package org.welisdoon.common;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @Classname LayersMap
 * @Description TODO
 * @Author Septem
 * @Date 11:50
 */
public class LayersMap<K, V> implements Map<K, V> {

    final protected LayersMap<K, V> parent;
    final protected Map<K, V> current;

    public LayersMap(Map<K, V> current, LayersMap<K, V> parent) {
        this.parent = parent;
        this.current = current;
        check();
    }

    public LayersMap(Map<K, V> current) {
        this(current, null);
    }

    public LayersMap<K, V> lowerLayersMap() {
        return new LayersMap<>(new HashMap<>(), this);
    }

    protected void check() {
        final Object key = "!@#$%^&*()_";
        final Object val = 1;
        ((Map) current).put(key, val);
        current.remove(key);
    }


    @Override
    public int size() {
        return current.size() + (parent != null ? parent.size() : 0);
    }

    @Override
    public boolean isEmpty() {
        return current.isEmpty() || (parent == null || parent.isEmpty());
    }

    @Override
    public boolean containsKey(Object key) {
        return current.containsKey(key) || (parent != null && parent.containsKey(key));
    }

    @Override
    public boolean containsValue(Object value) {
        return current.containsValue(value) || (parent != null && parent.containsValue(value));
    }

    @Override
    public V get(Object key) {
        V v = current.get(key);
        return v != null ? v : parent != null ? parent.get(key) : null;
    }

    @Override
    public V put(K key, V value) {
        return current.put(key, value);
    }

    public V put(K key, V value, boolean deep) {
        if (!deep) {
            return put(key, value);
        }
        if (!current.containsKey(key)) {
            LayersMap<K, V> map = parent;
            while (map != null) {
                if (map.containsKey(key))
                    return map.put(key, value);
                map = map.parent;
            }
        }
        return current.put(key, value);
    }

    @Override
    public V remove(Object key) {
        return current.remove(key);
    }

    @Override
    public void putAll(Map<? extends K, ? extends V> m) {
        current.putAll(m);
    }

    public void putAll(Map<? extends K, ? extends V> m, boolean deep) {
        if (!deep) {
            current.putAll(m);
            return;
        }
        for (Entry<? extends K, ? extends V> entry : m.entrySet()) {
            put(entry.getKey(), entry.getValue(), true);
        }
    }

    @Override
    public void clear() {
        current.clear();
    }

    public void clear(boolean deep) {
        current.clear();
        if (deep && parent != null)
            parent.clear(true);

    }

    @Override
    public Set<K> keySet() {
        Set<K> kSet = parent != null ? parent.keySet() : new HashSet<>();
        kSet.addAll(current.keySet());
        return kSet;
    }

    @Override
    public Collection<V> values() {
        return entrySet().stream().map(Entry::getValue).collect(Collectors.toList());
    }

    @Override
    public Set<Entry<K, V>> entrySet() {
        Set<Entry<K, V>> entries = parent != null ? parent.entrySet() : new HashSet<>();
        entries.addAll(current.entrySet());
        return entries;
    }
}
