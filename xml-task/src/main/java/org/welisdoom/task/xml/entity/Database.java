package org.welisdoom.task.xml.entity;

import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.sqlclient.SqlConnection;
import org.welisdoom.task.xml.annotations.Attr;
import org.welisdoom.task.xml.annotations.Tag;
import org.welisdoom.task.xml.connect.DataBaseConnectPool;
import org.welisdoom.task.xml.connect.Db;
import org.welisdoom.task.xml.intf.Copyable;
import org.welisdoom.task.xml.intf.type.Executable;
import org.welisdoom.task.xml.intf.type.Stream;
import org.welisdoon.common.ObjectUtils;
import org.welisdoon.web.common.ApplicationContextProvider;

import java.util.HashMap;
import java.util.Map;

/**
 * @Classname Database
 * @Description TODO
 * @Author Septem
 * @Date 16:48
 */
@Tag(value = "database", parentTagTypes = Initialization.class, desc = "数据库连接")
@Attr(name = "id", desc = "数据库标识")
@Attr(name = "db", desc = "数据库类型")
public class Database extends Unit {
    static Map<TaskRequest, Map<String, DataBaseConnectPool>> MAP = new HashMap<>();

    public static DataBaseConnectPool getDatabase(TaskRequest request, String name) {
        return MAP.get(request).get(name);
    }
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
            ObjectUtils.getMapValueOrNewSafe(ObjectUtils.getMapValueOrNewSafe(MAP, data, () -> new HashMap<>()), attributes.get("id"), () -> Database
                    .getDataBaseConnectPool(attributes.get("model")));
            super.start(data, toNext);

        } catch (Throwable throwable) {
            toNext.fail(throwable);
        }
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
                        ObjectUtils.getGenericTypes(dataBaseConnectPool.getClass(), DataBaseConnectPool.class, 1).isAssignableFrom(connection.getClass()))
                .findFirst().orElse(null);
    }

    @Override
    public void destroy(TaskRequest taskRequest) {
        super.destroy(taskRequest);
        MAP.remove(taskRequest);
    }

    protected static Future<SqlConnection> findConnect(Unit unit, TaskRequest data) {
        Unit p = unit.parent;
        while (!(p == null || p instanceof Transactional)) {
            p = p.parent;
        }
        if (p != null)
            return Future.succeededFuture(((Transactional) p).getSqlConnection(data));
        return getDataBase(unit, data).getConnect(unit.attributes.get("link"));
    }

    protected static DataBaseConnectPool getDataBase(Unit unit, TaskRequest data) {
        Unit p = unit.parent;
        while (!(p == null || p instanceof Transactional)) {
            p = p.parent;
        }
        if (p != null)
            return Database.getDatabase(data, p.attributes.get("link"));
        return Database.getDatabase(data, unit.attributes.get("link"));
    }
}
