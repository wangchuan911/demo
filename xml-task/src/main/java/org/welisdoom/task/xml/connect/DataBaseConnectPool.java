package org.welisdoom.task.xml.connect;

import com.alibaba.fastjson.util.TypeUtils;
import io.vertx.core.Future;
import io.vertx.sqlclient.*;
import ognl.Ognl;
import ognl.OgnlException;
import org.apache.ibatis.type.JdbcType;
import org.welisdoom.task.xml.entity.TaskRequest;
import org.welisdoon.common.data.BaseCondition;

import java.sql.JDBCType;
import java.sql.SQLType;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @Classname ConnectPool
 * @Description TODO
 * @Author Septem
 * @Date 13:53
 */
public interface DataBaseConnectPool<P extends Pool, S extends SqlConnection> {
    String PATTERN_STRING = "\\#\\{(.+?)\\,jdbcType\\=(\\w+)\\}";
    Pattern PATTERN = Pattern.compile(PATTERN_STRING);


    P getPool(String name);

    void setInstance(DatabaseLinkInfo config);

    Future<S> getConnect(String name);

    Future<RowSet<Row>> page(SqlConnection connection, String sql, BaseCondition<Long, TaskRequest> data);

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

    default String setValueToSql(Tuple tuple, String sql, BaseCondition<Long, TaskRequest> data) {
        JdbcType jdbcType;
        Object value;
        for (Map.Entry<String, JdbcType> sqlParamType : getSqlParamTypes(sql)) {
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
            tuple.addValue(value);
        }
        return sql.replaceAll(PATTERN_STRING, "?");
    }

    void removeInstance(String name);

    class DatabaseLinkInfo {
        String name;
        int port;
        String host;
        String database;
        String user;
        String pw;

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
    }
}
