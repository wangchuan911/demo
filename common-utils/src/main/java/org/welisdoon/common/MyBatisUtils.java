package org.welisdoon.common;

import com.alibaba.fastjson.util.TypeUtils;
import org.welisdoon.common.object.wrapper.Prepare;

import java.io.ByteArrayInputStream;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.sql.Date;
import java.sql.*;
import java.text.MessageFormat;
import java.time.LocalDate;
import java.time.chrono.ChronoLocalDate;
import java.time.chrono.ChronoLocalDateTime;
import java.util.*;
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

    String PATTERN_STRING = "\\#\\{\\s*(.+?)\\s*(?:\\,\\s*jdbcType\\s*\\=\\s*(\\w+)\\s*)?\\}";
    Pattern PATTERN = Pattern.compile(PATTERN_STRING);


    static void readSqlTemplate(String content, BiConsumer<String, String> consumer) {
        Matcher matcher = PATTERN.matcher(content);
        while (matcher.find()) {
            String sqlType = "UNKNOWN";
            String name;
            switch (matcher.groupCount()) {
                case 2:
                    sqlType = matcher.group(2);
                case 1:
                    name = matcher.group(1);
                    break;
                default:
                    throw new IllegalStateException("格式错误");
            }
            consumer.accept(name, sqlType);
        }
    }

    static List<Map.Entry<String, String>> readSqlTemplate(String content) {
        List<Map.Entry<String, String>> list = new LinkedList<>();
        readSqlTemplate(content, (s, s2) -> list.add(Map.entry(s, s2)));
        return list;
    }

    static Object getValue(String sqlType, Object value) {
        switch (sqlType) {
            case "INTEGER":
            case "SMALLINT":
            case "TINYINT":
                value = TypeUtils.castToInt(value);
                break;
            case "BLOB":
                value = TypeUtils.castToBytes(value);
                break;
            case "NUMERIC":
            case "BIGINT":
                value = TypeUtils.castToBigDecimal(value);
                break;
            case "BOOLEAN":
                value = TypeUtils.castToBoolean(value);
                break;
            case "BIT":
                value = TypeUtils.castToByte(value);
                break;
            case "DOUBLE":
                value = TypeUtils.castToDouble(value);
                break;
            case "TIMESTAMP":
                value = TypeUtils.castToTimestamp(value);
                if (Objects.nonNull(value) && value instanceof Timestamp) {
                    value = ((Timestamp) value).toLocalDateTime();
                }
                break;
            case "FLOAT":
                value = TypeUtils.castToFloat(value);
                break;
            case "DATE":
                value = TypeUtils.castToDate(value);
                break;
            case "CLOB":
            case "NCHAR":
            case "NCLOB":
            case "VARCHAR":
                value = TypeUtils.castToString(value);
            default:
                break;
        }
        return value;
    }

    static PreparedStatement prepared(Connection connection, String sql, Map<String, Object> map) throws SQLException {
        PreparedStatement preparedStatement = connection.prepareStatement(sql.replaceAll(PATTERN_STRING, "?"));
        List<Map.Entry<String, String>> list = readSqlTemplate(sql);
        ListIterator<Map.Entry<String, String>> iterator = list.listIterator();
        Map.Entry<String, String> entry;
        int i;
        Object value;
        while (iterator.hasNext()) {
            i = iterator.nextIndex();
            entry = iterator.next();
            value = map.get(entry.getKey());
            try {
                setVal(preparedStatement, i, entry.getValue(), value);
            } catch (NoSuchFieldException e) {
                throw new SQLException(MessageFormat.format("不支持的数据类型[{0}]{1},{2}", value == null ? "NULL" : value.getClass().getSimpleName(), entry.getKey(), entry.getValue()), e);
            }
        }
        return preparedStatement;
    }

    static PreparedStatement prepared(Connection connection, Prepare prepare) throws SQLException {
        PreparedStatement preparedStatement = connection.prepareStatement(prepare.sql);
        Object value;
        for (int i = 0; i < prepare.params.size(); i++) {
            value = prepare.params.get(i);
            try {
                setVal(preparedStatement, i + 1, guessSqlType(value), value);
            } catch (NoSuchFieldException e) {
                throw new SQLException(MessageFormat.format("位置{0}不支持的数据类型{1}", i + 1, value == null ? "NULL" : value.getClass().getSimpleName()), e);
            }
        }
        return preparedStatement;
    }

    static String guessSqlType(Object v) {
        if (v == null) {
            return "NULL";
        } else if (v instanceof java.util.Date || v instanceof Calendar || v instanceof ChronoLocalDate || v instanceof ChronoLocalDateTime) {
            return "TIMESTAMP";
        } else if (v instanceof Boolean) {
            return "BOOLEAN";
        } else if (v instanceof Byte) {
            return "BIT";
        } else if (v instanceof Number) {
            return "NUMERIC";
        } else {
            return "VARCHAR";
        }
    }

    static Object setVal(PreparedStatement preparedStatement, int i, String type, Object value) throws SQLException, NoSuchFieldException {
        switch (type) {
            case "BLOB":
                preparedStatement.setBlob(i, new ByteArrayInputStream((byte[]) (value = TypeUtils.castToBytes(value))));
                break;
            case "INTEGER":
            case "SMALLINT":
            case "TINYINT":
                preparedStatement.setInt(i, (Integer) (value = TypeUtils.castToInt(value)));
                break;
            case "NUMERIC":
            case "BIGINT":
                preparedStatement.setBigDecimal(i, (BigDecimal) (value = TypeUtils.castToBigDecimal(value)));
                break;
            case "BIT":
                preparedStatement.setByte(i, (Byte) (value = TypeUtils.castToByte(value)));
                break;
            case "BOOLEAN":
                preparedStatement.setBoolean(i, (Boolean) (value = TypeUtils.castToBoolean(value)));
                break;
            case "DOUBLE":
                preparedStatement.setDouble(i, (Double) (value = TypeUtils.castToDouble(value)));
                break;
            case "FLOAT":
                preparedStatement.setFloat(i, (Float) (value = TypeUtils.castToFloat(value)));
                break;
            case "TIMESTAMP":
                preparedStatement.setTimestamp(i, ((Timestamp) (value = TypeUtils.castToTimestamp(value))));
                break;
            case "DATE":
                preparedStatement.setDate(i, (Date) (value = new Date(TypeUtils.castToDate(value).getTime())));
                break;
            case "CLOB":
                preparedStatement.setClob(i, new InputStreamReader(new ByteArrayInputStream(((String) (value = TypeUtils.castToString(value))).getBytes(StandardCharsets.UTF_8))));
                break;
            case "NCLOB":
                preparedStatement.setNClob(i, new InputStreamReader(new ByteArrayInputStream(((String) (value = TypeUtils.castToString(value))).getBytes(StandardCharsets.UTF_8))));
                break;
            case "NCHAR":
                preparedStatement.setNString(i, (String) (value = TypeUtils.castToString(value)));
                break;
            case "VARCHAR":
                preparedStatement.setString(i, (String) (value = TypeUtils.castToString(value)));
                break;
            default:
                throw new NoSuchFieldException();
        }
        return value;
    }
}
