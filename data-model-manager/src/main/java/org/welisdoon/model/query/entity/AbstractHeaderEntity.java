package org.welisdoon.model.query.entity;

import org.welisdoon.model.data.entity.common.DictEntity;
import org.welisdoon.model.data.entity.database.ColumnEntity;
import org.welisdoon.model.query.annotations.Model;
import org.welisdoon.model.query.consts.QueryObjectType;

/**
 * @Classname FieldEntity
 * @Description TODO
 * @Author Septem
 * @Date 16:30
 */

@Model(QueryObjectType.Header)
public abstract class AbstractHeaderEntity extends AbstractQueryModelEntity {
    DictEntity targetType;

    public void setTargetType(DictEntity targetType) {
        this.targetType = targetType;
    }

    public DictEntity getTargetType() {
        return targetType;
    }
}
