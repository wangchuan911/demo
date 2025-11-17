package org.welisdoon.metadata.prototype.handle.link.construction.sql.content.xml.node;

import com.alibaba.fastjson.util.TypeUtils;
import org.apache.ibatis.type.JdbcType;
import org.welisdoom.task.xml.annotations.Attr;
import org.welisdoom.task.xml.annotations.Tag;
import org.welisdoom.task.xml.connect.DataBaseConnectPool;
import org.welisdoom.task.xml.handler.OgnlUtils;
import org.welisdoom.task.xml.intf.type.BaseUnit;
import org.welisdoom.task.xml.intf.type.Script;
import org.welisdoon.metadata.prototype.handle.link.construction.sql.content.xml.entity.SqlParameter;

import java.sql.Timestamp;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @Classname Content
 * @Description TODO
 * @Author Septem
 * @Date 15:07
 */

@Tag(value = "content", parentTagTypes = BaseUnit.class, desc = "单纯的文本内容")
@Attr(name = "content", desc = "文本内容")
public class Content extends LikeMyBatisSqlNode implements Script<SqlParameter> {
    public static final String PATTERN_STRING = "\\#\\{(.+?)\\,jdbcType\\=(\\w+)\\}";
    public static final Pattern PATTERN = Pattern.compile(PATTERN_STRING);


    String content;

    public Content(LikeMyBatisSqlNode parent, Map<String, String> attributes) {
        super(parent, attributes);
    }

    public Content setContent(String content) {
        this.content = content;
        return this;
    }

    public String getContent() {
        return content;
    }

    @Override
    public String getScript(SqlParameter request, String s) {
        Matcher matcher = PATTERN.matcher(content);
        JdbcType sqlType = null;
        String name;
        while (matcher.find()) {
            switch (matcher.groupCount()) {
                case 2:
                    sqlType = JdbcType.valueOf(matcher.group(2));
                case 1:
                    name = matcher.group(1);
                    break;
                default:
                    throw new IllegalStateException("格式错误");
            }
            Object value = request.getBus().get(name);
            if (sqlType == null || value == null) {
                request.addParam(value);
            } else {
                switch (sqlType) {
                    case BLOB:
                        value = TypeUtils.castToBytes(value);
                        break;
                    case INTEGER:
                    case SMALLINT:
                    case TINYINT:
                        value = TypeUtils.castToInt(value);
                        break;
                    case NUMERIC:
                    case BIGINT:
                        value = TypeUtils.castToBigDecimal(value);
                        break;
                    case BIT:
                        value = TypeUtils.castToByte(value);
                        break;
                    case BOOLEAN:
                        value = TypeUtils.castToBoolean(value);
                        break;
                    case DOUBLE:
                        value = TypeUtils.castToDouble(value);
                        break;
                    case FLOAT:
                        value = TypeUtils.castToFloat(value);
                        break;
                    case TIMESTAMP:
                        value = TypeUtils.castToTimestamp(value);
                        if (Objects.nonNull(value) && value instanceof Timestamp) {
                            value = ((Timestamp) value).toLocalDateTime();
                        }
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
                request.addParam(value);
            }
        }
        return content.replaceAll(PATTERN_STRING, "?") + s;
    }

    @Override
    public LikeMyBatisSqlNode copyTo(LikeMyBatisSqlNode parent) {
        Content content = (Content) super.copyTo(parent);
        content.content = this.content;
        return content;
    }

    @Override
    public boolean isStaticContent() {
        return true;
    }


}
