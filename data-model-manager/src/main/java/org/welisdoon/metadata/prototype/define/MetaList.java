package org.welisdoon.metadata.prototype.define;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

/**
 * @Classname MetaList
 * @Description TODO
 * @Author Septem
 * @Date 18:22
 */
public class MetaList<T> extends LinkedList<T> {
    protected List<T> deleteObjects = new LinkedList<>();

    public MetaList() {
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
    public MetaList(Collection<? extends T> c) {
        super(c);
    }

    @Override
    public boolean remove(Object o) {
        if (super.remove(o)) {
            deleteObjects.add((T) o);
            return true;
        }
        return false;
    }

    @Override
    public void clear() {
        deleteObjects.addAll(this);
        super.clear();
    }

    public List<T> getDeleteMetaObjects() {
        return deleteObjects;
    }
}
