package org.welisdoon.metadata.prototype.define;

import org.welisdoon.common.CacheLinkedList;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class MirrorList<E> extends CacheLinkedList<E> {
    final List<E> mirror;

    public MirrorList(List<E> init, List<E> mirror) {
        super();
        this.mirror = mirror;
        this.addAll(init);
    }

    public MirrorList(List<E> mirror) {
        this(Collections.emptyList(), mirror);
    }

    @Override
    protected boolean delObject(boolean remove, Object o) {
        if (remove) {
            mirror.remove(o);
        }
        return remove;
    }

    @Override
    protected E delObject(E o) {
        if (o != null) {
            mirror.remove(o);
        }
        return o;
    }

    @Override
    protected void delObject(Collection<? extends E> o) {
        if (o != null) {
            mirror.removeAll(o);
        }
    }

    @Override
    protected boolean addObject(boolean remove, Object o) {
        if (remove) {
            mirror.add((E) o);
        }
        return remove;
    }

    @Override
    protected E addObject(E o) {
        if (o != null) {
            mirror.add(o);
        }
        return o;
    }

    @Override
    protected void addObject(Collection<? extends E> o) {
        if (o != null) {
            mirror.addAll(o);
        }
    }

}
