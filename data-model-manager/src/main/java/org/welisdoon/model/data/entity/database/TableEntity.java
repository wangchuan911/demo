package org.welisdoon.model.data.entity.database;

import org.welisdoon.model.data.annotations.Model;
import org.welisdoon.model.data.consts.FieldLevel;
import org.welisdoon.model.data.consts.DataModelType;
import org.welisdoon.model.data.consts.TableType;

@Model(DataModelType.Table)
public class TableEntity extends AbstractDataEntity {
    DataSourceEntity dataSource;
    FieldEntity[] columns;
    TableEntity[] extendTable;
    TableType tableType;
    FieldLevel level;

    public void setLevel(FieldLevel level) {
        this.level = level;
    }

    public FieldLevel getLevel() {
        return level;
    }

    public void setColumns(FieldEntity[] columns) {
        this.columns = columns;
    }

    public FieldEntity[] getColumns() {
        return columns;
    }

    public void setExtendTable(TableEntity[] extendTable) {
        this.extendTable = extendTable;
    }

    public TableEntity[] getExtendTable() {
        return extendTable;
    }

    public TableType getTableType() {
        return tableType;
    }

    public void setTableType(TableType tableType) {
        this.tableType = tableType;
    }

    public DataSourceEntity getDataSource() {
        return dataSource;
    }

    public void setDataSource(DataSourceEntity dataSource) {
        this.dataSource = dataSource;
    }
}
