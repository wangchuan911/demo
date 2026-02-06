package org.welisdoom.task.xml.connect.sync;

import io.vertx.sqlclient.Tuple;
import org.springframework.stereotype.Component;
import org.welisdoom.task.xml.connect.Db;
import org.welisdoon.common.MyBatisUtils;
import org.welisdoon.common.data.BaseCondition;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

/**
 * @Classname OracleConnect
 * @Description TODO
 * @Author Septem
 * @Date 13:52
 */
@Component
@Db("postgresql")
public class PostgreSQLConnectManager extends DatasouceConnectManager {

    @Override
    public String toPageSql(String body) {
        return body + " limit ? offset ? ";
    }

    public void setPage(Tuple tuple, BaseCondition.Page page) {
        tuple.addValue(page.getPageSize());
        tuple.addValue(page.getStart() - 1);
    }

    @Override
    public String setPage(PreparedStatement preparedStatement, BaseCondition.Page page) throws SQLException {
        page.setStartIndex(0);
        int index = preparedStatement.getParameterMetaData().getParameterCount();
        preparedStatement.setLong(index - 1, page.getPageSize());
        preparedStatement.setLong(index, page.getStart());
        return page.getPageSize() + "," + page.getStart();
    }

    public String sqlFormat(String sql, List<Object> param) {
        return sql.replaceAll(MyBatisUtils.PATTERN_STRING, "?");
    }


}
