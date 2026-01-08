package org.welisdoom.task.xml.connect;

import io.vertx.core.Future;
import io.vertx.core.VertxOptions;
import io.vertx.core.impl.cpu.CpuCoreSensor;
import io.vertx.sqlclient.*;
import org.apache.ibatis.type.JdbcType;
import org.welisdoom.task.xml.connect.sync.DatasouceConnectManager;
import org.welisdoom.task.xml.handler.OgnlUtils;
import org.welisdoon.common.MyBatisUtils;
import org.welisdoon.common.data.BaseCondition;

import java.sql.JDBCType;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * @Classname ConnectPool
 * @Description TODO
 * @Author Septem
 * @Date 13:53
 */
@Deprecated
public interface DataBaseConnectPool<P extends Pool, S extends SqlConnection> extends ConnectPool<S> {
//    String PATTERN_STRING = "\\#\\{(.+?)\\,jdbcType\\=(\\w+)\\}";
//    Pattern PATTERN = Pattern.compile(PATTERN_STRING);

    @Deprecated
    P getPool(String name);

    @Deprecated
    Map<String, P> getPools();

    void setInstance(DatabaseLinkInfo config);

    @Deprecated
    default Future<S> getConnect(String name, IToken token) {
        try {
            return (Future) getPool(name).getConnection();
        } catch (Throwable e) {
            return Future.failedFuture(e);
        }
    }


    @Deprecated
    default Future<RowSet<Row>> execute(SqlConnection connection, String sql, Tuple tuple) {
        return connection.preparedQuery(sql).execute(tuple);
    }

    default List<Map.Entry<String, JdbcType>> getSqlParamTypes(String s) {
        List<Map.Entry<String, JdbcType>> list = new LinkedList<>();
        MyBatisUtils.readSqlTemplate(s, (s1, jdbcType) -> {
            JdbcType sqlType = JdbcType.valueOf(jdbcType);
            if (sqlType == null) sqlType = JdbcType.VARCHAR;
            list.add(Map.entry(s1, sqlType));
        });
        return list;
    }


    default JDBCType toJDBCType(JdbcType jdbcType) {
        for (JDBCType value : JDBCType.values()) {
            if (value.getVendorTypeNumber() == jdbcType.TYPE_CODE) {
                return value;
            }
        }
        return null;
    }

    String toPageSql(String body);

    void setPage(Tuple tuple, BaseCondition.Page page);

    String sqlFormat(String sql, List<Object> param);

    default void setValueToSql(List<Object> params, List<Map.Entry<String, JdbcType>> jdbcTypes, Map context, Map data) {
        JdbcType jdbcType;
        Object value;
        for (Map.Entry<String, JdbcType> sqlParamType : jdbcTypes) {
            jdbcType = sqlParamType.getValue();
            value = OgnlUtils.getValue(sqlParamType.getKey(), context, data, Object.class);
            if (value == null) {
                continue;
            }
            /*switch (jdbcType) {
                case INTEGER:
                case SMALLINT:
                case TINYINT:
                    value = TypeUtils.castToInt(value);
                    break;
                case NUMERIC:
                case BIGINT:
                    value = TypeUtils.castToBigDecimal(value);
                    break;
                case BLOB:
                    value = TypeUtils.castToBytes(value);
                    break;
                case BIT:
                    value = TypeUtils.castToByte(value);
                    break;
                case BOOLEAN:
                    value = TypeUtils.castToBoolean(value);
                    break;
                case DOUBLE:
                    value = TypeUtils.castToDouble(value);
                    break;
                case FLOAT:
                    value = TypeUtils.castToFloat(value);
                    break;
                case TIMESTAMP:
                    value = TypeUtils.castToTimestamp(value);
                    if (Objects.nonNull(value) && value instanceof Timestamp) {
                        value = ((Timestamp) value).toLocalDateTime();
                    }
                    break;
                case DATE:
                    value = TypeUtils.castToDate(value);
                    break;
                case CLOB:
                case NCHAR:
                case NCLOB:
                case VARCHAR:
                default:
                    value = TypeUtils.castToString(value);
                    break;


            }*/

            value = MyBatisUtils.getValue(jdbcType.name(), value);
            params.add(value);
        }
    }

    /*default void setValueToSql(List<Object> params, String sql, BaseCondition<String, TaskRequest> data) {
        setValueToSql(params, getSqlParamTypes(sql), data);
    }*/

    void removeInstance(String name);

    @Deprecated
    default <S extends SqlConnectOptions> S getSqlConnectOptions(S connectOptions, DatabaseLinkInfo config) {
        return (S) connectOptions
                .setPort(config.getPort())
                .setHost(config.getHost())
                .setDatabase(config.getDatabase())
                .setUser(config.getUser())
                .setPassword(config.getPw())
                .setReconnectAttempts(5)
                .setReconnectInterval(1000);
    }

    @Deprecated
    default PoolOptions getPoolOptions() {
        log("cpu core count:", CpuCoreSensor.availableProcessors() + "");
        return new PoolOptions()
                .setMaxSize(VertxOptions.DEFAULT_EVENT_LOOP_POOL_SIZE)
                .setConnectionTimeout(5).setConnectionTimeoutUnit(TimeUnit.MINUTES);
    }

    default void log(String prefix, Tuple tuple) {
        StringBuilder builder = new StringBuilder();
        Object obj;
        for (int i = 0, len = tuple.size(); i < len; i++) {
            obj = tuple.getValue(i);
            builder.append(", ").append(obj).append("(").append(obj == null ? "null" : obj.getClass().getSimpleName()).append(")");
        }
        log(prefix, builder.length() == 0 ? builder.toString() : builder.substring(1));
    }

    default void log(String prefix, String content) {
        System.out.print("==>");
        System.out.print(prefix);
        System.out.print("==>");
        System.out.print("[ ");
        System.out.print(content);
        System.out.println(" ]");
    }

    @Deprecated
    class DatabaseLinkInfo extends DatasouceConnectManager.DatasourceInfo {
        public DatabaseLinkInfo(DatasouceConnectManager.DatasourceInfo datasourceInfo) {
            this.name = datasourceInfo.getName();
            this.port = datasourceInfo.getPort();
            this.host = datasourceInfo.getHost();
            this.database = datasourceInfo.getDatabase();
            this.user = datasourceInfo.getUser();
            this.pw = datasourceInfo.getPw();
            this.model = datasourceInfo.getModel();
        }
    }

    @Deprecated
    class StaticSql extends DatasouceConnectManager.SqlTemplate {

        public StaticSql(String sql, List<Map.Entry<String, JdbcType>> types) {
            super(sql, types);
        }
    }


    @Deprecated
    default Future<Void> closePools() {
        return (Future) Future.join(new HashSet<String>(getPools().keySet()).stream().map(s -> closePool(s)).collect(Collectors.toList()));
    }

    @Deprecated
    default Future<Void> closePool(String name) {
        return getPools().remove(name).close();
    }
}
