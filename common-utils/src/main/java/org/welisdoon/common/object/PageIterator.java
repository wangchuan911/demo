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
public class PageIterator<T> implements Iterator<T> {
    Iterator<T> iterator;
    final Function<BaseCondition.Page, Collection<T>> queryFunction;
    boolean more;
    final BaseCondition.Page page;
    int position = -1;

    public PageIterator(BaseCondition.Page page, Function<BaseCondition.Page, Collection<T>> queryFunction) {
        this.page = page;
        this.queryFunction = queryFunction;
    }

    public PageIterator(Function<BaseCondition.Page, Collection<T>> queryFunction) {
        this(new BaseCondition.Page(1, 100), queryFunction);
    }

    @Override
    public boolean hasNext() {
        if (iterator == null) {
            iterator = queryFunction.apply(page).iterator();
            more = iterator.hasNext();
        }
        if (!more) {
            return false;
        } else if (!iterator.hasNext()) {
            iterator = queryFunction.apply(page.nextPage()).iterator();
            more = iterator.hasNext();
        }
        return iterator.hasNext();
    }

    @Override
    public T next() {
        position++;
        return iterator.next();
    }

    public boolean hasMore() {
        return more;
    }

    public int getPosition() {
        return position;
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
