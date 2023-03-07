package org.welisdoon.model.data.condition;

import org.welisdoon.model.data.entity.object.DataObjectEntity;
import org.welisdoon.model.data.entity.object.FieldEntity;

public class FieldCondition{
    FieldEntity entity;

    public void setEntity(FieldEntity entity) {
        this.entity = entity;
    }

    public FieldEntity getEntity() {
        return entity;
    }
}
