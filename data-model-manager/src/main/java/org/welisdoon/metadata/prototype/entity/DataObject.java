package org.welisdoon.metadata.prototype.entity;

import com.alibaba.fastjson.annotation.JSONField;
import com.fasterxml.jackson.annotation.JsonIgnore;
import org.welisdoon.metadata.prototype.consts.AttributeMetaType;
import org.welisdoon.metadata.prototype.define.MetaObject;

import java.util.Arrays;

/**
 * @Classname DataObject
 * @Description TODO
 * @Author Septem
 * @Date 14:49
 */
public class DataObject extends MetaObject {

    @JsonIgnore
    @JSONField(deserialize = false, serialize = false)
    public Field[] getFields() {
        return Arrays.stream(getAttributes()).filter(attribute -> attribute instanceof Field).toArray(Field[]::new);
    }

    @AttributeMetaType.MetaType(AttributeMetaType.Attributes)
    public static class Field extends Attribute<DataObject> {
        DataBaseTable.Column[] columns;

        public DataBaseTable.Column[] getColumns() {
            return columns;
        }

        public void setColumns(DataBaseTable.Column[] columns) {
            this.columns = columns;
        }
    }
}
