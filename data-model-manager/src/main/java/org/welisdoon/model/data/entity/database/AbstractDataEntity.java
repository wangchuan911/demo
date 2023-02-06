package org.welisdoon.model.data.entity.database;

import org.welisdoon.common.data.IData;
import org.welisdoon.model.data.annotations.Model;
import org.welisdoon.model.data.consts.DataModelType;

public abstract class AbstractDataEntity extends IData.DataObject<Long, DataModelType> {
    String code, name;

    public String getCode() {
        return code;
    }

    public <T extends AbstractDataEntity> T setCode(String code) {
        this.code = code;
        return (T) this;
    }

    public String getName() {
        return name;
    }

    public <T extends AbstractDataEntity> T setName(String name) {
        this.name = name;
        return (T) this;
    }

    public DataModelType getDataMarker() {
        return super.getDataMarker() == null ? this.getClass().getAnnotation(Model.class).value() : super.getDataMarker();
    }

    @Override
    public DataObject<Long, DataModelType> setDataMarker(DataModelType dataModelType) {
        return super.setDataMarker(this.getClass().getAnnotation(Model.class).value());
    }
}
