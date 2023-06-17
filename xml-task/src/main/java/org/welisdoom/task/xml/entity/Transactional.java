package org.welisdoom.task.xml.entity;


import io.vertx.core.CompositeFuture;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.sqlclient.SqlConnection;
import io.vertx.sqlclient.Transaction;
import org.welisdoom.task.xml.annotations.Attr;
import org.welisdoom.task.xml.annotations.Tag;
import org.welisdoom.task.xml.intf.type.Executable;
import org.welisdoon.common.LogUtils;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @Classname Transactional
 * @Description TODO
 * @Author Septem
 * @Date 17:59
 */
@Tag(value = "transactional", parentTagTypes = Executable.class, desc = "事务")
@Attr(name = "id", desc = "唯一标识")
@Attr(name = "link", desc = "database的Id")
public class Transactional extends Unit implements Executable {
//    Map<TaskRequest, Map.Entry<SqlConnection, Transaction>> MAP = new HashMap<>();

    @Override
    protected void start(TaskRequest data, Object preUnitResult, Promise<Object> toNext) {
        compose(getSqlConnection(data), connection -> {
            Promise<Object> promise = Promise.promise();
            super.start(data, preUnitResult, promise);

            return promise.future().transform(event -> {
                /*Map.Entry<SqlConnection, Transaction> entry = data.cache(this);
                if (event.succeeded())
                    return entry.getValue().commit();
                else
                    return entry.getValue().rollback();*/
                return CompositeFuture.all(this.allTransaction(data).stream().map(entry -> {
                    if (event.succeeded())
                        return entry.getValue().commit();
                    else
                        return entry.getValue().rollback();
                }).collect(Collectors.toList()));
            }).transform(event -> {
                Map.Entry<SqlConnection, Transaction> entry = data.clearCache(this);
                return entry.getKey().close();
            });
        }).onComplete(event -> {
            if (event.succeeded()) {
                toNext.complete();
            } else {
                toNext.fail(event.cause());
            }
        });
        /*compose(this.getDatabase(data), connection ->
                compose(connection.begin(), transaction -> {
                    data.cache(this, Map.entry(connection, transaction));
                    Promise<Object> promise = Promise.promise();
                    super.start(data, preUnitResult, promise);

                    return promise.future().transform(event -> {
                        Map.Entry<SqlConnection, Transaction> entry = data.cache(this);
                        if (event.succeeded())
                            return entry.getValue().commit();
                        else
                            return entry.getValue().rollback();
                    }).onComplete(event -> {
                        data.clearCache(this);
                        connection.close();
                    });
                })
        ).onComplete(event -> {
            if (event.succeeded()) {
                toNext.complete();
            } else {
                toNext.fail(event.cause());
            }
        });*/
        /*this.getDatabase(data).compose(connection ->
                        connection.begin().compose(transaction -> {
                            data.cache(this, Map.entry(connection, transaction));
//                    MAP.put(data, Map.entry(connection, transaction));
                            Promise<Object> promise = Promise.promise();
                            Future<Object> future = promise.future().onSuccess(o1 -> {
                                *//*MAP.get(data)*//*
                                ((Map.Entry<SqlConnection, Transaction>) data.cache(this)).getValue()
                                        .commit()
                                        .onSuccess(unused -> {
                                            toNext.complete(o1);
                                        }).onFailure(toNext::fail);
                            }).onFailure(throwable -> {
                                *//*MAP.get(data)*//*
                                ((Map.Entry<SqlConnection, Transaction>) data.cache(this)).getValue()
                                        .rollback()
                                        .onSuccess(unused -> {
                                            toNext.fail(throwable);
                                        }).onFailure(toNext::fail);
                            }).onComplete(objectAsyncResult -> {
                                *//*MAP.remove(data)*//*
                                ((Map.Entry<SqlConnection, Transaction>) data.clearCache(this)).getKey().close();
                            });
                            super.start(data, preUnitResult, promise);
                            return future;
                        })
        ).onFailure(toNext::fail);*/
    }


    public Future<SqlConnection> getSqlConnection(TaskRequest data) {
        Map.Entry<SqlConnection, Transaction> cache = data.cache(this);
        if (cache != null)
            return Future.succeededFuture(cache.getKey());
        Future<SqlConnection> future = Database.getDatabase(attributes.get("link")).getConnect(attributes.get("link"), data);
        return compose(future, connection -> {
            /*log(data.id);
            log(connection);*/
            return compose(connection.begin(), transaction -> {
                data.cache(this, Map.entry(connection, transaction));
                return Future.succeededFuture(connection);
            });
        });
    }

    @Override
    public Future<Void> destroy(TaskRequest taskRequest) {
        return super.destroy(taskRequest);
    }

    public Future<?> commit(TaskRequest data) {
        /*log("提交");
        log(MAP.get(data));*/
        return compose(((Map.Entry<SqlConnection, Transaction>) data.cache(this)).getValue().commit(), voidAsyncResult ->
                newTransaction(data)
        );
    }

    @Override
    protected Future<Void> hook(TaskRequest taskRequest) {
        return CompositeFuture.all(allTransaction(taskRequest).stream().map(entry ->
                CompositeFuture.all(Arrays.asList(
                        entry
                                .getValue()
                                .rollback()
                                .onSuccess(unused -> {
                                    log(LogUtils.styleString("", 31, 1, "事务终止"));
                                })
                                .onFailure(throwable -> {
                                    log(LogUtils.styleString("", 31, 1, "事务终止 回滚失败"));
                                    throwable.printStackTrace();
                                }),
                        entry.getKey()
                                .close()
                                .onSuccess(unused -> {
                                    log(LogUtils.styleString("", 31, 1, "连接终止"));
                                })
                                .onFailure(throwable -> {
                                    log(LogUtils.styleString("", 31, 1, "连接终止失败"));
                                    throwable.printStackTrace();
                                })
                ))
        ).collect(Collectors.toList())).transform(compositeFutureAsyncResult -> {
            clearCache(taskRequest);
            return super.hook(taskRequest);
        });
    }

    protected List<Map.Entry<SqlConnection, Transaction>> allTransaction(TaskRequest data) {
        List<Map.Entry<SqlConnection, Transaction>> list = new LinkedList<>();
        Map.Entry<SqlConnection, Transaction> map = data.cache(this);
        if (map != null)
            list.add(map);
        for (TaskRequest taskRequest : data.childrenRequest) {
            list.addAll(allTransaction(taskRequest));
        }
        return list;
    }

    public Future<?> rollback(TaskRequest data) {
        /*log("回滚");
        log(MAP1.get(data));*/
        return
                compose(((Map.Entry<SqlConnection, Transaction>) data.cache(this)).getValue().rollback(), voidAsyncResult ->
                        newTransaction(data)
                );
    }

    protected Future<Transaction> newTransaction(TaskRequest data) {
        Map.Entry<SqlConnection, Transaction> cache = data.cache(this);
        Future<SqlConnection> future = compose((Future) cache.getKey().close(),
                o -> Database.getDatabase(attributes.get("link")).getConnect(attributes.get("link"), data));
        return compose(future, connection ->
                connection.begin().onSuccess(transaction -> {
                    /*MAP.put(data, Map.entry(connection, transaction));*/
                    data.cache(this, Map.entry(connection, transaction));
                }));
    }

    protected void clearCache(TaskRequest data) {
        data.clearCache(this);
        for (TaskRequest taskRequest : data.childrenRequest) {
            taskRequest.clearCache(this);
        }
    }
}
