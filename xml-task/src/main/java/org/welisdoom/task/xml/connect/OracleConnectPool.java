package org.welisdoom.task.xml.connect;

import io.vertx.oracleclient.OracleConnectOptions;
import io.vertx.oracleclient.OracleConnection;
import io.vertx.oracleclient.OraclePool;
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
@Db("oracle")
public class OracleConnectPool implements DataBaseConnectPool<OraclePool, OracleConnection> {
    volatile Map<String, OraclePool> pools;

    ConfigDao configDao;

    @Autowired(required = false)
    public void setConfigDao(ConfigDao configDao) {
        this.configDao = configDao;
    }

    public void setInstance(DatabaseLinkInfo config) {
        OracleConnectOptions connectOptions = getSqlConnectOptions(new OracleConnectOptions(), config);

// Pool Options
        PoolOptions poolOptions = getPoolOptions();

// Create the pool from the data object
        if (getPools().containsKey(config.getName())) return;
        getPools().put(config.getName(), OraclePool.pool(Task.getVertx(), connectOptions, poolOptions));
    }

    @Override
    public synchronized OraclePool getPool(String name) {
        if (!getPools().containsKey(name)) {
            setInstance(this.configDao.getDatabase(name));
        }
        return getPools().get(name);
    }

    @Override
    public Map<String, OraclePool> getPools() {
        if (pools == null) {
            synchronized (this) {
                if (pools == null) {
                    pools = new HashMap<>();
                }
            }
        }
        return pools;
    }

    /*public Future<OracleConnection> getConnect(String name) {
        return (Future) getPool(name).getConnection();
    }*/


    @Override
    public String toPageSql(String body) {
        return String.format("select * from (select a.*,rownum as \"@RowNum\" from (%s and rownum <= ?)a) where \"@RowNum\">= ?", body);
    }

    @Override
    public void setPage(Tuple tuple, BaseCondition.Page page) {
        tuple.addValue(page.getEnd());
        tuple.addValue(page.getStart());
    }

    @Override
    public String sqlFormat(String sql, List<Object> param) {
        return sql.replaceAll(PATTERN_STRING, "?");
    }


    @Override
    public void removeInstance(String name) {
        getPools().remove(name);
    }

}
