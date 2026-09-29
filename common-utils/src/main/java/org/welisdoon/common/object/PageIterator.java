package org.welisdoon.common.object;

import org.welisdoon.common.data.BaseCondition;
import org.welisdoon.common.object.wrapper.IDataAccessObject;

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
public class PageIterator<T> implements ListIterator<T> {
    Map.Entry<List<T>, ListIterator<T>> iterator;
    final Function<BaseCondition.Page, List<T>> queryFunction;
    final BaseCondition.Page page;

    public PageIterator(BaseCondition.Page page, Function<BaseCondition.Page, List<T>> queryFunction) {
        this.page = page;
        this.queryFunction = queryFunction;
    }

    public PageIterator(Function<BaseCondition.Page, List<T>> queryFunction) {
        this(new BaseCondition.Page(1, 100), queryFunction);
    }

    public PageIterator(int index, Function<BaseCondition.Page, List<T>> queryFunction) {
        this(queryFunction);
        int offset = computeAndGetOffset(index);
        setIterator(offset);
    }

    protected void setIterator(List<T> list, int index) {
        if (index == page.getStart()) {
            iterator = Map.entry(list, list.listIterator());
        } else {
            iterator = Map.entry(list, list.listIterator((int) (index - page.getStart())));
        }
    }

    protected void setIterator() {
        setIterator((int) page.getStart());
    }

    protected void setIterator(int index) {
        setIterator(queryFunction.apply(page), index);
    }

    protected int computeAndGetOffset(int index) {
        page.setPage((index / page.getPage()) + 1);
        return index % page.getPage();
    }

    @Override
    public boolean hasNext() {
        if (iterator == null) {
            setIterator();
        } else if (iterator.getKey().isEmpty()) {
            return false;
        } else if (!iterator.getValue().hasNext()) {
            page.nextPage();
            setIterator();
        }
        return iterator.getValue().hasNext();
    }

    @Override
    public T next() {
        return iterator.getValue().next();
    }

    @Override
    public boolean hasPrevious() {
        if (iterator == null) {
            setIterator();
        } else if (!iterator.getValue().hasNext()) {
            page.lastPage();
            setIterator();
        }
        return iterator.getValue().hasNext();
    }

    @Override
    public T previous() {
        return iterator.getValue().next();
    }

    @Override
    public int nextIndex() {
        return (int) (iterator.getValue().nextIndex() + page.getStart());
    }

    @Override
    public int previousIndex() {
        return (int) (iterator.getValue().previousIndex() + page.getStart());
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


    public Spliterator<T> spliterator() {
        return Spliterators.spliteratorUnknownSize(this, 0);
    }


    public Stream<T> stream() {
        return StreamSupport.stream(spliterator(), false);
    }

    public Stream<T> parallelStream() {
        return StreamSupport.stream(spliterator(), true);
    }

    public void reset() {
        page.setPage(1);
        iterator = null;
    }
}
