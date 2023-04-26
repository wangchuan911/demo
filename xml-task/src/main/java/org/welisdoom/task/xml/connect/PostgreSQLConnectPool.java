package org.welisdoom.task.xml.connect;

import io.vertx.core.Future;
import io.vertx.oracleclient.OraclePool;
import io.vertx.pgclient.PgConnectOptions;
import io.vertx.pgclient.PgConnection;
import io.vertx.pgclient.PgPool;
import io.vertx.sqlclient.*;
import org.apache.ibatis.type.JdbcType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.welisdoom.task.xml.dao.ConfigDao;
import org.welisdoom.task.xml.entity.Task;
import org.welisdoom.task.xml.entity.TaskRequest;
import org.welisdoon.common.data.BaseCondition;
import org.welisdoon.web.common.ApplicationContextProvider;
import org.welisdoon.web.vertx.verticle.WorkerVerticle;

import java.sql.SQLType;
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
    static Map<String, PgPool> pools = new HashMap<>();

    ConfigDao configDao;

    @Autowired
    public void setConfigDao(ConfigDao configDao) {
        this.configDao = configDao;
    }

    @Override
    public PgPool getPool(String name) {
        if (!pools.containsKey(name)) {
            setInstance(this.configDao.getDatabase(name));
        }
        return pools.get(name);
    }

    @Override
    public void setInstance(DatabaseLinkInfo config) {
        PgConnectOptions connectOptions = new PgConnectOptions()
                .setPort(config.getPort())
                .setHost(config.getHost())
                .setDatabase(config.getDatabase())
                .setUser(config.getUser())
                .setPassword(config.getPw());

// Pool options
        PoolOptions poolOptions = new PoolOptions()
                .setMaxSize(5);

// Create the client pool
        pools.put(config.getName(), PgPool.pool(Task.getVertx(), connectOptions, poolOptions));
    }

    public Future<PgConnection> getConnect(String name) {
        return (Future) getPool(name).getConnection();
    }

    @Override
    public String toPageSql(String body) {
        return body + String.format(" limit ? offset ?");
    }

    public void setPage(Tuple tuple, BaseCondition.Page page) {
        tuple.addValue(page.getPageSize());
        tuple.addValue(page.getStart() - 1);
    }

    @Override
    public String sqlFormat(String sql, List<Object> param) {
        for (int i = 0; i < param.size(); i++) {
            sql = sql.replaceFirst(PATTERN_STRING, "\\$" + (i + 1));
        }
        return sql;
    }


    @Override
    public void removeInstance(String name) {
        pools.remove(name);
    }
}
