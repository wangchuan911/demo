package org.welisdoon.web.common.dao;

import java.util.List;

public interface ITemplateDao<K,E,C extends E> {
    E get(K key);

    E find(C condition);

    List<E> list(C condition);

    int put(E entity);

    int update(C condition);

    int add(E entity);

    int delete(K key);

    int clear(C condition);
}
