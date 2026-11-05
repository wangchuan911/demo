package org.welisdoon.common.object.wrapper.dao.base;

import org.welisdoon.common.object.PageIterator;
import org.welisdoon.common.object.PageList;

import java.util.*;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * @Classname PageIterator
 * @Description TODO
 * @Author Septem
 * @Date 19:38
 */
public class LazyEntityPageList<T extends IEntity> implements List<T> {

    PageList<LazyEntity> list;

    public LazyEntityPageList(PageList<LazyEntity> list) {
        this.list = list;
    }

    @Override
    public int size() {
        return list.size();
    }

    @Override
    public boolean isEmpty() {
        return list.isEmpty();
    }

    @Override
    public boolean contains(Object o) {
        return list.contains(o);
    }

    @Override
    public Iterator<T> iterator() {
        return new LazyEntityPageIterator<T>((PageIterator<LazyEntity>) list.iterator());
    }

    @Override
    public Object[] toArray() {
        return list.toArray();
    }

    @Override
    public <T1> T1[] toArray(T1[] a) {
        return list.toArray(a);

    }

    @Override
    public boolean add(T t) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean remove(Object o) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean containsAll(Collection<?> c) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean addAll(Collection<? extends T> c) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean addAll(int index, Collection<? extends T> c) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean retainAll(Collection<?> c) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void clear() {
        throw new UnsupportedOperationException();
    }

    @Override
    public T get(int index) {
        LazyEntity lazyEntity = list.get(index);

        return (T) lazyEntity;
    }

    private String outOfBoundsMsg(int index) {
        return "Index: " + index + ", Size: " + list.size();
    }

    private void checkElementIndex(int index) {
        if (!isRange(index))
            throw new IndexOutOfBoundsException(outOfBoundsMsg(index));
    }

    public boolean isRange(int index) {
        if (index < 0) {
            return false;
        }
        if (index > list.size()) {
            return false;
        }
        return true;
    }

    @Override
    public T set(int index, T element) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void add(int index, T element) {
        throw new UnsupportedOperationException();
    }

    @Override
    public T remove(int index) {
        throw new UnsupportedOperationException();
    }

    @Override
    public int indexOf(Object o) {
        return 0;
    }

    @Override
    public int lastIndexOf(Object o) {
        return 0;
    }

    @Override
    public ListIterator<T> listIterator() {
        return null;
    }

    @Override
    public ListIterator<T> listIterator(int index) {
        return new LazyEntityPageIterator((PageIterator) list.listIterator(index));
    }

    @Override
    public List<T> subList(int fromIndex, int toIndex) {
        throw new UnsupportedOperationException();
    }

    public Spliterator<T> spliterator() {
        return Spliterators.spliteratorUnknownSize(iterator(), 0);
    }


    public Stream<T> stream() {
        return StreamSupport.stream(spliterator(), false);
    }


    public Stream<T> parallelStream() {
        return StreamSupport.stream(spliterator(), true);
    }

    protected static class LazyEntity implements IEntity<Object> {
        IEntity builder;
        boolean loaded = false;
        AnnotationBuildInfo buildInfo;
        Map<String, Object> params;

        LazyEntity(AnnotationBuildInfo buildInfo, Map<String, Object> map) {
            this.buildInfo = buildInfo;
            this.params = map;
        }

        <T> T build() {
            if (!loaded) {
                loaded = true;
                builder = buildInfo.toEntity(this);
            }
            return (T) builder;
        }

        @Override
        public Map<String, Object> data() {
            return params;
        }
    }

    static class LazyEntityPageIterator<T> implements ListIterator<T> {
        PageIterator<LazyEntity> iterator;

        public LazyEntityPageIterator(PageIterator<LazyEntity> iterator) {
            this.iterator = iterator;
        }

        @Override
        public boolean hasNext() {
            return iterator.hasNext();
        }

        @Override
        public T next() {
            return iterator.next().build();
        }

        @Override
        public boolean hasPrevious() {
            return iterator.hasPrevious();
        }

        @Override
        public T previous() {
            return iterator.previous().build();
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
            throw new UnsupportedOperationException();
        }

        @Override
        public void set(T t) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void add(T t) {
            throw new UnsupportedOperationException();
        }
    }
}
