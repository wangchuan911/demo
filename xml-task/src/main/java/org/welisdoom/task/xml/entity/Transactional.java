package org.welisdoom.task.xml.entity;


import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.sqlclient.SqlConnection;
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
@Tag(value = "transactional", parentTagTypes = Executable.class)
public class Transactional extends Unit implements Executable {
    static Map<TaskRequest, Map<String, SqlConnection>> MAP = new HashMap<>();

    /*@Override
    protected void execute(TaskRequest data) throws Throwable {
        Map<String, SqlConnection>
                map = ObjectUtils.getMapValueOrNewSafe(MAP, data, () -> new HashMap<>());
        if (!data.isDebugger && !map.containsKey(attributes.get("name"))) {
            ((Future<SqlConnection>) Transactional
                    .getDataBaseConnectPool(attributes.get("db"))
                    .getConnect(attributes.get("name")))
                    .onSuccess(sqlConnection -> {
                        map.put(attributes.get("name"), sqlConnection);
                        try {
                            super.execute(data);
                        } catch (Throwable throwable) {
                            throwable.printStackTrace();
                        }
                    }).onFailure(data.promise::fail);
        } else {
            super.execute(data);
        }
    }*/

    @Override
    protected void start(TaskRequest data, Promise<Object> toNext) {

        try {
            Map<String, SqlConnection> map = ObjectUtils.getMapValueOrNewSafe(MAP, data, () -> new HashMap<>());
            if (!data.isDebugger && !map.containsKey(attributes.get("name"))) {
                ((Future<SqlConnection>) Transactional
                        .getDataBaseConnectPool(attributes.get("db"))
                        .getConnect(attributes.get("name")))
                        .onSuccess(sqlConnection -> {
                            map.put(attributes.get("name"), sqlConnection);
                            super.start(data, toNext);
                        }).onFailure(toNext::fail);
            } else {
                super.start(data, toNext);
            }
        } catch (Throwable throwable) {
            toNext.fail(throwable);
        }
    }

    public static SqlConnection getSqlConnection(TaskRequest request, String name) {
        return MAP.get(request).get(name);
    }

    public static DataBaseConnectPool getDataBaseConnectPool(String name) {
        return (DataBaseConnectPool) ApplicationContextProvider
                .getApplicationContext()
                .getBeansWithAnnotation(Db.class)
                .entrySet().stream()
                .map(Map.Entry::getValue)
                .filter(dataBaseConnectPool -> dataBaseConnectPool instanceof DataBaseConnectPool &&
                        ApplicationContextProvider.getRealClass(dataBaseConnectPool.getClass()).getAnnotation(Db.class).value().equals(name))
                .findFirst().orElse(null);
    }

    public static DataBaseConnectPool getDataBaseConnectPool(SqlConnection connection) {
        return (DataBaseConnectPool) ApplicationContextProvider
                .getApplicationContext()
                .getBeansWithAnnotation(Db.class)
                .entrySet().stream()
                .map(Map.Entry::getValue)
                .filter(dataBaseConnectPool -> dataBaseConnectPool instanceof DataBaseConnectPool &&
                        ObjectUtils.getGenericTypes(dataBaseConnectPool.getClass(), DataBaseConnectPool.class, 1) == connection.getClass())
                .findFirst().orElse(null);
    }
}
