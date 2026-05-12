package org.welisdoom.task.xml.connect.sync;

import com.alibaba.druid.pool.DruidDataSource;
import org.apache.ibatis.type.JdbcType;
import org.welisdoom.task.xml.intf.ISession;
import org.welisdoon.common.data.BaseCondition;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @Classname D
 * @Description TODO
 * @Author Septem
 * @Date 16:16
 */
public abstract class DatasouceConnectManager implements IConnectManager<Connection> {
    Map<String, DataSource> dataSource = new HashMap<>();

    public DataSource getDataSource(String name) {
        return dataSource.get(name);
    }

    public Map<String, DataSource> getDataSource() {
        return dataSource;
    }

    public void setInstance(DatasourceInfo config) {
        DruidDataSource druidDataSource = new DruidDataSource();
        druidDataSource.setUrl(String.format("jdbc:%s://%s:%d/%s", config.model, config.host, config.port, config.database));
        druidDataSource.setUsername(config.user);
        druidDataSource.setPassword(config.pw);
        switch (config.model) {
            case "mysql":
                druidDataSource.setDriverClassName("com.mysql.jdbc.Driver");
                break;
            case "oracle":
                druidDataSource.setDriverClassName("oracle.jdbc.driver.OracleDriver");
                break;
            case "postgresql":
                druidDataSource.setDriverClassName("org.postgresql.Driver");
                break;
            default:
                druidDataSource.setDriverClassName(config.model);
        }
        dataSource.put(config.getName(), druidDataSource);
    }

    public Connection getConnect(String name, ISession session) throws SQLException {
        return getDataSource(name).getConnection();
    }

    abstract public String toPageSql(String body);

    abstract public long[] setPage(PreparedStatement preparedStatement, BaseCondition.Page page) throws SQLException;

    abstract public String sqlFormat(String sql, List<Object> param);

    public void log(String prefix, String content) {
        System.out.print("==>");
        System.out.print(prefix);
        System.out.print("==>");
        System.out.print("[ ");
        System.out.print(content);
        System.out.println(" ]");
    }

    public static class DatasourceInfo {
        protected String name;
        protected int port;
        protected String host;
        protected String database;
        protected String user;
        protected String pw;
        protected String model;
        protected String url;

        public int getPort() {
            return port;
        }

        public void setPort(int port) {
            this.port = port;
        }

        public String getHost() {
            return host;
        }

        public void setHost(String host) {
            this.host = host;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getDatabase() {
            return database;
        }

        public void setDatabase(String database) {
            this.database = database;
        }

        public String getUser() {
            return user;
        }

        public void setUser(String user) {
            this.user = user;
        }

        public String getPw() {
            return pw;
        }

        public void setPw(String pw) {
            this.pw = pw;
        }

        public String getModel() {
            return model;
        }

        public void setModel(String model) {
            this.model = model;
        }


    }

    public static class SqlTemplate {
        final public String sql;
        final public List<Map.Entry<String, JdbcType>> types;
        final public StringBuilder log1 = new StringBuilder();
        final public StringBuilder log2 = new StringBuilder();

        public SqlTemplate(String sql, List<Map.Entry<String, JdbcType>> types) {
            this.sql = sql;
            this.types = types;
        }

        public List<Map.Entry<String, JdbcType>> getTypes() {
            return types;
        }

        public String getSql() {
            return sql;
        }

    }
}
