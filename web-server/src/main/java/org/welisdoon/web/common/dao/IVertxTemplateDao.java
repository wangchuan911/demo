package org.welisdoon.web.common.dao;

import java.util.List;
import java.util.concurrent.Future;

public interface IVertxTemplateDao<K, E, C> {
    Future<E> get(K key);

    Future<E> find(C condition);

    Future<List<E>> list(C condition);

    Future<Integer> put(E entity);

    Future<Integer> update(C condition);

    Future<Integer> add(E entity);

    Future<Integer> delete(K key);

    Future<Integer> clear(C condition);
}
