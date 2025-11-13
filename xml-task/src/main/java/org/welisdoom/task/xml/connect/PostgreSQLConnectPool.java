package org.welisdoom.task.xml.connect;

import io.vertx.pgclient.PgBuilder;
import io.vertx.pgclient.PgConnectOptions;
import io.vertx.pgclient.PgConnection;
import io.vertx.sqlclient.Pool;
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
public class PostgreSQLConnectPool implements DataBaseConnectPool<Pool, PgConnection> {
    volatile Map<String, Pool> pools;

    ConfigDao configDao;

    @Autowired(required = false)
    public void setConfigDao(ConfigDao configDao) {
        this.configDao = configDao;
    }

    @Override
    public synchronized Pool getPool(String name) {
        if (!getPools().containsKey(name)) {
            setInstance(this.configDao.getDatabase(name));
        }
        return getPools().get(name);
    }

    @Override
    public Map<String, Pool> getPools() {
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
        getPools().put(config.getName(), PgBuilder.pool().using(Task.getVertx()).connectingTo( connectOptions).with(poolOptions).build());
    }

    /*public Future<PgConnection> getConnect(String name) {
        return (Future) getPool(name).getConnection();
    }*/
    final static String pageSqlSign = "/*@#END%&!/";

    @Override
    public String toPageSql(String body) {
        return body + pageSqlSign;
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
        if (sql.endsWith(pageSqlSign)) {
            sql = sql.replace(pageSqlSign, " limit $" + (i + 1) + " offset $" + (i + 2));
        }
        return sql;
    }


    @Override
    public void removeInstance(String name) {
        getPools().remove(name);
    }
}
