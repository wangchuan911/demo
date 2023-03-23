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
    IForeignAssign assign;

    public IForeignAssign getAssign() {
        return assign;
    }

    public void setAssign(IForeignAssign assign) {
        this.assign = assign;
    }
}
