package org.welisdoon.metadata.prototype.handle.link.construction.sql.content.xml.entity;

import io.vertx.core.Completable;
import io.vertx.core.Future;
import io.vertx.jdbcclient.JDBCConnectOptions;
import io.vertx.jdbcclient.JDBCPool;
import io.vertx.sqlclient.*;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.lang.Nullable;
import org.welisdoon.web.common.ApplicationContextProvider;
import org.welisdoon.web.vertx.verticle.WorkerVerticle;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @Classname PgDataBasePool
 * @Description TODO
 * @Author Septem
 * @Date 12:02
 */

public class VertxSqlDataBasePool {

    protected Map<String, Pool> dataBases = new HashMap();


    public JDBCConnectOptions getJDBCConnectOptions(String name) {
        return ApplicationContextProvider.getBean(JdbcTemplate.class).queryForObject("select * from md_connect where name=? and model=8000", (rs, rowNum) -> {
            JDBCConnectOptions jdbcConnectOptions = new JDBCConnectOptions();
            jdbcConnectOptions.setJdbcUrl(rs.getString("URL"));
            jdbcConnectOptions.setPassword(rs.getString("PASSWORD"));
            jdbcConnectOptions.setUser(rs.getString("USER"));
            return jdbcConnectOptions;
        }, name);
    }

    public void getConnect(String name, Function<SqlConnection, Future<?>> sqlConnectionCompletable) {
        if (!dataBases.containsKey(name)) {
            synchronized (dataBases) {
                if (!dataBases.containsKey(name)) {
                    initDataSource(name);
                }
            }
        }
        dataBases.get(name).getConnection()
                .andThen(connection -> {
                    if (connection.succeeded()) {
                        try {
                            sqlConnectionCompletable.apply(connection.result()).eventually(connection.result()::close);
                        } catch (Throwable e) {
                            connection.result().close();
                        }
                    }
                });
    }

    public <T> Future<List<T>> select(SqlConnection connection, RowMapper<T> tRowMapper, String sql, Object... params) {
        return connection.preparedQuery(sql).execute(Tuple.from(params)).compose(rows -> {
            AtomicInteger index = new AtomicInteger(0);
            return Future.succeededFuture(rows.stream().map(row -> tRowMapper.mapRow(row, index.getAndIncrement())).collect(Collectors.toList()));
        });
    }

    public <T> Future<List<T>> update(SqlConnection connection, Class<T> keyType, String sql, Object... params) {
        return connection.preparedQuery(sql).execute(Tuple.from(params)).compose(rows -> {
            Row lastInsertId = rows.property(JDBCPool.GENERATED_KEYS);
            // just refer to the position as usual:
            List<T> list = new LinkedList<>();
            for (int i = 0; i < lastInsertId.size(); i++) {
                list.add(lastInsertId.get(keyType, i));
            }
            return Future.succeededFuture(list);
        });
    }

    public void initDataSource(String name) {
        if (!this.dataBases.containsKey(name)) {
            synchronized (this) {
                if (!this.dataBases.containsKey(name)) {
                    JDBCConnectOptions connectOptions = getJDBCConnectOptions(name);
                    PoolOptions poolOptions = new PoolOptions()
                            .setMaxSize(16);
//                    WorkerVerticle
                    Pool pool = JDBCPool.pool(ApplicationContextProvider.getBean(WorkerVerticle.class).getVertx(), connectOptions, poolOptions);
                    dataBases.put(name, pool);
                }
            }
        }
    }


    public enum Model {
        DATASOURCE(8000), URL(8001), FTP(8002), UNKNOWN(null);

        Integer typeId;

        Model(Integer typeId) {
            this.typeId = typeId;
        }

    }


    @FunctionalInterface
    public interface RowMapper<T> {
        @Nullable
        T mapRow(Row rs, int rowNum);
    }

}
