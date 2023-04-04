package org.welisdoom.task.xml.connect;

import io.vertx.core.Future;
import io.vertx.oracleclient.OraclePool;
import io.vertx.pgclient.PgConnectOptions;
import io.vertx.pgclient.PgConnection;
import io.vertx.pgclient.PgPool;
import io.vertx.sqlclient.*;
import org.springframework.stereotype.Component;
import org.welisdoom.task.xml.entity.TaskRequest;
import org.welisdoon.common.data.BaseCondition;
import org.welisdoon.web.common.ApplicationContextProvider;
import org.welisdoon.web.vertx.verticle.WorkerVerticle;

import java.util.HashMap;
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

    @Override
    public PgPool getPool(String name) {
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
        pools.put(config.getName(), PgPool.pool(ApplicationContextProvider.getBean(WorkerVerticle.class).pool().getOne(), connectOptions, poolOptions));
    }

    public Future<PgConnection> getConnect(String name) {
        return (Future) getPool(name).getConnection();
    }

    public Future<RowSet<Row>> page(SqlConnection connection, String sql, BaseCondition<Long, TaskRequest> data) {
        sql = sql + String.format(" limit %d offset %d", data.getPage().getPageSize(), data.getPage().getStart());
        return connection.preparedQuery(sql).execute(Tuple.tuple());
    }

    @Override
    public void removeInstance(String name) {
        pools.remove(name);
    }
}
