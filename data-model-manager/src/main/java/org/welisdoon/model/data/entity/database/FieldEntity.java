package org.welisdoon.model.data.entity.database;

import org.welisdoon.model.data.annotations.Model;
import org.welisdoon.model.data.consts.DataModelType;

import java.util.Map;

@Model(DataModelType.Field)
public class FieldEntity extends AbstractDataEntity {
    ColumnEntity column;
    ColumnEntity[] columns;
    boolean lock = false;
    Map<String, Object> inputStyle;

    public boolean isLock() {
        return lock;
    }

    public void setLock(boolean lock) {
        this.lock = lock;
    }

    public ColumnEntity getColumn() {
        return column;
    }

    public void setColumn(ColumnEntity column) {
        this.column = column;
    }

    public ColumnEntity[] getColumns() {
        return columns;
    }

    public void setColumns(ColumnEntity[] columns) {
        this.columns = columns;
    }

    public Map<String, Object> getInputStyle() {
        return inputStyle;
    }

    public void setInputStyle(Map<String, Object> inputStyle) {
        this.inputStyle = inputStyle;
    }
}

