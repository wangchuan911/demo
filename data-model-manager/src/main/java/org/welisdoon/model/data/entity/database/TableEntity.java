package org.welisdoon.model.data.entity.database;

import org.welisdoon.model.data.annotations.Model;
import org.welisdoon.model.data.consts.DataModelType;
import org.welisdoon.model.data.consts.TableType;
import org.welisdoon.model.query.entity.IQueryTarget;

@Model(DataModelType.Table)
public class TableEntity extends AbstractDataEntity implements IQueryTarget {
    DataSourceEntity dataSource;
    ColumnEntity[] columns;
    TableEntity[] extendTable;
    TableType tableType;
    Long[] keys;

    public void setColumns(ColumnEntity[] columns) {
        this.columns = columns;
    }

    public ColumnEntity[] getColumns() {
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

    public Long[] getKeys() {
        return keys;
    }

    public void setKeys(Long[] keys) {
        this.keys = keys;
    }
}
