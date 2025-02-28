package org.welisdoon.metadata.prototype.define;

import java.util.LinkedList;
import java.util.List;

/**
 * @Classname MetaList
 * @Description TODO
 * @Author Septem
 * @Date 18:22
 */
public class MetaList<T extends MetaPrototype> extends LinkedList<T> {
    protected List<T> deleteMetaObjects = new LinkedList<>();

    @Override
    public boolean remove(Object o) {
        if (super.remove(o)) {
            deleteMetaObjects.add((T) o);
            return true;
        }
        return false;
    }

    @Override
    public void clear() {
        deleteMetaObjects.addAll(this);
        super.clear();
    }

    public void save() {
        for (T deleteMetaObject : deleteMetaObjects) {
            if (this.contains(deleteMetaObject)) continue;
            deleteMetaObject.remove();
        }
        deleteMetaObjects.clear();
    }
}
