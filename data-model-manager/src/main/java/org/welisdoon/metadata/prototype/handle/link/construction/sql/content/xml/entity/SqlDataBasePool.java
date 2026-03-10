package org.welisdoon.metadata.prototype.handle.link.construction.sql.content.xml.entity;

import com.alibaba.druid.pool.DruidDataSource;
import org.apache.logging.log4j.util.TriConsumer;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.welisdoon.common.MyBatisUtils;
import org.welisdoon.common.object.wrapper.PagePrepare;
import org.welisdoon.common.object.wrapper.Prepare;
import org.welisdoon.web.common.ApplicationContextProvider;

import javax.sql.DataSource;
import java.sql.*;
import java.sql.PreparedStatement;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Function;

/**
 * @Classname PgDataBasePool
 * @Description TODO
 * @Author Septem
 * @Date 12:02
 */
@Component
public class SqlDataBasePool {
//    static Map<String, Mappers> MAPPERS = new HashMap<>();

    protected static final Map<String, DataSource> DATA_SOURCE_MAP = new HashMap();
    protected static final ThreadLocal<Connection> THREAD_LOCAL = new InheritableThreadLocal<>();

    protected Connection getConnect(String name) throws SQLException {
        if (THREAD_LOCAL.get() != null) {
            return THREAD_LOCAL.get();
        }
        DataSource dataSource = DATA_SOURCE_MAP.get(name);
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
            PagePrepare pagePrepare = new PagePrepare(prepare, PagePrepare.DefaultPageFormat.mysql);

            boolean hasMoreAllPage = false;
            boolean hasMoreOnePage;
            PreparedStatement preparedStatement = MyBatisUtils.prepared(connection, pagePrepare);
            do {
                ResultSet resultSet = preparedStatement.executeQuery();
                hasMoreOnePage = false;
                int i = 0;
                ResultSetMetaData metaData = resultSet.getMetaData();
                try (resultSet) {
                    while (resultSet.next()) {
                        hasMoreOnePage = true;
                        hasMoreAllPage = true;
                        consumer.accept(metaData, resultSet, i++);
                    }
                }
                pagePrepare.nextPage(preparedStatement);
            } while (hasMoreOnePage);
            return hasMoreAllPage;
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



}
