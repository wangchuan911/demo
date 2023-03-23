package org.welisdoon.model.data.entity.database;

/**
 * @Classname IColumnValue
 * @Description TODO
 * @Author Septem
 * @Date 16:07
 */
public interface IColumnValue {
    Object getValue();

    <T extends IColumnValue> T setValue(Object value);

    Object getFormatValue();

    <T extends IColumnValue> T setFormatValue(Object formatValue);
}
