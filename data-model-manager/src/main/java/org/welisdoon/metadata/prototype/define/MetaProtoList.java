package org.welisdoon.metadata.prototype.define;

import java.util.*;

/**
 * @Classname MetaList
 * @Description TODO
 * @Author Septem
 * @Date 18:22
 */
public class MetaProtoList<T> extends LinkedList<T> {
    protected List<T> removeObjects = new LinkedList<>();

    public MetaProtoList() {
        super();
    }

    /**
     * Constructs a list containing the elements of the specified
     * collection, in the order they are returned by the collection's
     * iterator.
     *
     * @param c the collection whose elements are to be placed into this list
     * @throws NullPointerException if the specified collection is null
     */
    public MetaProtoList(Collection<? extends T> c) {
        super(c);
    }

    @Override
    public boolean remove(Object o) {
        if (super.remove(o)) {
            removeObjects.add((T) o);
            return true;
        }
        return false;
    }

    public ListIterator<T> listIterator(final int index) {
        return new MetaListIterator(super.listIterator(index));
    }

    private class MetaListIterator implements ListIterator<T> {
        ListIterator<T> iterator;
        T current;

        public MetaListIterator(ListIterator<T> iterator) {
            this.iterator = iterator;
        }

        @Override
        public boolean hasNext() {
            return iterator.hasNext();
        }

        @Override
        public T next() {
            return current = iterator.next();
        }

        @Override
        public boolean hasPrevious() {
            return iterator.hasPrevious();
        }

        @Override
        public T previous() {
            return current = iterator.previous();
        }

        @Override
        public int nextIndex() {
            return iterator.nextIndex();
        }

        @Override
        public int previousIndex() {
            return iterator.previousIndex();
        }

        @Override
        public void remove() {
            if (current != null)
                removeObjects.add(current);
            iterator.remove();
        }

        @Override
        public void set(T t) {
            iterator.set(t);
        }

        @Override
        public void add(T t) {
            iterator.add(t);
        }
    }

    @Override
    public void clear() {
        removeObjects.addAll(this);
        super.clear();
    }

    public List<T> getRemovedObjects() {
        return removeObjects;
    }

    public static final <T> MetaProtoList<T> emptyList() {
        return EmptyList.instance;
    }


    final static class EmptyList<T> extends MetaProtoList<T> {
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
