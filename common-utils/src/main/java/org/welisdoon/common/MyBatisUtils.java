package org.welisdoon.common;

import com.alibaba.fastjson.util.TypeUtils;
import org.apache.commons.lang3.StringUtils;

import java.sql.JDBCType;
import java.sql.Timestamp;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @Classname MyBatisUtils
 * @Description TODO
 * @Author Septem
 * @Date 11:39
 */
public interface MyBatisUtils {

    String PATTERN_STRING = "\\#\\{(.+?)\\,jdbcType\\=(\\w+)\\}";
    Pattern PATTERN = Pattern.compile(PATTERN_STRING);


    static void readSqlTemplate(String content, BiConsumer<String, JDBCType> consumer) {
        Matcher matcher = PATTERN.matcher(content);
        while (matcher.find()) {
            JDBCType sqlType = null;
            String name;
            switch (matcher.groupCount()) {
                case 2:
                    sqlType = JDBCType.valueOf(matcher.group(2));
                case 1:
                    name = matcher.group(1);
                    break;
                default:
                    throw new IllegalStateException("格式错误");
            }
            consumer.accept(name, sqlType);
        }
    }

    static Object getValue(JDBCType sqlType, Object value) {
        switch (sqlType) {
            case INTEGER:
            case SMALLINT:
            case TINYINT:
                value = TypeUtils.castToInt(value);
                break;
            case BLOB:
                value = TypeUtils.castToBytes(value);
                break;
            case NUMERIC:
            case BIGINT:
                value = TypeUtils.castToBigDecimal(value);
                break;
            case BOOLEAN:
                value = TypeUtils.castToBoolean(value);
                break;
            case BIT:
                value = TypeUtils.castToByte(value);
                break;
            case DOUBLE:
                value = TypeUtils.castToDouble(value);
                break;
            case TIMESTAMP:
                value = TypeUtils.castToTimestamp(value);
                if (Objects.nonNull(value) && value instanceof Timestamp) {
                    value = ((Timestamp) value).toLocalDateTime();
                }
                break;
            case FLOAT:
                value = TypeUtils.castToFloat(value);
                break;
            case DATE:
                value = TypeUtils.castToDate(value);
                break;
            case CLOB:
            case NCHAR:
            case NCLOB:
            case VARCHAR:
                value = TypeUtils.castToString(value);
            default:
                break;
        }
        return value;
    }
}
