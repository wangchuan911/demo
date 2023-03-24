package org.welisdoon.model.data.entity.object;


import com.alibaba.fastjson.annotation.JSONField;
import com.alibaba.fastjson.util.TypeUtils;
import com.fasterxml.jackson.annotation.JsonIgnore;
import org.welisdoon.model.data.annotations.Model;
import org.welisdoon.model.data.consts.DataModelType;
import org.welisdoon.model.data.dao.DataObjectDao;
import org.welisdoon.model.data.dao.TableDao;
import org.welisdoon.model.data.entity.database.AbstractDataEntity;
import org.welisdoon.model.data.entity.database.IForeignAssign;
import org.welisdoon.model.data.entity.database.TableEntity;
import org.welisdoon.model.query.entity.IQueryTarget;
import org.welisdoon.web.common.ApplicationContextProvider;

import java.util.Objects;

@Model(DataModelType.Object)
public class DataObjectEntity extends AbstractDataEntity implements IQueryTarget, IForeignAssign {
    @JSONField(deserialize = false, serialize = false)
    @JsonIgnore
    TableEntity table;
    Long tableId;
    TableEntity[] extendTable;
    FieldEntity[] fields;

    public void setFields(FieldEntity[] fields) {
        this.fields = fields;
    }

    public FieldEntity[] getFields() {
        return fields;
    }

    @Override
    public TableEntity foreignTable() {
        return table;
    }

    public TableEntity getTable() {
        return table;
    }

    public void setTable(TableEntity table) {
        this.table = table;
    }

    public TableEntity[] getExtendTable() {
        return extendTable;
    }

    public void setExtendTable(TableEntity[] extendTable) {
        this.extendTable = extendTable;
    }

    public Long getTableId() {
        return tableId;
    }

    public void setTableId(Long tableId) {
        this.tableId = tableId;
    }
}
