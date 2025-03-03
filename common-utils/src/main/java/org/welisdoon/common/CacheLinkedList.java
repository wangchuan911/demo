package org.welisdoon.common;

import java.util.Collection;
import java.util.LinkedList;
import java.util.ListIterator;

/**
 * @Classname CacheLinkedList
 * @Description TODO
 * @Author Septem
 * @Date 9:19
 */
public class CacheLinkedList<E> extends LinkedList<E> {
    protected LinkedList<E> deleted = new LinkedList<>();
    protected LinkedList<E> added = new LinkedList<>();

    public CacheLinkedList() {
        super();
    }

    public CacheLinkedList(Collection<? extends E> c) {
        super(c);
    }

    @Override
    public void add(int index, E element) {
        super.add(index, element);
        addObject(element);
    }

    @Override
    public void addLast(E e) {
        super.addLast(e);
        addObject(e);
    }

    @Override
    public void addFirst(E e) {
        super.addFirst(e);
        addObject(e);
    }

    @Override
    public boolean addAll(int index, Collection<? extends E> c) {
        boolean flag = super.addAll(index, c);
        addObject(c);
        return flag;
    }

    @Override
    public boolean addAll(Collection<? extends E> c) {
        boolean flag = super.addAll(c);
        addObject(c);
        return flag;
    }

    @Override
    public boolean add(E e) {
        boolean flag = super.add(e);
        addObject(e);
        return flag;
    }


    @Override
    public boolean removeLastOccurrence(Object o) {
        return delObject(super.removeLastOccurrence(o), o);
    }


    @Override
    public E removeLast() {
        return delObject(super.removeLast());
    }


    @Override
    public E removeFirst() {
        return delObject(super.removeFirst());
    }

    @Override
    public boolean remove(Object o) {
        return delObject(super.remove(o), o);
    }


    @Override
    public E remove() {
        return delObject(super.remove());
    }

    @Override
    public E remove(int index) {
        return delObject(super.remove(index));
    }

    @Override
    public void clear() {
        delObject(this);
        super.clear();
    }

    protected E delObject(E o) {
        if (o != null) {
            deleted.add(o);
        }
        return o;
    }

    protected boolean delObject(boolean remove, Object o) {
        if (remove) {
            deleted.add((E) o);
        }
        return remove;
    }

    protected void delObject(Collection<? extends E> o) {
        if (o != null) {
            deleted.addAll(o);
        }
    }

    protected E addObject(E o) {
        if (o != null) {
            added.add(o);
        }
        return o;
    }

    protected void addObject(Collection<? extends E> o) {
        if (o != null) {
            added.addAll(o);
        }
    }

    protected boolean addObject(boolean remove, Object o) {
        if (remove) {
            added.add((E) o);
        }
        return remove;
    }

    private class CacheLinkedListIterator implements ListIterator<E> {
        ListIterator<E> iterator;
        E current;

        public CacheLinkedListIterator(ListIterator<E> iterator) {
            this.iterator = iterator;
        }

        @Override
        public boolean hasNext() {
            return iterator.hasNext();
        }

        @Override
        public E next() {
            return current = iterator.next();
        }

        @Override
        public boolean hasPrevious() {
            return iterator.hasPrevious();
        }

        @Override
        public E previous() {
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
                CacheLinkedList.this.deleted.add(current);
            iterator.remove();
        }

        @Override
        public void set(E t) {
            iterator.set(t);
            if (current != null) {
                CacheLinkedList.this.deleted.add(current);
                current = t;
            }
            if (t != null)
                CacheLinkedList.this.added.add(t);
        }

        @Override
        public void add(E t) {
            iterator.add(t);
            added.add(t);
        }
    }

}
