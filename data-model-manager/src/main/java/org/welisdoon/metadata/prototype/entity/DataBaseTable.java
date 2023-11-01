package org.welisdoon.metadata.prototype.entity;

import org.welisdoon.metadata.prototype.consts.AttributeMetaType;
import org.welisdoon.metadata.prototype.consts.ObjectMetaType;
import org.welisdoon.metadata.prototype.define.MetaAttribute;
import org.welisdoon.metadata.prototype.define.MetaObject;

/**
 * @Classname DataBaseTable
 * @Description TODO
 * @Author Septem
 * @Date 11:50
 */
@ObjectMetaType.MetaType(ObjectMetaType.Table)
public class DataBaseTable extends MetaObject {
    Column[] columns;

    public DataBaseTable(long id) {

    }

    public Column[] getColumns() {
        return columns;
    }

    @AttributeMetaType.MetaType(AttributeMetaType.Column)
    public static class Column extends MetaAttribute {
        boolean primary;

        public boolean isPrimary() {
            return primary;
        }

        public void setPrimary(boolean primary) {
            this.primary = primary;
        }
    }


}
