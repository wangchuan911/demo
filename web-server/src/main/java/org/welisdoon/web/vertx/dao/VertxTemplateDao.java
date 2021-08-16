package org.welisdoon.web.vertx.dao;

import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import org.welisdoon.web.common.dao.ITemplateDao;

import java.util.List;

/**
 * @Classname VertixTemplateDao
 * @Description TODO
 * @Author wang.zhidong
 * @Date 2021/8/13 15:16
 */
public abstract class VertxTemplateDao<K, E, C> implements IVertxTemplateDao<K, E, C> {

    ITemplateDao<K, E, C> templateDao;

    public void setTemplateDao(ITemplateDao<K, E, C> templateDao) {
        this.templateDao = templateDao;
    }

    @Override
    public void get(K key, Handler<AsyncResult<E>> handler) {
        Future<E> future = Future.future(ePromise -> {
            ePromise.complete(templateDao.get(key));
        });
        future.onComplete(handler);
    }

    @Override
    public void find(C condition, Handler<AsyncResult<E>> handler) {
        Future<E> future = Future.future(ePromise -> {
            ePromise.complete(templateDao.find(condition));
        });
        future.onComplete(handler);
    }

    @Override
    public void list(C condition, Handler<AsyncResult<List<E>>> handler) {
        Future<List<E>> future = Future.future(ePromise -> {
            ePromise.complete(templateDao.list(condition));
        });
        future.onComplete(handler);
    }

    @Override
    public void put(E entity, Handler<AsyncResult<Integer>> handler) {
        Future<Integer> future = Future.future(ePromise -> {
            ePromise.complete(templateDao.put(entity));
        });
        future.onComplete(handler);
    }

    @Override
    public void update(C condition, Handler<AsyncResult<Integer>> handler) {
        Future<Integer> future = Future.future(ePromise -> {
            ePromise.complete(templateDao.update(condition));
        });
        future.onComplete(handler);
    }

    @Override
    public void add(E entity, Handler<AsyncResult<Integer>> handler) {
        Future<Integer> future = Future.future(ePromise -> {
            ePromise.complete(templateDao.add(entity));
        });
        future.onComplete(handler);
    }

    @Override
    public void delete(K key, Handler<AsyncResult<Integer>> handler) {
        Future<Integer> future = Future.future(ePromise -> {
            ePromise.complete(templateDao.delete(key));
        });
        future.onComplete(handler);
    }

    @Override
    public void clear(C condition, Handler<AsyncResult<Integer>> handler) {
        Future<Integer> future = Future.future(ePromise -> {
            ePromise.complete(templateDao.clear(condition));
        });
        future.onComplete(handler);
    }
}
