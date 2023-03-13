package org.welisdoon.model.data.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.welisdoon.model.data.entity.common.AbstractCommonEntity;
import org.welisdoon.model.data.entity.database.AbstractDataEntity;
import org.welisdoon.model.data.entity.database.ColumnEntity;
import org.welisdoon.model.data.entity.database.TableEntity;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @Classname SqlExecuteUtils
 * @Description TODO
 * @Author Septem
 * @Date 17:45
 */
@Component
public class TableResultUtils {
    private static final Logger logger = LoggerFactory.getLogger(TableResultUtils.class);
    static JdbcTemplate jdbcTemplate;
    final static String SQL = "select %s from %s where %s";

    @Autowired
    TableResultUtils(JdbcTemplate jdbcTemplate) {
        TableResultUtils.jdbcTemplate = jdbcTemplate;
    }


    public static <T> List<T> queryForList(Object id, Class<T> returnType, TableEntity table) {
        return queryForList(id, returnType, table.getColumns());
    }

    protected static String generateSql(TableEntity table, ColumnEntity... columns) {
        String sql = String.format(SQL, Arrays.stream(columns).map(AbstractCommonEntity::getCode).collect(Collectors.joining(",")), table.getCode(), String.format("%s = ?", table.getPrimary().getCode()));
        logger.info(sql);
        return sql;
    }

    public static int[] argTypes(int... ints) {
        return ints;
    }

    public static Object[] params(Object... params) {
        return params;
    }

    public static <T> List<T> queryForList(Object id, Class<T> returnType, ColumnEntity... columns) {
        TableEntity table = columns[0].getTable();
        String sql = generateSql(table, columns);
        logger.info(sql);
        return jdbcTemplate.queryForList(sql, params(id), argTypes(table.getPrimary().getType().TYPE_CODE), returnType);
    }

    public static <T> T queryForObject(Object id, Class<T> returnType, ColumnEntity... columns) {
        return queryForList(id, returnType, columns).get(0);
    }

    public static Map<String, Object> queryForMap(Object id, ColumnEntity... columns) {
        TableEntity table = columns[0].getTable();
        return jdbcTemplate.queryForMap(generateSql(table, columns), params(id), argTypes(table.getPrimary().getType().TYPE_CODE));
    }
}
