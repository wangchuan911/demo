package org.welisdoon.metadata.prototype.define;

import org.welisdoon.common.CacheLinkedList;

import java.util.*;

/**
 * @Classname MetaList
 * @Description TODO
 * @Author Septem
 * @Date 18:22
 */
public class MetaProtoList<T extends MetaPrototype> extends CacheLinkedList<T> {
    LifeState state = LifeState.Initial;

    public MetaProtoList() {
        super();
    }

    public MetaProtoList(Collection<? extends T> c) {
        super(c);
    }

    int save() {
        if (state == LifeState.Delete || state == LifeState.Initial) {
            return 0;
        }
        int update = 0;
        for (T t : deleted) {
            if (this.contains(t)) continue;
            update += t.remove();
        }
        for (T t : this) {
            t.save();
        }
        deleted.clear();
        added.clear();
        state = LifeState.Loaded;
        return update;
    }

    int delete() {
        if (state == LifeState.Initial) {
            return 0;
        }
        int update = 0;
        for (T t : this) {
            update += t.remove();
        }
        this.clear();
        for (T t : deleted) {
            update += t.remove();
        }
        deleted.clear();
        for (T t : added) {
            update += t.remove();
        }
        added.clear();
        state = LifeState.Delete;
        return update;
    }

    @Override
    protected T delObject(T o) {
        state = LifeState.Edit;
        return super.delObject(o);
    }

    @Override
    protected boolean delObject(boolean remove, Object o) {
        state = LifeState.Edit;
        return super.delObject(remove, o);
    }

    @Override
    protected void delObject(Collection<? extends T> o) {
        state = LifeState.Edit;
        super.delObject(o);
    }

    @Override
    protected T addObject(T o) {
        state = LifeState.Edit;
        return super.addObject(o);
    }

    @Override
    protected void addObject(Collection<? extends T> o) {
        state = LifeState.Edit;
        super.addObject(o);
    }

    @Override
    protected boolean addObject(boolean remove, Object o) {
        return super.addObject(remove, o);
    }

    public void setState(LifeState state) {
        this.state = state;
    }

    public LifeState getState() {
        return state;
    }

    public List<T> getRemovedObjects() {
        return deleted;
    }

    public static <T extends MetaPrototype> MetaProtoList<T> of(T... ts) {
        return new MetaProtoList<>(Arrays.asList(ts));
    }

    public static final <T extends MetaPrototype> MetaProtoList<T> emptyList() {
        return EmptyList.instance;
    }

    public enum LifeState {
        Initial, Loading, Edit, Loaded, Delete
    }

    final static class EmptyList<T extends MetaPrototype> extends MetaProtoList<T> {
        static EmptyList instance = new EmptyList();

        @Override
        public boolean add(T t) {
            throw new IllegalStateException("不支持");
        }

        @Override
        public boolean addAll(Collection<? extends T> c) {
            throw new IllegalStateException("不支持");
        }

        @Override
        public boolean addAll(int index, Collection<? extends T> c) {
            throw new IllegalStateException("不支持");
        }

        @Override
        public void add(int index, T element) {
            throw new IllegalStateException("不支持");
        }

        @Override
        public void addFirst(T t) {
            throw new IllegalStateException("不支持");
        }

        @Override
        public void addLast(T t) {
            throw new IllegalStateException("不支持");
        }
    }
}
