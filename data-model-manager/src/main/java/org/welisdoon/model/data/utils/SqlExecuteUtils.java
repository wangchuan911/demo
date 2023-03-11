package org.welisdoon.model.data.utils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.welisdoon.model.data.entity.database.AbstractDataEntity;
import org.welisdoon.model.data.entity.database.ColumnEntity;
import org.welisdoon.model.data.entity.database.TableEntity;

import javax.annotation.PostConstruct;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @Classname SqlExecuteUtils
 * @Description TODO
 * @Author Septem
 * @Date 17:45
 */
@Component
public class SqlExecuteUtils {
    static JdbcTemplate jdbcTemplate;
    final static String SQL = "select %s from %s where %s";

    @Autowired
    SqlExecuteUtils(JdbcTemplate jdbcTemplate) {
        SqlExecuteUtils.jdbcTemplate = jdbcTemplate;
    }


    public static <T> List<T> queryForList(Object id, Class<T> returnType, TableEntity table) {
        return queryForList(id, returnType, table.getColumns());
    }

    public static <T> List<T> queryForList(Object id, Class<T> returnType, ColumnEntity... columns) {
        TableEntity table = columns[0].getTable();
        String sql = String.format(SQL, Arrays.stream(columns).map(AbstractDataEntity::getCode).collect(Collectors.joining(",")), table.getCode(), String.format("%s = ?", table.getPrimary().getCode()));
        return jdbcTemplate.queryForList(sql, new Object[]{id}, new int[]{table.getPrimary().getType().TYPE_CODE}, returnType);
    }

    public static <T> T queryForObject(Object id, Class<T> returnType, ColumnEntity column) {
        return queryForList(id, returnType, column).get(0);
    }
}
