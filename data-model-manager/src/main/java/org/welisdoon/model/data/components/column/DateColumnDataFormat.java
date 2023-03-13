package org.welisdoon.model.data.components.column;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.util.TypeUtils;
import com.sun.xml.bind.v2.util.TypeCast;
import org.apache.ibatis.type.JdbcType;
import org.springframework.format.datetime.joda.LocalDateTimeParser;
import org.springframework.stereotype.Component;
import org.welisdoon.model.data.entity.database.ColumnEntity;
import org.welisdoon.model.data.entity.database.IColumnDataFormat;
import org.welisdoon.model.data.entity.database.IColumnValue;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.Map;

/**
 * @Classname DataFormat
 * @Description TODO
 * @Author Septem
 * @Date 14:59
 */
@IColumnDataFormat.JDBCType({JdbcType.TIME, JdbcType.TIMESTAMP, JdbcType.DATE})
@Component
public class DateColumnDataFormat implements IColumnDataFormat {


    @Override
    public void deserialize(IColumnValue column) {
        column.setValue(TypeUtils.castToDate(column.getFormatValue()));
    }

    @Override
    public void serialize(IColumnValue column) {
        if (column.getValue() == null) return;
        column.setFormatValue(new SimpleDateFormat(JSON.DEFFAULT_DATE_FORMAT).format(column.getValue()));
    }
}
