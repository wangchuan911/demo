package org.welisdoon.model.data.condition;

import org.welisdoon.model.data.entity.database.ColumnEntity;
import org.welisdoon.model.data.entity.database.TableEntity;

public class ColumnCondition  {
    ColumnEntity entity;

    public void setEntity(ColumnEntity entity) {
        this.entity = entity;
    }

    public ColumnEntity getEntity() {
        return entity;
    }
}
