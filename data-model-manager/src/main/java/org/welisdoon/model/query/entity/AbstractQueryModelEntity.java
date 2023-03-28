package org.welisdoon.model.query.entity;

import org.welisdoon.common.data.IData;
import org.welisdoon.model.data.entity.common.AbstractCommonEntity;
import org.welisdoon.model.query.annotations.Model;
import org.welisdoon.model.query.consts.QueryObjectType;

public abstract class AbstractQueryModelEntity extends AbstractCommonEntity<QueryObjectType> {

    public QueryObjectType getDataMarker() {
        return super.getDataMarker() == null ? this.getModel().value() : super.getDataMarker();
    }

    @Override
    public DataObject<Long, QueryObjectType> setDataMarker(QueryObjectType dataModelType) {
        return super.setDataMarker(this.getModel().value());
    }

    final protected Model getModel() {
        Class<?> aClass = this.getClass();
        Model model;
        do {
            model = aClass.getAnnotation(Model.class);
            aClass = aClass.getSuperclass();
        }
        while (model == null && AbstractQueryModelEntity.class.isAssignableFrom(aClass));
        return model;
    }
}
