package org.welisdoon.metadata.prototype.handle.link.construction.sql.content.xml.entity;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.annotation.JSONField;
import com.alibaba.fastjson.parser.DefaultJSONParser;
import com.alibaba.fastjson.parser.JSONToken;
import com.alibaba.fastjson.parser.deserializer.ObjectDeserializer;
import org.welisdoon.metadata.prototype.condition.Page;
import org.welisdoon.metadata.prototype.consts.MetaUtils;
import org.welisdoon.metadata.prototype.define.MetaObject;
import org.welisdoon.metadata.prototype.handle.link.construction.sql.content.XmlTemplateFormatContent;

import java.lang.reflect.Type;
import java.util.List;

/**
 * @Classname QueryTemplateInstance
 * @Description TODO
 * @Author Septem
 * @Date 9:50
 */
public class QueryTemplateInstance {
    String database;
    String nameSpace;

    @JSONField(serialize = false)
    List<QParam> params;
    List<QInput> input;
    List<QColumn> column;
    Page pager;
    Long objectId;

    public SqlParameter getSqlParameter() {
        return new SqlParameter();
    }

    public QueryTemplateInstance() {

    }

    public XmlTemplateFormatContent templateFormatContent() {
        return new XmlTemplateFormatContent(MetaUtils.getInstance().getObject(objectId));
    }

    public QueryTemplateInstance(MetaObject metaObject) {
        database = "demo";
        nameSpace = metaObject.getCode();
        this.objectId = metaObject.getId();
    }

    public Long getObjectId() {
        return objectId;
    }

    public void setObjectId(Long objectId) {
        this.objectId = objectId;
    }

    public String getDatabase() {
        return database;
    }

    public void setDatabase(String database) {
        this.database = database;
    }

    public String getNameSpace() {
        return nameSpace;
    }

    public void setNameSpace(String nameSpace) {
        this.nameSpace = nameSpace;
    }

    public List<QParam> getParams() {
        return params;
    }

    public void setParams(List<QParam> params) {
        this.params = params;
    }

    public List<QInput> getInput() {
        return input;
    }

    public void setInput(List<QInput> input) {
        this.input = input;
    }

    public List<QColumn> getColumn() {
        return column;
    }

    public void setColumn(List<QColumn> column) {
        this.column = column;
    }

    public Page getPager() {
        return pager;
    }

    public void setPager(Page pager) {
        this.pager = pager;
    }

    public static class QColumn {

    }

    public static class QInput {

    }

    public static class QParam {
        @JSONField
        String code;
        @JSONField
        String value;
        @JSONField(deserializeUsing = InputType.Converter.class)
        InputType type;
        @JSONField(deserializeUsing = OperatorType.Converter.class)
        OperatorType operator;

        public String getCode() {
            return code;
        }

        public void setCode(String code) {
            this.code = code;
        }

        public String getValue() {
            return value;
        }

        public void setValue(String value) {
            this.value = value;
        }

        public InputType getType() {
            return type;
        }

        public void setType(InputType type) {
            this.type = type;
        }

        public OperatorType getOperator() {
            return operator;
        }

        public void setOperator(OperatorType operator) {
            this.operator = operator;
        }
    }


    public enum OperatorType {
        EQUAL("equal"),
        START_WITH("startWith"),
        END_WITH("endWith"),
        CONTAIN("contain"),
        GREAT_THAN("greatThan"),
        LESS_THAN("lessThan"),
        RANGE("range"),
        TRUE("true"),
        FALSE("false");
        String value;

        OperatorType(String s) {
            this.value = s;
        }

        public static class Converter implements ObjectDeserializer {
            static final String key = "\"key\":\"";

            @Override
            public OperatorType deserialze(DefaultJSONParser parser, Type type, Object fieldName) {
                JSONObject object = parser.parseObject();
                if (object != null) {
                    String val = object.getString("key");
                    for (OperatorType value : OperatorType.values()) {
                        if (value.value.equals(val)) {
                            return value;
                        }
                    }
                }
                return null;
            }

            @Override
            public int getFastMatchToken() {
                return JSONToken.LBRACE;
            }
        }
    }

    public enum InputType {
        TEXT("text"),
        DECIMAL("decimal"),
        INT("int"),
        TIME("time"),
        BOOLEAN("boolean");

        String value;

        InputType(String s) {
            this.value = s;
        }

        public static class Converter implements ObjectDeserializer {

            @Override
            public InputType deserialze(DefaultJSONParser parser, Type type, Object fieldName) {
                for (InputType value : InputType.values()) {
                    if (value.value.equals(parser.getLexer().stringVal())) {
                        return value;
                    }
                }
                return null;
            }

            @Override
            public int getFastMatchToken() {
                return JSONToken.LITERAL_STRING;
            }
        }
    }


}
