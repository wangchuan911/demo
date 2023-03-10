package org.welisdoon.model.data.entity.database;

import com.alibaba.fastjson.annotation.JSONField;
import com.fasterxml.jackson.annotation.JsonIgnore;
import org.apache.ibatis.type.JdbcType;
import org.welisdoon.model.data.annotations.Model;
import org.welisdoon.model.data.consts.DataModelType;
import org.welisdoon.model.data.utils.SqlExecuteUtils;

@Model(DataModelType.Column)
public class ColumnEntity extends AbstractDataEntity implements IForeignAssign {
    String length;
    @JsonIgnore
    @JSONField(serialize = false)
    TableEntity table;
    JdbcType type = JdbcType.UNDEFINED;
    ForeignEntity foreign;

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


    public void setLength(String length) {
        this.length = length;
    }

    public String getLength() {
        return length;
    }


    public ForeignEntity getForeign() {
        return foreign;
    }

    public void setForeign(ForeignEntity foreign) {
        this.foreign = foreign;
    }

    public boolean isForeign() {
        return this.foreign != null;
    }

    @Override
    public TableEntity getForeignTable(String id) {
        String assignId = SqlExecuteUtils.queryForObject(id, String.class, this);
        return getForeign().getForeignColumn().getForeignTable(assignId);
    }
}
