package org.welisdoon.metadata.prototype.handle.link.construction.sql.content.xml.entity;

import io.vertx.core.Future;
import io.vertx.jdbcclient.JDBCConnectOptions;
import io.vertx.jdbcclient.JDBCPool;
import io.vertx.sqlclient.*;
import org.apache.commons.io.IOUtils;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;
import org.welisdoon.common.ObjectUtils;
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
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @Classname PgDataBasePool
 * @Description TODO
 * @Author Septem
 * @Date 12:02
 */
@Component
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
                .compose(connection -> {
                    try {
                        return sqlConnectionCompletable.apply(connection).eventually(connection::close);
                    } catch (Throwable e) {
                        connection.close();
                        return Future.failedFuture(e);
                    }
                }, Future::failedFuture);
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


    public void page(String method, TemplateFormatContent templateFormatContent,
                     TemplateIntance templateParameter,
                     BiConsumer<List<?>, Throwable> listConsumer) {

        Mappers.Mapper mapper;
        if (templateParameter.template == null) {
            Mappers mappers = ObjectUtils.synchronizedGet(MAPPERS, stringMappersMap -> stringMappersMap.get(templateParameter.database), stringMappersMap -> {
                Mappers mappers1 = new Mappers(templateParameter.database);
                stringMappersMap.put(templateParameter.database, mappers1);
                return mappers1;
            });
            templateFormatContent.build();


            mapper = mappers.getChild(likeMyBatisSqlNode -> Objects.equals(likeMyBatisSqlNode.getId(), templateParameter.nameSpace))
                    .stream().findFirst().map(likeMyBatisSqlNode -> (Mappers.Mapper) likeMyBatisSqlNode).orElseGet(() -> {
                        try {
                            Mappers.Mapper mapper1 = LikeMyBatisSAXParser.load(IOUtils.toInputStream((CharSequence) templateFormatContent.getValue(), Charset.defaultCharset()), mappers);
                            mappers.addChildren(mapper1);
                            return mapper1;
                        } catch (ParserConfigurationException | SAXException | IOException e) {
                            throw new IllegalStateException(e);
                        }
                    });

        } else {
            mapper = templateParameter.template;
        }
        SqlParameter sqlParameter = templateParameter.getSqlParameter();
        mapper.generateSqlInfo(method, sqlParameter);
        String sql;
        Object[] objects;
        if (templateParameter.page != null) {
            sql = sqlParameter.sql + " limit ? offset ?";
            objects = Arrays.copyOf(sqlParameter.params.toArray(), sqlParameter.params.size() + 2);
            objects[objects.length - 2] = templateParameter.page.getPageSize();
            objects[objects.length - 1] = templateParameter.page.getPageSize() * templateParameter.page.getPage();
        } else {
            sql = sqlParameter.sql;
            objects = sqlParameter.params.toArray();
        }
        RowMapper tRowMapper = (rs, rowNum) -> {
            Map<String, Object> map = new HashMap<>(rs.size(), 1.f);
            for (int i = 0; i < rs.size(); i++) {
                map.put(rs.getColumnName(i), rs.getValue(i));
            }
            return map;
        };
        getConnect(templateParameter.database, sqlConnection -> {
            return select(sqlConnection, tRowMapper, sql, objects)
                    .onComplete((result, failure) -> {
                        listConsumer.accept((List) result, failure);
                    });
        });
    }

    @FunctionalInterface
    protected interface RowMapper<T> {
        @Nullable
        T mapRow(Row rs, int rowNum);
    }


}
