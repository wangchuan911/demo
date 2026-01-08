package org.welisdoom.task.xml.entity;

import com.alibaba.fastjson.util.TypeUtils;
import io.vertx.core.Future;
import io.vertx.sqlclient.SqlConnection;
import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.type.JdbcType;
import org.ehcache.impl.internal.concurrent.ConcurrentHashMap;
import org.welisdoom.task.xml.annotations.Attr;
import org.welisdoom.task.xml.annotations.Tag;
import org.welisdoom.task.xml.connect.DataBaseConnectPool;
import org.welisdoom.task.xml.connect.Db;
import org.welisdoom.task.xml.connect.sync.DatasouceConnectManager;
import org.welisdoom.task.xml.dao.ConfigDao;
import org.welisdoom.task.xml.handler.OgnlUtils;
import org.welisdoom.task.xml.intf.ApplicationContextProvider;
import org.welisdoon.common.MyBatisUtils;
import org.welisdoon.common.ObjectUtils;

import javax.sql.DataSource;
import java.io.ByteArrayInputStream;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;

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
    static volatile Map<String, DataBaseConnectPool> MAP = new HashMap<>();
    static volatile Map<String, DataSource> MAP_SYNC = new HashMap<>();

    @Override
    protected void startSync(TaskSession data) throws Throwable {

        ObjectUtils.getMapValueOrNewSafe(MAP_SYNC, attributes.get("id"), () -> Database.getDataSource(attributes.get("id")));
        super.startSync(data);

    }

    @Deprecated
    public static DataBaseConnectPool getDatabase(String name) {
        return MAP.get(name);
    }

    @Deprecated
    public static DataSource getDatabaseSync(String name) {
        return MAP_SYNC.get(name);
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
    protected Future<Object> start(TaskSession data, Object preUnitResult) {
        try {
            ObjectUtils.getMapValueOrNewSafe(MAP, attributes.get("id"), () -> Database
                    .getDataBaseConnectPool(attributes.get("id")));
            return super.start(data, preUnitResult);

        } catch (Throwable throwable) {
            return Future.failedFuture(throwable);
        }
    }


    @Deprecated
    public static synchronized DataBaseConnectPool getDataBaseConnectPool(String name) {
        DatasouceConnectManager.DatasourceInfo linkInfo = ApplicationContextProvider.getApplicationContext().getBean(ConfigDao.class).getDatabase(name);
        DataBaseConnectPool pool = ApplicationContextProvider
                .getApplicationContext()
                .getBeansWithAnnotation(Db.class)
                .entrySet().stream().filter(entry -> entry.getValue() instanceof DataBaseConnectPool)
                .map(stringObjectEntry -> (DataBaseConnectPool) stringObjectEntry.getValue())
                .filter(dataBaseConnectPool -> dataBaseConnectPool instanceof DataBaseConnectPool &&
                        ApplicationContextProvider.getRealClass(dataBaseConnectPool.getClass()).getAnnotation(Db.class).value().equals(linkInfo.getModel()))
                .findFirst().orElse(null);
        if (pool != null)
            pool.setInstance(new DataBaseConnectPool.DatabaseLinkInfo(linkInfo));
        return pool;
    }

    public static synchronized DataSource getDataSource(String name) {
        DatasouceConnectManager.DatasourceInfo linkInfo = ApplicationContextProvider.getApplicationContext().getBean(ConfigDao.class).getDatabase(name);
        DatasouceConnectManager pool = getDatasouceConnectPool(name);
        return pool.getDataSource(linkInfo.getName());
    }

    public static synchronized DatasouceConnectManager getDatasouceConnectPool(String name) {
        DatasouceConnectManager.DatasourceInfo linkInfo = ApplicationContextProvider.getApplicationContext().getBean(ConfigDao.class).getDatabase(name);
        DatasouceConnectManager pool = ApplicationContextProvider
                .getApplicationContext()
                .getBeansWithAnnotation(Db.class)
                .entrySet().stream().filter(entry -> entry.getValue() instanceof DatasouceConnectManager)
                .map(stringObjectEntry -> (DatasouceConnectManager) stringObjectEntry.getValue())
                .filter(dataBaseConnectPool -> ApplicationContextProvider.getRealClass(dataBaseConnectPool.getClass()).getAnnotation(Db.class).value().equals(linkInfo.getModel()))
                .findFirst().orElse(null);
        if (pool != null)
            pool.setInstance(linkInfo);
        return pool;
    }

    protected static <T> Future<T> doConnect(Unit unit, TaskSession data, Function<SqlConnection, Future<T>> function) {
        return findConnect(unit, data)
                .compose(function)
                .transform(result -> {
                    return Database.releaseConnect(unit, data)
                            .transform(o -> result.succeeded() ?
                                    Future.succeededFuture(result.result()) :
                                    Future.failedFuture(result.cause()));
                });
    }

    protected static Future<SqlConnection> findConnect(Unit unit, TaskSession data) {
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
                    if (!sqlConnectionCounter.containsKey(connection)) {
                        synchronized (sqlConnectionCounter) {
                            if (!sqlConnectionCounter.containsKey(connection)) {
                                sqlConnectionCounter.put((SqlConnection) connection, new AtomicInteger(0));
                                data.cache(unit, connection);
                            }
                        }
                    }
                    sqlConnectionCounter.get(connection).incrementAndGet();
                });
            }
            return Future.succeededFuture(data.cache(unit));
        }
    }

    static volatile Map<SqlConnection, AtomicInteger> sqlConnectionCounter = new ConcurrentHashMap<>();

    protected static Optional<Transactional> getTransactional(Unit unit, TaskSession data) {
        String link = unit.attributes.get("link");
        Optional<Transactional> optional;
        if (StringUtils.isEmpty(link)) {
            optional = Optional.ofNullable(unit.getParent(Transactional.class));
        } else {
            optional = unit.getParents(Transactional.class).stream().filter(transactional -> transactional.attributes.get("link").equals(link)).findFirst();
        }
        return optional;
    }

    @Deprecated
    protected static synchronized Future<Object> releaseConnect(Unit unit, TaskSession data) {
        Optional<Transactional> optional = getTransactional(unit, data);
        if (optional.isPresent()) {
            return Future.succeededFuture();
        } else if (data.cache(unit) == null) {
            return Future.failedFuture("not connect");
        } else {
            SqlConnection connection = data.cache(unit);
            AtomicInteger count = sqlConnectionCounter.get(connection);
            if (count != null) {
                if (count.decrementAndGet() <= 0) {
                    sqlConnectionCounter.remove(connection);
                    data.clearCache(unit);
                } else
                    return Future.succeededFuture();
            }
            return (Future) connection.close();
        }
    }

    @Deprecated
    protected static DataBaseConnectPool getDataBase(Unit unit) {
        Unit p = unit.parent;
        while (!(p == null || p instanceof Transactional)) {
            p = p.parent;
        }
        if (p != null)
            return Database.getDatabase(p.attributes.get("link"));
        return Database.getDatabase(unit.attributes.get("link"));
    }

    protected static DataSouceConnect getDataBaseSync(Unit unit) throws SQLException {
        DataSouceConnect p = unit.getParents(Transactional.class).stream().filter(transactional -> Objects.equals(unit.attributes.get("link"), transactional.attributes.get("link"))).findFirst().map(transactional -> {
            return new DataSouceConnect(unit.attributes.get("link"), transactional.connection, true);
        }).orElse(null);
        if (p == null)
            p = new DataSouceConnect(unit.attributes.get("link"), Database.getDatabaseSync(unit.attributes.get("link")).getConnection(), false);
        return p;
    }

    public static class DataSouceConnect {
        final String name;
        final Connection connection;
        final boolean transaction;
        PreparedStatement preparedStatement;
        DatasouceConnectManager.SqlTemplate sqlTemplate;

        public DataSouceConnect(String name, Connection connection, boolean transaction) {
            this.name = name;
            this.connection = connection;
            this.transaction = transaction;
        }

        public PreparedStatement prepare(final String sql, TaskSession data) throws SQLException {
            if (preparedStatement == null || sqlTemplate == null || !Objects.equals(sql, sqlTemplate.sql)) {
                this.sqlTemplate = new DatasouceConnectManager.SqlTemplate(sql, new LinkedList<>());
                MyBatisUtils.readSqlTemplate(sql, (s1, jdbcType) -> {
                    JdbcType sqlType = Optional.ofNullable(JdbcType.valueOf(jdbcType)).orElse(JdbcType.VARCHAR);
                    this.sqlTemplate.getTypes().add(Map.entry(s1, sqlType));
                });
                String sql2 = sql.replaceAll(MyBatisUtils.PATTERN_STRING, "?");
                preparedStatement = connection.prepareStatement(sql2);
                this.sqlTemplate.log1.setLength(0);
                this.sqlTemplate.log1.append("sql   :").append(sql2);
            }
            this.sqlTemplate.log2.setLength(0);
            this.sqlTemplate.log2.append("params:");
            for (int i = 0; i < this.sqlTemplate.getTypes().size(); i++) {
                Map.Entry<String, JdbcType> entry = this.sqlTemplate.getTypes().get(i);
                Object value = OgnlUtils.getValue(entry.getKey(), data.getOgnlContext(), data.getBus(), Object.class);
                switch (entry.getValue()) {
                    case BLOB:
                        preparedStatement.setBlob(i, new ByteArrayInputStream((byte[]) (value = TypeUtils.castToBytes(value))));
                        break;
                    case INTEGER:
                    case SMALLINT:
                    case TINYINT:
                        preparedStatement.setInt(i, (Integer) (value = TypeUtils.castToInt(value)));
                        break;
                    case NUMERIC:
                    case BIGINT:
                        preparedStatement.setBigDecimal(i, (BigDecimal) (value = TypeUtils.castToBigDecimal(value)));
                        break;
                    case BIT:
                        preparedStatement.setByte(i, (Byte) (value = TypeUtils.castToByte(value)));
                        break;
                    case BOOLEAN:
                        preparedStatement.setBoolean(i, (Boolean) (value = TypeUtils.castToBoolean(value)));
                        break;
                    case DOUBLE:
                        preparedStatement.setDouble(i, (Double) (value = TypeUtils.castToDouble(value)));
                        break;
                    case FLOAT:
                        preparedStatement.setFloat(i, (Float) (value = TypeUtils.castToFloat(value)));
                        break;
                    case TIMESTAMP:
                        preparedStatement.setTimestamp(i, ((Timestamp) (value = TypeUtils.castToTimestamp(value))));
                        break;
                    case DATE:
                        preparedStatement.setDate(i, (Date) (value = new Date(TypeUtils.castToDate(value).getTime())));
                        break;
                    case CLOB:
                        preparedStatement.setClob(i, new InputStreamReader(new ByteArrayInputStream(((String) (value = TypeUtils.castToString(value))).getBytes(StandardCharsets.UTF_8))));
                        break;
                    case NCLOB:
                        preparedStatement.setNClob(i, new InputStreamReader(new ByteArrayInputStream(((String) (value = TypeUtils.castToString(value))).getBytes(StandardCharsets.UTF_8))));
                        break;
                    case NCHAR:
                        preparedStatement.setNString(i, (String) (value = TypeUtils.castToString(value)));
                        break;
                    case VARCHAR:
                        preparedStatement.setString(i, (String) (value = TypeUtils.castToString(value)));
                        break;
                    default:
                        throw new IllegalArgumentException("不支持的jdbc" + entry.getValue());
                }
                this.sqlTemplate.log2.append(value);
                if (value != null) {
                    this.sqlTemplate.log2.append("/*").append(value.getClass().getSimpleName()).append("*/");
                }
            }
            return preparedStatement;
        }

        public void commit() throws SQLException {
            connection.commit();
        }

        public void close() throws SQLException {
            if (!transaction)
                connection.close();
        }

        public DatasouceConnectManager getDatasouceConnectPool() {
            return Database.getDatasouceConnectPool(this.name);
        }
    }
}
