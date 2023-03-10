package org.welisdoon.model.data.entity.database;

import org.welisdoon.model.data.annotations.Model;
import org.welisdoon.model.data.consts.DataModelType;
import org.welisdoon.model.data.consts.TableType;
import org.welisdoon.model.query.entity.IQueryTarget;

@Model(DataModelType.Table)
public class TableEntity extends AbstractDataEntity implements IQueryTarget, IForeignAssign {
    DataSourceEntity dataSource;
    ColumnEntity[] columns;
    TableType tableType;
    Long[] keys;
    ColumnEntity primary;

    public void setColumns(ColumnEntity[] columns) {
        this.columns = columns;
    }

    public ColumnEntity[] getColumns() {
        return columns;
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

    @Override
    public TableEntity getForeignTable(String value) {
        return this;
    }

    public ColumnEntity getPrimary() {
        return primary;
    }

    public void setPrimary(ColumnEntity primary) {
        this.primary = primary;
    }
}
