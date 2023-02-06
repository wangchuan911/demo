package org.welisdoon.model.data.entity.database;


import org.welisdoon.model.data.annotations.Model;
import org.welisdoon.model.data.consts.DataModelType;

@Model(DataModelType.Object)
public class DataObjectEntity extends AbstractDataEntity {
    TableEntity table;

    public TableEntity getTable() {
        return table;
    }

    public void setTable(TableEntity table) {
        this.table = table;
    }
}
