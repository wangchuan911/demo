package org.welisdoom.task.xml.connect.sync;

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
@Db("oracle")
public class OracleConnectManager extends DatasouceConnectManager {


    @Override
    public String toPageSql(String body) {
        return String.format("select * from (select a.*,rownum as \"@RowNum\" from (%s and rownum <= ?)a) where \"@RowNum\">= ?", body);
    }

    @Override
    public void setPage(PreparedStatement preparedStatement, BaseCondition.Page page) throws SQLException {
        int index = preparedStatement.getParameterMetaData().getParameterCount();
        preparedStatement.setLong(index, page.getEnd());
        preparedStatement.setLong(index + 1, page.getStart());
    }

    @Override
    public String sqlFormat(String sql, List<Object> param) {
        return sql.replaceAll(MyBatisUtils.PATTERN_STRING, "?");
    }


}
