package org.welisdoom.task.xml.connect;

import io.vertx.pgclient.PgConnectOptions;
import io.vertx.pgclient.PgConnection;
import io.vertx.pgclient.PgPool;
import io.vertx.sqlclient.PoolOptions;
import io.vertx.sqlclient.Tuple;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.welisdoom.task.xml.dao.ConfigDao;
import org.welisdoom.task.xml.entity.Task;
import org.welisdoon.common.data.BaseCondition;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @Classname OracleConnect
 * @Description TODO
 * @Author Septem
 * @Date 13:52
 */
@Component
@Db("postgresql")
public class PostgreSQLConnectPool implements DataBaseConnectPool<PgPool, PgConnection> {
    volatile Map<String, PgPool> pools;

    ConfigDao configDao;

    @Autowired
    public void setConfigDao(ConfigDao configDao) {
        this.configDao = configDao;
    }

    @Override
    public synchronized PgPool getPool(String name) {
        if (!getPools().containsKey(name)) {
            setInstance(this.configDao.getDatabase(name));
        }
        return getPools().get(name);
    }

    @Override
    public Map<String, PgPool> getPools() {
        if (pools == null) {
            synchronized (this) {
                if (pools == null) {
                    pools = new HashMap<>();
                }
            }
        }
        return pools;
    }

    @Override
    public void setInstance(DatabaseLinkInfo config) {
        PgConnectOptions connectOptions = getSqlConnectOptions(new PgConnectOptions(), config);

// Pool options
        PoolOptions poolOptions = getPoolOptions();

// Create the client pool
        if (getPools().containsKey(config.getName())) return;
        getPools().put(config.getName(), PgPool.pool(Task.getVertx(), connectOptions, poolOptions));
    }

    /*public Future<PgConnection> getConnect(String name) {
        return (Future) getPool(name).getConnection();
    }*/
    static String pageSql = " limit ? offset ?";

    @Override
    public String toPageSql(String body) {
        return body + pageSql;
    }

    public void setPage(Tuple tuple, BaseCondition.Page page) {
        tuple.addValue(page.getPageSize());
        tuple.addValue(page.getStart() - 1);
    }

    @Override
    public String sqlFormat(String sql, List<Object> param) {
        int i = 0;
        for (; i < param.size(); i++) {
            sql = sql.replaceFirst(PATTERN_STRING, "\\$" + (i + 1));
        }
        if (sql.lastIndexOf(pageSql) >= 0) {
            sql = sql.replaceFirst("\\?", "\\$" + (i + 1)).replaceFirst("\\?", "\\$" + (i + 2));
        }
        return sql;
    }


    @Override
    public void removeInstance(String name) {
        getPools().remove(name);
    }
}
