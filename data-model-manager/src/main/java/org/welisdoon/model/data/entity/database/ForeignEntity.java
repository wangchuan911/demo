package org.welisdoon.model.data.entity.database;

import com.alibaba.fastjson.annotation.JSONField;
import com.fasterxml.jackson.annotation.JsonIgnore;
import org.welisdoon.model.data.annotations.Model;
import org.welisdoon.model.data.consts.DataModelType;

/**
 * @Classname ColumnGroup
 * @Description TODO
 * @Author Septem
 * @Date 11:54
 */
@Model(DataModelType.Foreign)
public class ForeignEntity extends AbstractDataEntity {
    long targetId;
    long targetTypeId;
    @JsonIgnore
    @JSONField(deserialize = false, serialize = false)
    ColumnEntity column;
    IForeignTarget target;

    public IForeignTarget getTarget() {
        return target;
    }

    public void setTarget(IForeignTarget target) {
        this.target = target;
    }

    public void setTargetId(long targetId) {
        this.targetId = targetId;
    }

    public long getTargetId() {
        return targetId;
    }

    public long getTargetTypeId() {
        return targetTypeId;
    }

    public void setTargetTypeId(long targetTypeId) {
        this.targetTypeId = targetTypeId;
    }

    public void setColumn(ColumnEntity column) {
        this.column = column;
    }

    public ColumnEntity getColumn() {
        return column;
    }
}
