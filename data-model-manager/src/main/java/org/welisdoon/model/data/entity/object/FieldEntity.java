package org.welisdoon.model.data.entity.object;

import org.welisdoon.model.data.annotations.Model;
import org.welisdoon.model.data.consts.DataModelType;
import org.welisdoon.model.data.entity.database.AbstractDataEntity;
import org.welisdoon.model.data.entity.database.ColumnEntity;

@Model(DataModelType.Field)
public class FieldEntity extends AbstractDataEntity {
    ColumnEntity[] columns;
    boolean lock = false;
    String style;

    String defaultValue;

    public String getDefaultValue() {
        return defaultValue;
    }

    public void setDefaultValue(String defaultValue) {
        this.defaultValue = defaultValue;
    }

    public boolean isLock() {
        return lock;
    }

    public void setLock(boolean lock) {
        this.lock = lock;
    }

    public ColumnEntity[] getColumns() {
        return columns;
    }

    public void setColumns(ColumnEntity[] columns) {
        this.columns = columns;
    }

    public String getStyle() {
        return style;
    }

    public void setStyle(String style) {
        this.style = style;
    }
}

