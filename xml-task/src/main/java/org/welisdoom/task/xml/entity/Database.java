package org.welisdoom.task.xml.entity;

import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.sqlclient.SqlConnection;
import org.apache.commons.lang3.StringUtils;
import org.ehcache.impl.internal.concurrent.ConcurrentHashMap;
import org.welisdoom.task.xml.annotations.Attr;
import org.welisdoom.task.xml.annotations.Tag;
import org.welisdoom.task.xml.connect.DataBaseConnectPool;
import org.welisdoom.task.xml.connect.Db;
import org.welisdoom.task.xml.dao.ConfigDao;
import org.welisdoom.task.xml.intf.Copyable;
import org.welisdoom.task.xml.intf.type.Executable;
import org.welisdoom.task.xml.intf.type.Stream;
import org.welisdoon.common.ObjectUtils;
import org.welisdoon.web.common.ApplicationContextProvider;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

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
    static Map<String, DataBaseConnectPool> MAP = new HashMap<>();

    public static DataBaseConnectPool getDatabase(String name) {
        return MAP.get(name);
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
    protected void start(TaskRequest data, Object preUnitResult, Promise<Object> toNext) {
        try {
            ObjectUtils.getMapValueOrNewSafe(MAP, attributes.get("id"), () -> Database
                    .getDataBaseConnectPool(attributes.get("id")));
            super.start(data, preUnitResult, toNext);

        } catch (Throwable throwable) {
            toNext.fail(throwable);
        }
    }


    public static DataBaseConnectPool getDataBaseConnectPool(String name) {
        DataBaseConnectPool.DatabaseLinkInfo linkInfo = ApplicationContextProvider.getApplicationContext().getBean(ConfigDao.class).getDatabase(name);
        DataBaseConnectPool pool = ApplicationContextProvider
                .getApplicationContext()
                .getBeansWithAnnotation(Db.class)
                .entrySet().stream()
                .map(stringObjectEntry -> (DataBaseConnectPool) stringObjectEntry.getValue())
                .filter(dataBaseConnectPool -> dataBaseConnectPool instanceof DataBaseConnectPool &&
                        ApplicationContextProvider.getRealClass(dataBaseConnectPool.getClass()).getAnnotation(Db.class).value().equals(linkInfo.getModel()))
                .findFirst().orElse(null);
        return pool;
    }


    @Override
    public void destroy(TaskRequest taskRequest) {
        super.destroy(taskRequest);
    }

    protected static Future<SqlConnection> findConnect(Unit unit, TaskRequest data) {
        /*String link = unit.attributes.get("link");
        Optional<Transactional> optional;
        if (StringUtils.isEmpty(link)) {
            optional = Optional.ofNullable(unit.getParent(Transactional.class));
        } else {
            optional = unit.getParents(Transactional.class).stream().filter(transactional -> transactional.attributes.get("link").equals(link)).findFirst();
        }
        if (optional.isPresent())
            return optional.get().getSqlConnection(data);
        try {
            return data.cache(unit, () -> getDataBase(unit, data).getConnect(unit.attributes.get("link"), data));
        } catch (Throwable throwable) {
            return Future.failedFuture(throwable);
        }*/
        Optional<Transactional> optional = getTransactional(unit, data);
        if (optional.isPresent()) {
            return optional.get().getSqlConnection(data);
        } else {
            if (data.cache(unit) == null) {
                return getDataBase(unit).getConnect(unit.attributes.get("link"), data).onSuccess(connection -> {
                    AtomicInteger atomicInteger = sqlConnectionCounter.get(connection);
                    if (atomicInteger == null) {
                        synchronized (sqlConnectionCounter) {
                            if (atomicInteger == null) {
                                sqlConnectionCounter.put((SqlConnection) connection, atomicInteger = new AtomicInteger(0));
                                data.cache(unit, connection);
                            }
                        }
                    }
                    atomicInteger.incrementAndGet();
                });
            }
            return Future.succeededFuture(data.cache(unit));
        }
    }

    static Map<SqlConnection, AtomicInteger> sqlConnectionCounter = new ConcurrentHashMap<>();

    protected static Optional<Transactional> getTransactional(Unit unit, TaskRequest data) {
        String link = unit.attributes.get("link");
        Optional<Transactional> optional;
        if (StringUtils.isEmpty(link)) {
            optional = Optional.ofNullable(unit.getParent(Transactional.class));
        } else {
            optional = unit.getParents(Transactional.class).stream().filter(transactional -> transactional.attributes.get("link").equals(link)).findFirst();
        }
        return optional;
    }

    protected static Future<Object> releaseConnect(Unit unit, TaskRequest data) {
        Optional<Transactional> optional = getTransactional(unit, data);
        if (optional.isPresent()) {
            return Future.succeededFuture();
        } else if (data.cache(unit) == null) {
            return Future.failedFuture("not connect");
        } else {
            SqlConnection connection = data.clearCache(unit);
            AtomicInteger count = sqlConnectionCounter.get(connection);
            if (count != null) {
                if (count.decrementAndGet() <= 0)
                    sqlConnectionCounter.remove(connection);
                else
                    return Future.succeededFuture();
            }
            return (Future) connection.close();
        }
    }

    protected static DataBaseConnectPool getDataBase(Unit unit) {
        Unit p = unit.parent;
        while (!(p == null || p instanceof Transactional)) {
            p = p.parent;
        }
        if (p != null)
            return Database.getDatabase(p.attributes.get("link"));
        return Database.getDatabase(unit.attributes.get("link"));
    }
}
