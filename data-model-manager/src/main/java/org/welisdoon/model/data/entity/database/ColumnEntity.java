package org.welisdoon.model.data.entity.database;

import org.apache.ibatis.type.JdbcType;
import org.welisdoon.model.data.annotations.Model;
import org.welisdoon.model.data.consts.ColumnLevel;
import org.welisdoon.model.data.consts.DataModelType;
import org.welisdoon.model.data.entity.input.AbstractInputEntity;

@Model(DataModelType.Column)
public class ColumnEntity extends AbstractDataEntity {
    Integer[] length;
    TableEntity table;
    JdbcType type = JdbcType.UNDEFINED;
    ColumnLevel level;


    public TableEntity getTable() {
        return table;
    }

    public ColumnEntity setTable(TableEntity table) {
        this.table = table;
        return this;
    }

    public ColumnEntity setType(JdbcType type) {
        this.type = type;
        return this;
    }

    public JdbcType getType() {
        return type;
    }


    public void setLength(Integer[] length) {
        this.length = length;
    }

    public Integer[] getLength() {
        return length == null ? new Integer[]{0} : this.length;
    }




    public ColumnLevel getLevel() {
        return level;
    }

    public void setLevel(ColumnLevel level) {
        this.level = level;
    }
}
