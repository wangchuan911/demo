package org.welisdoom.task.xml.connect;

import com.alibaba.fastjson.util.TypeUtils;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.sqlclient.*;
import ognl.Ognl;
import ognl.OgnlException;
import org.apache.ibatis.type.JdbcType;
import org.welisdoom.task.xml.entity.TaskRequest;
import org.welisdoon.common.ObjectUtils;
import org.welisdoon.common.data.BaseCondition;

import java.util.*;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @Classname ConnectPool
 * @Description TODO
 * @Author Septem
 * @Date 13:53
 */
public interface DataBaseConnectPool<P extends Pool, S extends SqlConnection> extends ConnectPool {
    String PATTERN_STRING = "\\#\\{(.+?)\\,jdbcType\\=(\\w+)\\}";
    Pattern PATTERN = Pattern.compile(PATTERN_STRING);

    Map<IToken, Map<String, SqlConnection>> connectings = new HashMap<>();
    Map<String, Future<SqlConnection>> locks = new HashMap<>();

    P getPool(String name);

    void setInstance(DatabaseLinkInfo config);

    default Future<S> getConnect(String name, IToken token) {
        try {
            Map<String, SqlConnection> map = ObjectUtils.getMapValueOrNewSafe(connectings, token, () -> new HashMap<>());
            if (map.containsKey(name)) {
                return (Future) Future.succeededFuture(map.get(name));
            }
            Future<SqlConnection> lock = ObjectUtils.getMapValueOrNewSafe(locks, name, () -> getPool(name).getConnection()
                    .onSuccess(event -> {
                        map.put(name, event);
                    }).onComplete(event -> {
                        locks.remove(name);
                    }));
            return (Future) lock;
        } catch (Throwable e) {
            return Future.failedFuture(e);
        }
    }

    default Future<RowSet<Row>> page(SqlConnection connection, String sql, BaseCondition<String, TaskRequest> data) {
        List<Object> params = new LinkedList<>();
        setValueToSql(params, getSqlParamTypes(sql = toPageSql(sql)), data);
        sql = sqlFormat(sql, params);
        Tuple tuple = Tuple.tuple(params);
        setPage(tuple, data.getPage());
        log("sql", sql);
        log("params", tuple);
        return connection.preparedQuery(sql).execute(tuple);
    }

    default Future<Object> pageScroll(SqlConnection connection, String sql, BaseCondition<String, TaskRequest> data, Function<RowSet<Row>, Future<Object>> future) {
        List<Object> params = new LinkedList<>();
        setValueToSql(params, getSqlParamTypes(sql = toPageSql(sql)), data);
        sql = sqlFormat(sql, params);
        return pageScroll(connection, sql, params, data.getPage(), future);
    }

    default Future<Object> pageScroll(SqlConnection connection, String sql, List<Object> list, BaseCondition.Page page, Function<RowSet<Row>, Future<Object>> future) {
        Tuple tuple = Tuple.tuple(list);
        setPage(tuple, page);
        log("sql", sql);
        log("params", tuple);
        return connection
                .preparedQuery(sql)
                .execute(tuple).compose(rows ->
                        future
                                .apply(rows)
                                .compose(o -> rows.size() < page.getPageSize()
                                        ? Future.succeededFuture()
                                        : pageScroll(connection, sql, list, page.setPage(page.getPage() + 1), future)
                                ));
    }

    default Future<Integer> update(SqlConnection connection, String sql, BaseCondition<String, TaskRequest> data) {
        List<Object> params = new LinkedList<>();
        setValueToSql(params, getSqlParamTypes(sql), data);
        sql = sqlFormat(sql, params);
        Tuple tuple = Tuple.tuple(params);
        log("sql", sql);
        log("params", tuple);
        return connection.preparedQuery(sql).execute(tuple).compose(rows -> Future.succeededFuture(rows.rowCount()));
    }

    default List<Map.Entry<String, JdbcType>> getSqlParamTypes(String s) {
        List<Map.Entry<String, JdbcType>> list = new LinkedList<>();
        Matcher matcher = DataBaseConnectPool.PATTERN.matcher(s);
        JdbcType sqlType = null;
        String name;
        while (matcher.find()) {
            switch (matcher.groupCount()) {
                case 2:
                    sqlType = JdbcType.valueOf(matcher.group(2));
                case 1:
                    name = matcher.group(1);
                    break;
                default:
                    continue;
            }
            list.add(Map.entry(name, sqlType == null ? JdbcType.VARCHAR : sqlType));
        }
        return list;
    }

    String toPageSql(String body);

    void setPage(Tuple tuple, BaseCondition.Page page);

    String sqlFormat(String sql, List<Object> param);

    default void setValueToSql(List<Object> params, List<Map.Entry<String, JdbcType>> jdbcTypes, BaseCondition<String, TaskRequest> data) {
        JdbcType jdbcType;
        Object value;
        for (Map.Entry<String, JdbcType> sqlParamType : jdbcTypes) {
            jdbcType = sqlParamType.getValue();
            try {
                value = Ognl.getValue(sqlParamType.getKey(), data.getData().getOgnlContext(), data.getData().getBus(), Object.class);
            } catch (OgnlException e) {
                throw new RuntimeException(e.getMessage(), e);
            }
            if (value != null)
                switch (jdbcType) {
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


                }
            params.add(value);
        }
    }

    /*default void setValueToSql(List<Object> params, String sql, BaseCondition<String, TaskRequest> data) {
        setValueToSql(params, getSqlParamTypes(sql), data);
    }*/

    void removeInstance(String name);

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

    class DatabaseLinkInfo {
        String name;
        int port;
        String host;
        String database;
        String user;
        String pw;
        String model;

        public int getPort() {
            return port;
        }

        public void setPort(int port) {
            this.port = port;
        }

        public String getHost() {
            return host;
        }

        public void setHost(String host) {
            this.host = host;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getDatabase() {
            return database;
        }

        public void setDatabase(String database) {
            this.database = database;
        }

        public String getUser() {
            return user;
        }

        public void setUser(String user) {
            this.user = user;
        }

        public String getPw() {
            return pw;
        }

        public void setPw(String pw) {
            this.pw = pw;
        }

        public String getModel() {
            return model;
        }

        public void setModel(String model) {
            this.model = model;
        }
    }

    class StaticSql {
        String sql;
        List<Map.Entry<String, JdbcType>> types;

        public StaticSql(String sql, List<Map.Entry<String, JdbcType>> types) {
            this.sql = sql;
            this.types = types;
        }

        public List<Map.Entry<String, JdbcType>> getTypes() {
            return types;
        }

        public String getSql() {
            return sql;
        }

    }


}
