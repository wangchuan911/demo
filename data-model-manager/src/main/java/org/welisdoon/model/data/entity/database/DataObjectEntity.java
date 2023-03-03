package org.welisdoon.model.data.entity.database;


import org.welisdoon.model.data.annotations.Model;
import org.welisdoon.model.data.consts.DataModelType;

@Model(DataModelType.Object)
public class DataObjectEntity extends AbstractDataEntity {

    FieldEntity[] fields;

    public void setFields(FieldEntity[] fields) {
        this.fields = fields;
    }

    public FieldEntity[] getFields() {
        return fields;
    }
}
