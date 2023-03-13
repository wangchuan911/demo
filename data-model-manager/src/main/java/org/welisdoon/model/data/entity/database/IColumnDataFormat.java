package org.welisdoon.model.data.entity.database;

import org.apache.ibatis.type.JdbcType;
import org.welisdoon.model.data.utils.TableResultUtils;
import org.welisdoon.web.common.ApplicationContextProvider;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * @Classname IColumnDataFormater
 * @Description TODO
 * @Author Septem
 * @Date 15:00
 */


public interface IColumnDataFormat {

    void deserialize(IColumnValue column);

    void serialize(IColumnValue column);

    @Target({ElementType.TYPE})
    @Retention(RetentionPolicy.RUNTIME)
    @interface JDBCType {
        JdbcType[] value();
    }

    Map<JdbcType, Class<? extends IColumnDataFormat>> iColumnDataFormatMap = new HashMap<>();

    static void setValue(Map<String, Object> object, TableEntity table) {
        setValue(object, table.getColumns());
    }

    static void setValue(Map<String, Object> object, ColumnEntity... entities) {
        IColumnDataFormat iColumnDataFormat;
        String fName;
        Map<JdbcType, IColumnDataFormat> formatMap = new HashMap<>();
        try {
            for (ColumnEntity entity : entities) {
                iColumnDataFormat = Optional.ofNullable(formatMap.get(entity.getType())).orElseGet(() -> {
                    IColumnDataFormat format = ApplicationContextProvider.getApplicationContext().getBean(iColumnDataFormatMap.get(entity.getType()));
                    formatMap.put(entity.getType(), format);
                    return format;
                });
                fName = String.format("F%d", entity.getId());
                if (!object.containsKey(fName)) continue;
                entity.setFormatValue(object.get(fName));
                iColumnDataFormat.deserialize(entity);
            }
        } finally {
            formatMap.clear();
        }
    }

    static void setFormatValue(long id, TableEntity table) {
        Map<String, Object> result = TableResultUtils.queryForMap(id, table.getColumns());
        setFormatValue(result, table.getColumns());
    }

    static void setFormatValue(Map<String, Object> object, ColumnEntity... entities) {
        IColumnDataFormat iColumnDataFormat;
        Map<JdbcType, IColumnDataFormat> formatMap = new HashMap<>();
        try {
            for (ColumnEntity entity : entities) {
                iColumnDataFormat = Optional.ofNullable(formatMap.get(entity.getType())).orElseGet(() -> {
                    IColumnDataFormat format = ApplicationContextProvider.getApplicationContext().getBean(iColumnDataFormatMap.get(entity.getType()));
                    formatMap.put(entity.getType(), format);
                    return format;
                });
                if (!object.containsKey(entity.getCode())) continue;
                entity.setValue(object.get(entity.getCode()));
                iColumnDataFormat.serialize(entity);
            }
        } finally {
            formatMap.clear();
        }
    }
}
