package org.welisdoon.model.data.entity.object;


import org.welisdoon.model.data.annotations.Model;
import org.welisdoon.model.data.consts.DataModelType;
import org.welisdoon.model.data.entity.database.AbstractDataEntity;
import org.welisdoon.model.data.entity.database.IForeignAssign;
import org.welisdoon.model.query.entity.IQueryTarget;

@Model(DataModelType.Object)
public class DataObjectEntity extends AbstractDataEntity implements IQueryTarget, IForeignAssign {

    FieldEntity[] fields;

    public void setFields(FieldEntity[] fields) {
        this.fields = fields;
    }

    public FieldEntity[] getFields() {
        return fields;
    }
}
