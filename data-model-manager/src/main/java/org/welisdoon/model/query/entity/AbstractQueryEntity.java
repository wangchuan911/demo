package org.welisdoon.model.query.entity;

import org.welisdoon.model.query.annotations.Model;
import org.welisdoon.model.query.consts.QueryObjectType;


@Model(QueryObjectType.Query)
public abstract class AbstractQueryEntity<Target extends IQueryTarget> extends AbstractQueryModelEntity {
    AbstractInputEntity[] inputs;
    AbstractHeaderEntity[] fieldEntity;
    Target target;

    public AbstractInputEntity[] getInputs() {
        return inputs;
    }

    public void setInputs(AbstractInputEntity[] inputs) {
        this.inputs = inputs;
    }

    public AbstractHeaderEntity[] getFieldEntity() {
        return fieldEntity;
    }

    public void setFieldEntity(AbstractHeaderEntity[] fieldEntity) {
        this.fieldEntity = fieldEntity;
    }

    public Target getTarget() {
        return target;
    }

    public void setTarget(Target target) {
        this.target = target;
    }
}
