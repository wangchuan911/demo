package org.welisdoon.metadata.prototype.handle.link.construction.sql.content.xml.entity;

import io.vertx.core.Completable;
import io.vertx.core.Future;
import io.vertx.jdbcclient.JDBCConnectOptions;
import io.vertx.jdbcclient.JDBCPool;
import io.vertx.sqlclient.*;
import org.apache.commons.io.IOUtils;
import org.checkerframework.checker.nullness.Opt;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.lang.Nullable;
import org.welisdoon.common.ObjectUtils;
import org.welisdoon.common.data.BaseCondition;
import org.welisdoon.metadata.prototype.condition.Page;
import org.welisdoon.metadata.prototype.handle.link.construction.sql.content.TemplateFormatContent;
import org.welisdoon.metadata.prototype.handle.link.construction.sql.content.xml.node.Mappers;
import org.welisdoon.metadata.prototype.handle.link.construction.sql.content.xml.parser.LikeMyBatisSAXParser;
import org.welisdoon.web.common.ApplicationContextProvider;
import org.welisdoon.web.vertx.verticle.WorkerVerticle;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @Classname PgDataBasePool
 * @Description TODO
 * @Author Septem
 * @Date 12:02
 */

public class VertxSqlDataBasePool {
    static Map<String, Mappers> MAPPERS = new HashMap<>();

    protected Map<String, Pool> dataBases = new HashMap();


    protected JDBCConnectOptions getJDBCConnectOptions(String name) {
        return ApplicationContextProvider.getBean(JdbcTemplate.class).queryForObject("select * from md_connect where name=? and model=8000", (rs, rowNum) -> {
            JDBCConnectOptions jdbcConnectOptions = new JDBCConnectOptions();
            jdbcConnectOptions.setJdbcUrl(rs.getString("URL"));
            jdbcConnectOptions.setPassword(rs.getString("PASSWORD"));
            jdbcConnectOptions.setUser(rs.getString("USER"));
            return jdbcConnectOptions;
        }, name);
    }

    protected void getConnect(String name, Function<SqlConnection, Future<?>> sqlConnectionCompletable) {
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

    protected <T> Future<List<T>> select(SqlConnection connection, RowMapper<T> tRowMapper, String sql, Object... params) {
        return connection.preparedQuery(sql).execute(Tuple.from(params)).compose(rows -> {
            AtomicInteger index = new AtomicInteger(0);
            return Future.succeededFuture(rows.stream().map(row -> tRowMapper.mapRow(row, index.getAndIncrement())).collect(Collectors.toList()));
        });
    }

    protected <T> Future<List<T>> update(SqlConnection connection, Class<T> keyType, String sql, Object... params) {
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

    protected void initDataSource(String name) {
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


    protected enum Model {
        DATASOURCE(8000), URL(8001), FTP(8002), UNKNOWN(null);

        Integer typeId;

        Model(Integer typeId) {
            this.typeId = typeId;
        }

    }


    public List<Map<String, Object>> page(String database,
                                          String nameSpace,
                                          String method,
                                          TemplateFormatContent templateFormatContent,
                                          Page page,
                                          SqlParameter sqlParameter) {
        Mappers mappers = ObjectUtils.synchronizedGet(MAPPERS, stringMappersMap -> stringMappersMap.get(database), stringMappersMap -> {
            Mappers mappers1 = new Mappers(database);
            stringMappersMap.put(database, mappers1);
            return mappers1;
        });
        templateFormatContent.build();


        Mappers.Mapper mapper = mappers.getChild(likeMyBatisSqlNode -> Objects.equals(likeMyBatisSqlNode.getId(), nameSpace))
                .stream().findFirst().map(likeMyBatisSqlNode -> (Mappers.Mapper) likeMyBatisSqlNode).orElseGet(() -> {
                    try {
                        return LikeMyBatisSAXParser.load(IOUtils.toInputStream((CharSequence) templateFormatContent.getValue(), Charset.defaultCharset()), mappers);
                    } catch (ParserConfigurationException | SAXException | IOException e) {
                        throw new IllegalStateException(e);
                    }
                });

        mapper.generateSqlInfo(method, sqlParameter);
        switch (sqlParameter.sqlType) {
            case DDL:
                getConnect(database, sqlConnection -> {
                    update(sqlConnection, Object.class, sqlParameter.sql, sqlParameter.params.toArray());
                    return Future.succeededFuture();
                });
            case DQL:
                getConnect(database, sqlConnection -> {
                    select(sqlConnection, (rs, rowNum) -> {
                        return Map.of();
                    }, sqlParameter.sql, sqlParameter.params.toArray());
                    return Future.succeededFuture();
                });
        }
        return null;
    }

    @FunctionalInterface
    protected interface RowMapper<T> {
        @Nullable
        T mapRow(Row rs, int rowNum);
    }


}
