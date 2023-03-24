package org.welisdoon.model.data.utils;

import com.alibaba.fastjson.JSON;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.welisdoon.common.ObjectUtils;
import org.welisdoon.model.data.components.foreign.IForeignKeyOperator;
import org.welisdoon.model.data.entity.common.AbstractCommonEntity;
import org.welisdoon.model.data.entity.database.ColumnEntity;
import org.welisdoon.model.data.entity.database.IColumnDataFormat;
import org.welisdoon.model.data.entity.database.TableEntity;
import org.welisdoon.model.data.entity.object.DataObjectEntity;
import org.welisdoon.model.data.entity.object.FieldEntity;
import org.welisdoon.web.common.ApplicationContextProvider;

import java.util.*;
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
    static Map<Class<? extends ColumnEntity>, IForeignKeyOperator<ColumnEntity>> operator = new HashMap<>();

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
        System.out.println(id);
        return jdbcTemplate.queryForList(generateSql(table, columns), params(id), argTypes(table.getPrimary().getType().TYPE_CODE)).stream().findFirst().orElseGet(() -> Map.of());
    }

    public static Map<String, Object> queryForMap(Object id, TableEntity table) {
        return queryForMap(id, table.getColumns());
    }

    public static Map<String, Object> query(Object id, TableEntity table) {
        return query(id, table, (columnEntity, tableEntity) -> {
            query(columnEntity.getValue(), tableEntity, (columnEntity1, tableEntity1) -> {
            });
        });
    }

    public static Map<String, Object> query(Object id, TableEntity table, QueryColumnInnerCallBack callBack) {
        Map<String, Object> result = TableResultUtils.queryForMap(id, table);
        IColumnDataFormat.setFormatValue(result, table.getColumns());
        IColumnDataFormat.setFormatValue(result, table.getPrimary());
        Map<ColumnEntity, TableEntity> map = new LinkedHashMap<>();
        for (ColumnEntity column : table.getColumns()) {
            if (column.getForeign() != null && column.getValue() != null) {
                map.put(column, getTable(column));
//                query(column.getValue(), map.get(column));
                callBack.callback(column, map.get(column));
            }
        }
        for (Map.Entry<ColumnEntity, TableEntity> entry : map.entrySet()) {
            entry.getKey().getForeign().setTarget(entry.getValue());
            entry.getKey().setFormatValue(Arrays.stream(entry.getValue().getColumns())
                    .filter(columnEntity -> Objects.nonNull(columnEntity.getFormatValue()))
                    .collect(Collectors.toMap(ColumnEntity::getCode, ColumnEntity::getFormatValue, (o, o2) -> o)));
        }
        return result;
    }

    protected static TableEntity getTable(ColumnEntity column) {
        try {
            return ObjectUtils.getMapValueOrNewSafe(operator, column.getClass(), () ->
                    ApplicationContextProvider.getBean(column.getClass().getAnnotation(IForeignKeyOperator.ForeignKey.class).operator())
            ).getTable(column);
        } catch (Throwable throwable) {
            throwable.printStackTrace();
            throw new RuntimeException(throwable.getMessage(), throwable);
        }
    }

    @FunctionalInterface
    interface QueryColumnInnerCallBack {
        void callback(ColumnEntity columnEntity, TableEntity tableEntity);
    }

    public static Map<String, Object> query(Object id, DataObjectEntity objectEntity) {
        return query(id, objectEntity, (columnEntity, tableEntity) -> {
            query(columnEntity.getValue(), tableEntity, (columnEntity1, tableEntity1) -> {
            });
        });
    }

    public static Map<String, Object> query(Object id, DataObjectEntity objectEntity, QueryColumnInnerCallBack callBack) {
        ColumnEntity[] columns;
        {
            Set<ColumnEntity> columnSet = Arrays.stream(objectEntity.getFields()).flatMap(fieldEntity -> Arrays.stream(fieldEntity.getColumns())).distinct().collect(Collectors.toSet());
            columnSet.add(objectEntity.getTable().getPrimary());
            columns = columnSet.toArray(ColumnEntity[]::new);
        }

        Map<String, Object> result = TableResultUtils.queryForMap(id, columns);
        IColumnDataFormat.setFormatValue(result, columns);
        Map<ColumnEntity, TableEntity> map = new LinkedHashMap<>();
        for (ColumnEntity column : columns) {
            if (column.getForeign() != null && column.getValue() != null) {
                map.put(column, getTable(column));
//                query(column.getValue(), map.get(column));
                callBack.callback(column, map.get(column));
            }
        }
        for (Map.Entry<ColumnEntity, TableEntity> entry : map.entrySet()) {
            entry.getKey().getForeign().setTarget(entry.getValue());
            entry.getKey().setFormatValue(
                    JSON.toJSONString(Arrays.stream(entry.getValue().getColumns())
                            .filter(columnEntity -> Objects.nonNull(columnEntity.getFormatValue()))
                            .collect(Collectors.toMap(ColumnEntity::getCode, ColumnEntity::getFormatValue, (o, o2) -> o))));
        }
        return result;
    }
}
