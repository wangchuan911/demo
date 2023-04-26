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
    Map<TaskRequest, SqlConnection> MAP = new HashMap<>();
    Map<TaskRequest, Transaction> MAP1 = new HashMap<>();

    static HookRollbackThread hookRollbackThread = new HookRollbackThread();

    static class HookRollbackThread extends Thread {
        public final Set<Transactional> transactions = new HashSet<>();

        @Override
        public void run() {
            for (Transactional transactional : transactions) {
                for (Map.Entry<TaskRequest, Transaction> taskRequestTransactionEntry : transactional.MAP1.entrySet()) {
                    try {
                        taskRequestTransactionEntry
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
                }
                for (Map.Entry<TaskRequest, SqlConnection> entry : transactional.MAP.entrySet()) {
                    try {
                        entry.getValue().close()
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
                }
            }

        }
    }

    ;

    public Transactional() {
        super();
        hookRollbackThread.transactions.add(this);
    }

    static {
        Runtime.getRuntime().addShutdownHook(hookRollbackThread);
    }

    protected Future<? extends SqlConnection> getDatabase(TaskRequest data) {
        return Database.getDatabase(data, attributes.get("link")).getConnect(attributes.get("link"));
    }

    @Override
    protected void start(TaskRequest data, Promise<Object> toNext) {
        this.getDatabase(data).compose(connection -> {
            MAP.put(data, connection);
            return connection.begin().compose(transaction -> {
                MAP1.put(data, transaction);
                Promise<Object> promise = Promise.promise();
                Future<Object> future = promise.future().onSuccess(o1 -> {
                    MAP1.get(data)
                            .commit()
                            .onSuccess(unused -> {
                                toNext.complete(o1);
                            }).onFailure(toNext::fail);
                }).onFailure(throwable -> {
                    MAP1.get(data)
                            .rollback()
                            .onSuccess(unused -> {
                                toNext.fail(throwable);
                            }).onFailure(toNext::fail);
                }).onComplete(objectAsyncResult -> {
                    MAP1.remove(data);
                    MAP.remove(data).close();
                });
                super.start(data, promise);
                return future;
            });
        }).onFailure(toNext::fail);
    }


    public SqlConnection getSqlConnection(TaskRequest request) {
        return MAP.get(request);
    }

    @Override
    public void destroy(TaskRequest taskRequest) {
        super.destroy(taskRequest);
        MAP.remove(taskRequest);
        MAP1.remove(taskRequest);
    }

    public Future<?> commit(TaskRequest data) {
        log("提交");
        log(MAP1.get(data));
        return MAP1.get(data).commit().compose(voidAsyncResult ->
                getDatabase(data).compose(connection -> {
                    return connection.begin().onSuccess(transaction -> {
                        MAP.put(data, connection).close();
                        MAP1.put(data, transaction);
                        log(MAP1.get(data));
                    });
                })
        );
    }

    public Future<?> rollback(TaskRequest data) {
        log("回滚");
        log(MAP1.get(data));
        return MAP1.get(data).rollback().compose(voidAsyncResult ->
                getDatabase(data).compose(connection -> {
                    return connection.begin().onSuccess(transaction -> {
                        MAP.put(data, connection).close();
                        MAP1.put(data, transaction);
                        log(MAP1.get(data));
                    });
                })
        );
    }
}
