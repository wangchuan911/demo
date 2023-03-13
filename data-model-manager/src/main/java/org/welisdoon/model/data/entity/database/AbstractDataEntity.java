package org.welisdoon.model.data.entity.database;

import org.welisdoon.common.data.IData;
import org.welisdoon.model.data.annotations.Model;
import org.welisdoon.model.data.consts.DataModelType;
import org.welisdoon.model.data.entity.common.AbstractCommonEntity;

public abstract class AbstractDataEntity extends AbstractCommonEntity<DataModelType> {

    public DataModelType getDataMarker() {
        return super.getDataMarker() == null ? this.getClass().getAnnotation(Model.class).value() : super.getDataMarker();
    }

    @Override
    public DataObject<Long, DataModelType> setDataMarker(DataModelType dataModelType) {
        return super.setDataMarker(this.getClass().getAnnotation(Model.class).value());
    }


}
