package org.welisdoon.model.data.entity.database;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.annotation.JSONField;
import org.apache.ibatis.type.JdbcType;
import org.welisdoon.web.common.ApplicationContextProvider;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.HashMap;
import java.util.Map;

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

    static void setValue(JSONObject object, ColumnEntity... entities) {
        IColumnDataFormat iColumnDataFormat;
        String fName;
        for (ColumnEntity entity : entities) {
            iColumnDataFormat = ApplicationContextProvider.getApplicationContext().getBean(iColumnDataFormatMap.get(entity.getType()));
            fName = String.format("F%d", entity.getId());
            if (!object.containsKey(fName)) continue;
            entity.setFormatValue(object.get(fName));
            iColumnDataFormat.deserialize(entity);
        }
    }
}
