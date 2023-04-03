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

    void setInstance(String config);

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
}
