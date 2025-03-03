package org.welisdoon.metadata.prototype.define;

import org.welisdoon.common.CacheLinkedList;

import java.util.*;

/**
 * @Classname MetaList
 * @Description TODO
 * @Author Septem
 * @Date 18:22
 */
public class MetaProtoList<T> extends CacheLinkedList<T> {

    public MetaProtoList() {
    }

    public MetaProtoList(Collection<? extends T> c) {
        super(c);
    }

    void save() {

    }

    void delete() {

    }

    public List<T> getRemovedObjects() {
        return deleted;
    }

    public static <T> MetaProtoList<T> of(T... ts) {
        return new MetaProtoList<>(Arrays.asList(ts));
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
