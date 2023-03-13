package org.welisdoon.model.data.entity.database;

/**
 * @Classname IColumnValue
 * @Description TODO
 * @Author Septem
 * @Date 16:07
 */
public interface IColumnValue {
    Object getValue();

    void setValue(Object value);

    Object getFormatValue();

    void setFormatValue(Object formatValue);
}
