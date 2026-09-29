package org.welisdoon.common.object;

import org.welisdoon.common.data.BaseCondition;

import javax.naming.OperationNotSupportedException;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * @Classname PageIterator
 * @Description TODO
 * @Author Septem
 * @Date 19:38
 */
public class PageList<T> implements List<T> {
    final Function<BaseCondition.Page, List<T>> queryFunction;
    final BaseCondition.Page page;
    int size = 0;
    List<T> list;

    public PageList(BaseCondition.Page page, Function<BaseCondition.Page, List<T>> queryFunction) {
        this.page = page;
        this.queryFunction = queryFunction;
    }

    public PageList(Function<BaseCondition.Page, List<T>> queryFunction) {
        this(new BaseCondition.Page(1, 100), queryFunction);
    }

    @Override
    public int size() {
        return size;
    }

    @Override
    public boolean isEmpty() {
        return size() == 0;
    }

    @Override
    public boolean contains(Object o) {
        return false;
    }

    @Override
    public Iterator<T> iterator() {
        return new PageIterator<>(queryFunction);
    }

    @Override
    public Object[] toArray() {
        Object[] objects = new Object[size];
        Iterator<T> tIterator = iterator();
        for (int i = 0; i < objects.length && tIterator.hasNext(); i++) {
            objects[i] = tIterator.next();
        }
        return objects;
    }

    @Override
    public <T1> T1[] toArray(T1[] a) {
        if (a.length < size)
            a = (T1[]) java.lang.reflect.Array.newInstance(
                    a.getClass().getComponentType(), size);
        Iterator<T> tIterator = iterator();
        for (int i = 0; i < a.length && tIterator.hasNext(); i++) {
            a[i] = (T1) tIterator.next();
        }
        return a;

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

    }

    @Override
    public T get(int index) {
        checkElementIndex(index);
        if (index >= page.getStart() && index < page.getEnd()) {
            return list.get((int) (index - page.getStart()));
        } else {
            page.setPage((index / page.getPageSize()) + 1);
            list = queryFunction.apply(page);
            return this.get(index);
        }
    }

    private String outOfBoundsMsg(int index) {
        return "Index: " + index + ", Size: " + size;
    }

    private void checkElementIndex(int index) {
        if (!isRange(index))
            throw new IndexOutOfBoundsException(outOfBoundsMsg(index));
    }

    public boolean isRange(int index) {
        if (index < 0) {
            return false;
        }
        if (index > size) {
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
        return new PageIterator<>(index, queryFunction);
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

}
