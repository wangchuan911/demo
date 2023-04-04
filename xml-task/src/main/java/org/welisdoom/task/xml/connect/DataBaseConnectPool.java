package org.welisdoom.task.xml.connect;

import io.vertx.core.Future;
import io.vertx.sqlclient.Pool;
import io.vertx.sqlclient.Row;
import io.vertx.sqlclient.RowSet;
import io.vertx.sqlclient.SqlConnection;
import org.welisdoom.task.xml.entity.TaskRequest;
import org.welisdoon.common.data.BaseCondition;

import java.sql.JDBCType;
import java.sql.SQLType;
import java.util.HashMap;
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
    Pattern PATTERN = Pattern.compile("\\$\\{([\\w\\.\\_\\@\\-\\&]+)\\,jdbcType\\=(\\w+)\\}");

    P getPool(String name);

    void setInstance(DatabaseLinkInfo config);

    Future<S> getConnect(String name);

    Future<RowSet<Row>> page(SqlConnection connection, String sql, BaseCondition<Long, TaskRequest> data);

    default Map<String, SQLType> getSqlParamTypes(String s) {
        Map<String, SQLType> map = new HashMap<>();
        Pattern.compile("\\$\\{([\\w\\.\\_\\@\\-\\&]+)(?:\\,jdbcType\\=)\\}");
        Matcher matcher = DataBaseConnectPool.PATTERN.matcher(s);
        SQLType sqlType = null;
        String name;
        while (matcher.find()) {
            switch (matcher.groupCount()) {
                case 2:
                    sqlType = JDBCType.valueOf(matcher.group(2));
                case 1:
                    name = matcher.group(1);
                    break;
                default:
                    continue;
            }
            map.put(name, sqlType == null ? JDBCType.VARCHAR : sqlType);
        }
        return map;
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
