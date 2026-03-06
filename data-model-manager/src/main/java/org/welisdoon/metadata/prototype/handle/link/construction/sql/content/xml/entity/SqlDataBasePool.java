package org.welisdoon.metadata.prototype.handle.link.construction.sql.content.xml.entity;

import com.alibaba.druid.pool.DruidDataSource;
import io.vertx.core.Future;
import io.vertx.jdbcclient.JDBCConnectOptions;
import io.vertx.jdbcclient.JDBCPool;
import io.vertx.sqlclient.*;
import org.apache.logging.log4j.util.TriConsumer;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;
import org.welisdoon.common.MyBatisUtils;
import org.welisdoon.common.object.wrapper.PagePrepare;
import org.welisdoon.common.object.wrapper.Prepare;
import org.welisdoon.web.common.ApplicationContextProvider;
import org.welisdoon.web.vertx.verticle.WorkerVerticle;

import javax.sql.DataSource;
import java.sql.*;
import java.sql.PreparedStatement;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * @Classname PgDataBasePool
 * @Description TODO
 * @Author Septem
 * @Date 12:02
 */
@Component
public class SqlDataBasePool {
//    static Map<String, Mappers> MAPPERS = new HashMap<>();

    protected Map<String, DataSource> dataBases = new HashMap();

    protected Connection getConnect(String name) throws SQLException {
        DataSource dataSource = dataBases.get(name);
        if (dataSource == null)
            dataSource = ApplicationContextProvider.getBean(JdbcTemplate.class).queryForObject("select * from md_connect where name = ? and model = ?", (rs, rowNum) -> {
                DruidDataSource druidDataSource = new DruidDataSource();
                String url = rs.getString("URL");
                druidDataSource.setUrl(url);
                druidDataSource.setUsername(rs.getString("USER"));
                druidDataSource.setPassword(rs.getString("PASSWORD"));
                if (url.contains("mysql")) {
                    druidDataSource.setDriverClassName("com.mysql.jdbc.Driver");
                } else if (url.contains("oracle")) {
                    druidDataSource.setDriverClassName("oracle.jdbc.driver.OracleDriver");
                } else if (url.contains("postgresql")) {
                    druidDataSource.setDriverClassName("org.postgresql.Driver");
                } else {
                    throw new IllegalArgumentException("");
                }
                return (DataSource) druidDataSource;
            }, name, Model.DATASOURCE);
        return dataSource.getConnection();
    }


    protected boolean page(String name, Prepare prepare, TriConsumer<ResultSetMetaData, ResultSet, Integer> consumer) throws SQLException {
        try (Connection connection = getConnect(name)) {
            PagePrepare pagePrepare = new PagePrepare(prepare, new PagePrepare.Format() {
                @Override
                public String getStart() {
                    return "";
                }

                @Override
                public String getEnd() {
                    return " limit ? offset";
                }

                @Override
                public int getPageArg1(int page, int size) {
                    return (page - 1) * size;
                }

                @Override
                public int getPageArg2(int page, int size) {
                    return size;
                }
            });

            boolean next1 = false;
            boolean next;
            PreparedStatement preparedStatement = MyBatisUtils.prepared(connection, pagePrepare);
            do {
                ResultSet resultSet = preparedStatement.executeQuery();
                next = false;
                int i = 0;
                ResultSetMetaData metaData = resultSet.getMetaData();
                while (resultSet.next()) {
                    next = true;
                    next1 = true;
                    consumer.accept(metaData, resultSet, i++);
                }
                pagePrepare.nextPage(preparedStatement);
            } while (next);
            return next1;
        }
    }

    protected int update(String name, Prepare prepare) throws SQLException {
        try (Connection connection = getConnect(name)) {
            PreparedStatement preparedStatement = MyBatisUtils.prepared(connection, prepare);
            return preparedStatement.executeUpdate();
        }
    }


    protected enum Model {
        DATASOURCE(8000), URL(8001), FTP(8002), UNKNOWN(null);

        Integer typeId;

        Model(Integer typeId) {
            this.typeId = typeId;
        }

    }

    protected enum Datasource {
        mysql(new PagePrepare.Format() {
            @Override
            public String getStart() {
                return null;
            }

            @Override
            public String getEnd() {
                return null;
            }

            @Override
            public int getPageArg1(int page, int size) {
                return 0;
            }

            @Override
            public int getPageArg2(int page, int size) {
                return 0;
            }
        }), oracle(new PagePrepare.Format() {
            @Override
            public String getStart() {
                return "select * from (select rownum as rn,* from (";
            }

            @Override
            public String getEnd() {
                return " ) where rownum<? ) and rn >?";
            }

            @Override
            public int getPageArg1(int page, int size) {
                return ((page - 1) * size) + 1;
            }

            @Override
            public int getPageArg2(int page, int size) {
                return page * size;
            }
        }),
        pg(new PagePrepare.Format() {
            @Override
            public String getStart() {
                return null;
            }

            @Override
            public String getEnd() {
                return null;
            }

            @Override
            public int getPageArg1(int page, int size) {
                return 0;
            }

            @Override
            public int getPageArg2(int page, int size) {
                return 0;
            }
        });
        PagePrepare.Format format;

        Datasource(PagePrepare.Format format) {
            this.format = format;
        }
    }

}
