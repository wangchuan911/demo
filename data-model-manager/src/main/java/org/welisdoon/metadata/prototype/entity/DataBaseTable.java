package org.welisdoon.metadata.prototype.entity;

import com.alibaba.fastjson.annotation.JSONField;
import com.fasterxml.jackson.annotation.JsonIgnore;
import org.welisdoon.metadata.prototype.consts.AttributeMetaType;
import org.welisdoon.metadata.prototype.define.MetaObject;

import java.util.Arrays;

/**
 * @Classname DataBaseTable
 * @Description TODO
 * @Author Septem
 * @Date 11:50
 */
public class DataBaseTable extends MetaObject {


    @JsonIgnore
    @JSONField(deserialize = false, serialize = false)
    public Column[] getColumns() {
        return Arrays.stream(getAttributes()).filter(attribute -> attribute instanceof Column).toArray(Column[]::new);
    }

    @AttributeMetaType.MetaType(AttributeMetaType.Column)
    public static class Column extends Attribute<DataBaseTable> {
        boolean primary;

        public boolean isPrimary() {
            return primary;
        }

        public void setPrimary(boolean primary) {
            this.primary = primary;
        }
    }


}
