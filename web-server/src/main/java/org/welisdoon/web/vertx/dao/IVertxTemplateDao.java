package org.welisdoon.web.vertx.dao;

import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;

import java.util.List;

public interface IVertxTemplateDao<K, E, C> {
    void get(K key, Handler<AsyncResult<E>> handler);

    void find(C condition, Handler<AsyncResult<E>> handler);

    void list(C condition, Handler<AsyncResult<List<E>>> handler);

    void put(E entity, Handler<AsyncResult<Integer>> handler);

    void update(C condition, Handler<AsyncResult<Integer>> handler);

    void add(E entity, Handler<AsyncResult<Integer>> handler);

    void delete(K key, Handler<AsyncResult<Integer>> handler);

    void clear(C condition, Handler<AsyncResult<Integer>> handler);
}
