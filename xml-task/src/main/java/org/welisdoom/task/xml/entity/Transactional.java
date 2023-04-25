package org.welisdoom.task.xml.entity;


import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.sqlclient.SqlConnection;
import io.vertx.sqlclient.Transaction;
import org.welisdoom.task.xml.annotations.Attr;
import org.welisdoom.task.xml.annotations.Tag;
import org.welisdoom.task.xml.connect.DataBaseConnectPool;
import org.welisdoom.task.xml.connect.Db;
import org.welisdoom.task.xml.intf.type.Executable;
import org.welisdoon.common.ObjectUtils;
import org.welisdoon.web.common.ApplicationContextProvider;

import java.util.HashMap;
import java.util.Map;

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
    static Map<TaskRequest, Transaction> MAP1 = new HashMap<>();

    @Override
    protected void start(TaskRequest data, Promise<Object> toNext) {

        ((Future<Object>) Database.getDatabase(data, attributes.get("link")).getConnect(attributes.get("link"))).onSuccess(o -> {
            MAP.put(data, (SqlConnection) o);
            ((SqlConnection) o).begin().onSuccess(transaction -> {
                MAP1.put(data, transaction);
                Promise<Object> promise = Promise.promise();
                promise.future().onSuccess(o1 -> {
                    transaction
                            .commit()
                            .onSuccess(unused -> {
                                toNext.complete(o1);
                            }).onFailure(toNext::fail);
                }).onFailure(throwable -> {
                    transaction
                            .rollback()
                            .onSuccess(unused -> {
                                toNext.fail(throwable);
                            }).onFailure(toNext::fail);
                });
                super.start(data, promise);
            });
        }).onFailure(toNext::fail);
    }


    public SqlConnection getSqlConnection(TaskRequest request) {
        return MAP.get(request);
    }

    @Override
    public void destroy(TaskRequest taskRequest) {
        super.destroy(taskRequest);
        MAP.remove(taskRequest).close();
        MAP1.remove(taskRequest);
    }

    public static Future<Void> commit(TaskRequest data) {
        return MAP1.get(data).commit();
    }

    public static Future<Void> rollback(TaskRequest data) {
        return MAP1.get(data).rollback();
    }
}
