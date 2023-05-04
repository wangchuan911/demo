package org.welisdoom.task.xml.entity;


import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.sqlclient.SqlConnection;
import io.vertx.sqlclient.Transaction;
import org.welisdoom.task.xml.annotations.Attr;
import org.welisdoom.task.xml.annotations.Tag;
import org.welisdoom.task.xml.intf.type.Executable;
import org.welisdoon.common.LogUtils;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

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


    protected Future<? extends SqlConnection> getDatabase(TaskRequest data) {
        return Database.getDatabase(data, attributes.get("link")).getConnect(attributes.get("link"), data);
    }

    @Override
    protected void start(TaskRequest data, Object preUnitResult, Promise<Object> toNext) {
        this.getDatabase(data).compose(connection ->
                        connection.begin().compose(transaction -> {
                            data.cache(this, Map.entry(connection, transaction));
//                    MAP.put(data, Map.entry(connection, transaction));
                            Promise<Object> promise = Promise.promise();
                            Future<Object> future = promise.future().onSuccess(o1 -> {
                                /*MAP.get(data)*/
                                ((Map.Entry<SqlConnection, Transaction>) data.cache(this)).getValue()
                                        .commit()
                                        .onSuccess(unused -> {
                                            toNext.complete(o1);
                                        }).onFailure(toNext::fail);
                            }).onFailure(throwable -> {
                                /*MAP.get(data)*/
                                ((Map.Entry<SqlConnection, Transaction>) data.cache(this)).getValue()
                                        .rollback()
                                        .onSuccess(unused -> {
                                            toNext.fail(throwable);
                                        }).onFailure(toNext::fail);
                            }).onComplete(objectAsyncResult -> {
                                /*MAP.remove(data)*/
                                ((Map.Entry<SqlConnection, Transaction>) data.clearCache(this)).getKey().close();
                            });
                            super.start(data, preUnitResult, promise);
                            return future;
                        })
        ).onFailure(toNext::fail);
    }


    public SqlConnection getSqlConnection(TaskRequest request) {
        return /*MAP.get(request)*/((Map.Entry<SqlConnection, Transaction>) request.cache(this)).getKey();
    }

    @Override
    public void destroy(TaskRequest taskRequest) {
        super.destroy(taskRequest);
    }

    public Future<?> commit(TaskRequest data) {
        /*log("提交");
        log(MAP.get(data));*/
        return ((Map.Entry<SqlConnection, Transaction>) data.cache(this)).getValue().commit().compose(voidAsyncResult ->
                newTransaction(data)
        );
    }

    @Override
    protected void hook(TaskRequest taskRequest) {
        Map.Entry<SqlConnection, Transaction> entry = taskRequest.clearCache(this);
        try {
            entry
                    .getValue()
                    .rollback()
                    .onSuccess(unused -> {
                        System.out.println(LogUtils.styleString("", 31, 1, "事务终止"));
                    })
                    .onFailure(throwable -> {
                        System.out.println(LogUtils.styleString("", 31, 1, "事务终止 回滚失败"));
                        throwable.printStackTrace();
                    });
        } catch (Throwable e) {
            e.printStackTrace();
        }
        try {
            entry.getKey()
                    .close()
                    .onSuccess(unused -> {
                        System.out.println(LogUtils.styleString("", 31, 1, "连接终止"));
                    })
                    .onFailure(throwable -> {
                        System.out.println(LogUtils.styleString("", 31, 1, "连接终止失败"));
                        throwable.printStackTrace();
                    });
        } catch (Throwable e) {
            e.printStackTrace();
        }
        super.hook(taskRequest);
    }

    public Future<?> rollback(TaskRequest data) {
        /*log("回滚");
        log(MAP1.get(data));*/
        return
                ((Map.Entry<SqlConnection, Transaction>) data.cache(this)).getValue().rollback().compose(voidAsyncResult ->
                        newTransaction(data)
                );
    }

    protected Future<Transaction> newTransaction(TaskRequest data) {
        return getDatabase(data).compose(connection ->
                connection.begin().onSuccess(transaction -> {
                    /*MAP.put(data, Map.entry(connection, transaction));*/
                    data.cache(this, Map.entry(connection, transaction));
                }));
    }
}
