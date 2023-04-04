package org.welisdoom.task.xml.connect;

import com.alibaba.fastjson.JSONObject;
import io.vertx.core.Future;
import io.vertx.oracleclient.OracleConnectOptions;
import io.vertx.oracleclient.OracleConnection;
import io.vertx.oracleclient.OraclePool;
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
@Db("oracle")
public class OracleConnectPool implements DataBaseConnectPool<OraclePool, OracleConnection> {
    static Map<String, OraclePool> pools = new HashMap<>();

    public void setInstance(DatabaseLinkInfo config) {
        OracleConnectOptions connectOptions = new OracleConnectOptions()
                .setPort(config.getPort())
                .setHost(config.getHost())
                .setDatabase(config.getDatabase())
                .setUser(config.getUser())
                .setPassword(config.getPw());

// Pool Options
        PoolOptions poolOptions = new PoolOptions().setMaxSize(10);

// Create the pool from the data object
        pools.put(config.getName(), OraclePool.pool(WorkerVerticle.pool().getOne(), connectOptions, poolOptions));
    }

    @Override
    public OraclePool getPool(String name) {
        return pools.get(name);
    }

    public Future<OracleConnection> getConnect(String name) {
        return (Future) getPool(name).getConnection();
    }

    @Override
    public Future<RowSet<Row>> page(SqlConnection connection, String sql, BaseCondition<Long, TaskRequest> data) {
        sql = String.format("select * from (select a.*,rownum rn from (%s and rownum <= %d)) where rn> %d", data.getPage().getEnd(), data.getPage().getStart());
        return connection.preparedQuery(sql).execute(Tuple.tuple());
    }

    @Override
    public void removeInstance(String name) {
        pools.remove(name);
    }

}
