package org.welisdoom.task.xml.entity;


import io.vertx.core.Future;
import io.vertx.sqlclient.SqlConnection;
import io.vertx.sqlclient.Transaction;
import org.welisdoom.task.xml.annotations.Attr;
import org.welisdoom.task.xml.annotations.Tag;
import org.welisdoom.task.xml.intf.type.Executable;
import org.welisdoon.common.LogUtils;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
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
    Connection connection;


    @Override
    protected void startSync(TaskSession data) throws Throwable {
        connection = Database.getDataSource(attributes.get("link")).getConnection();
        try {
            super.startSync(data);
        } catch (Break.BreakLoopThrowable | Break.SkipOneLoopThrowable e) {
            connection.commit();
        } catch (Throwable e) {
            connection.rollback();
            throw e;
        }
        if (!connection.isClosed())
            connection.close();
        connection = null;
    }

    @Override
    @Deprecated
    protected Future start(TaskSession data, Object preUnitResult) {
        return getSqlConnection(data).compose(connection -> {
            return super.start(data, preUnitResult).transform(event -> {
                /*Map.Entry<SqlConnection, Transaction> entry = data.cache(this);
                if (event.succeeded())
                    return entry.getValue().commit();
                else
                    return entry.getValue().rollback();*/
                return Future.join(this.allTransaction(data).stream().map(entry -> {
                    if (event.succeeded())
                        return entry.getValue().commit();
                    else
                        return entry.getValue().rollback();
                }).collect(Collectors.toList()));
            }).transform(event -> {
                Map.Entry<SqlConnection, Transaction> entry = data.clearCache(this);
                return entry.getKey().close();
            });
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


    @Deprecated
    public Future<SqlConnection> getSqlConnection(TaskSession data) {
        Map.Entry<SqlConnection, Transaction> cache = data.cache(this);
        if (cache != null)
            return Future.succeededFuture(cache.getKey());
        Future<SqlConnection> future = Database.getDatabase(attributes.get("link")).getConnect(attributes.get("link"), data);
        return future.compose(connection -> {
            /*log(data.id);
            log(connection);*/
            return connection.begin().compose(transaction -> {
                data.cache(this, Map.entry(connection, transaction));
                return Future.succeededFuture(connection);
            });
        });
    }

    @Override
    public Future<Void> destroy(TaskSession taskSession) {
        return super.destroy(taskSession);
    }

    @Deprecated
    public Future<?> commit(TaskSession data) {
        /*log("提交");
        log(MAP.get(data));*/
        return ((Map.Entry<SqlConnection, Transaction>) data.cache(this)).getValue().commit().compose(voidAsyncResult ->
                newTransaction(data)
        );
    }

    @Deprecated
    @Override
    protected Future<Void> hook(TaskSession taskSession) {
        return Future.join(allTransaction(taskSession).stream().map(entry ->
                Future.join(Arrays.asList(
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
            clearCache(taskSession);
            return super.hook(taskSession);
        });
    }

    @Override
    protected void hookSync(TaskSession taskSession) {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.rollback();
                log(LogUtils.styleString("", 31, 1, "事务终止"));
                if (!connection.isClosed())
                    connection.close();
                log(LogUtils.styleString("", 31, 1, "连接终止"));
            }
        } catch (SQLException e) {
            log(LogUtils.styleString("", 31, 1, "连接终止失败"));
            e.printStackTrace();
        }
        clearCache(taskSession);
        super.hookSync(taskSession);
    }

    @Deprecated
    protected List<Map.Entry<SqlConnection, Transaction>> allTransaction(TaskSession data) {
        List<Map.Entry<SqlConnection, Transaction>> list = new LinkedList<>();
        Map.Entry<SqlConnection, Transaction> map = data.cache(this);
        if (map != null)
            list.add(map);
        for (TaskSession taskSession : data.childrenSession) {
            list.addAll(allTransaction(taskSession));
        }
        return list;
    }

    @Deprecated
    public Future<?> rollback(TaskSession data) {
        /*log("回滚");
        log(MAP1.get(data));*/
        return
                ((Map.Entry<SqlConnection, Transaction>) data.cache(this)).getValue().rollback().compose(voidAsyncResult ->
                        newTransaction(data)
                );
    }

    @Deprecated
    protected Future<Transaction> newTransaction(TaskSession data) {
        Map.Entry<SqlConnection, Transaction> cache = data.cache(this);
        Future<SqlConnection> future = (Future) cache.getKey().close().compose(
                o -> Database.getDatabase(attributes.get("link")).getConnect(attributes.get("link"), data));
        return future.compose(connection ->
                connection.begin().onSuccess(transaction -> {
                    /*MAP.put(data, Map.entry(connection, transaction));*/
                    data.cache(this, Map.entry(connection, transaction));
                }));
    }

    protected void clearCache(TaskSession data) {
        data.clearCache(this);
        for (TaskSession taskSession : data.childrenSession) {
            taskSession.clearCache(this);
        }
    }
}
