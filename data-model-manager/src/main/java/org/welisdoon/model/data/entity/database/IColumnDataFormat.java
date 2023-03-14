package org.welisdoon.model.data.entity.database;

import org.apache.ibatis.type.JdbcType;
import org.welisdoon.model.data.entity.object.DataObjectEntity;
import org.welisdoon.model.data.entity.object.FieldEntity;
import org.welisdoon.model.data.utils.TableResultUtils;
import org.welisdoon.web.common.ApplicationContextProvider;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.*;

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

    static void setValue(Object id, Map<String, Object> object, DataObjectEntity dataObjectEntity) {
        setValue(object, dataObjectEntity.getFields());
    }

    static void setValue(Map<String, Object> object, DataObjectEntity dataObjectEntity) {
        setValue(object, dataObjectEntity.getFields());
    }

    static void setValue(Map<String, Object> object, FieldEntity... fields) {
        Set<String> keys = object.keySet();
        ColumnEntity entity = null;
        String fKey;
        for (FieldEntity field : fields) {
            for (String key : keys) {
                fKey = String.format("F%s", field.getId());
                if (!key.startsWith(fKey)) continue;

                if (key.equals(fKey)) {
                    entity = field.getColumns()[0];
                    entity.setFormatValue(object.get(key));
                    find(entity.getType()).deserialize(entity);
                    break;
                } else {
                    for (ColumnEntity column : field.getColumns()) {
                        if (String.format("%s_C%s", fKey, column.getId()).equals(key)) {
                            entity = column;
                            break;
                        }
                    }
                    if (entity != null) {
                        entity.setFormatValue(object.get(key));
                        find(entity.getType()).deserialize(entity);
                    }
                    continue;
                }
            }
        }
    }


    static void setValue(Map<String, Object> object, TableEntity table) {
        setValue(object, table.getColumns());
    }

    Map<JdbcType, IColumnDataFormat> formatMap = new HashMap<>();

    static IColumnDataFormat find(JdbcType type) {
        return Optional.ofNullable(formatMap.get(type)).orElseGet(() -> {
            IColumnDataFormat format;
            synchronized (formatMap) {
                format = ApplicationContextProvider.getApplicationContext().getBean(iColumnDataFormatMap.get(type));
                formatMap.put(type, format);
            }
            return format;
        });
    }

    static void setValue(Map<String, Object> object, ColumnEntity... entities) {
        IColumnDataFormat iColumnDataFormat;
        String fName;
        try {
            for (ColumnEntity entity : entities) {
                iColumnDataFormat = find(entity.getType());
                fName = String.format("C%d", entity.getId());
                if (!object.containsKey(fName)) continue;
                entity.setFormatValue(object.get(fName));
                iColumnDataFormat.deserialize(entity);
            }
        } finally {
            formatMap.clear();
        }
    }

    static void setFormatValue(Object id, TableEntity table) {
        Map<String, Object> result = TableResultUtils.queryForMap(id, table.getColumns());
        setFormatValue(result, table.getColumns());
    }

    static void setFormatValue(Map<String, Object> object, ColumnEntity... entities) {
        IColumnDataFormat iColumnDataFormat;
        try {
            for (ColumnEntity entity : entities) {
                iColumnDataFormat = find(entity.getType());
                if (!object.containsKey(entity.getCode())) continue;
                entity.setValue(object.get(entity.getCode()));
                iColumnDataFormat.serialize(entity);
            }
        } finally {
            formatMap.clear();
        }
    }
}
