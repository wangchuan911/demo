package org.welisdoon.model.data.components.column;

import org.apache.ibatis.type.JdbcType;
import org.springframework.stereotype.Component;
import org.welisdoon.model.data.entity.database.ColumnEntity;
import org.welisdoon.model.data.entity.database.IColumnDataFormat;
import org.welisdoon.model.data.entity.database.IColumnValue;
import org.welisdoon.web.common.ApplicationContextProvider;

import javax.annotation.PostConstruct;
import java.util.Map;

/**
 * @Classname DefaultColumnDataFormat
 * @Description TODO
 * @Author Septem
 * @Date 15:19
 */
@Component
public class DefaultColumnDataFormat implements IColumnDataFormat {

    @PostConstruct
    void init() {
        ApplicationContextProvider.getApplicationContext().getBeansWithAnnotation(JDBCType.class)
                .entrySet()
                .stream()
                .filter(stringObjectEntry -> IColumnDataFormat.class.isAssignableFrom(stringObjectEntry.getValue().getClass()))
                .map(stringObjectEntry -> {
                    return (IColumnDataFormat) stringObjectEntry.getValue();
                }).forEach(o -> {
            Class<? extends IColumnDataFormat> clazz = (Class<? extends IColumnDataFormat>) ApplicationContextProvider.getRealClass(o.getClass());
            for (JdbcType value : clazz.getAnnotation(JDBCType.class).value()) {
                iColumnDataFormatMap.put(value, clazz);
            }
        });
        for (JdbcType value : JdbcType.values()) {
            if (!iColumnDataFormatMap.containsKey(value))
                iColumnDataFormatMap.put(value, DefaultColumnDataFormat.class);
        }
    }


    @Override
    public void deserialize(IColumnValue column) {
        column.setValue(column.getFormatValue());
    }

    @Override
    public void serialize(IColumnValue column) {
        column.setFormatValue(column.getValue());
    }
}
