package org.welisdoon.model.data.entity.database;

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
    ColumnEntity foreignColumn;
    IForeignAssign assign;

    public ColumnEntity getForeignColumn() {
        return foreignColumn;
    }

    public void setForeignColumn(ColumnEntity foreignColumn) {
        this.foreignColumn = foreignColumn;
    }

    public IForeignAssign getAssign() {
        return assign;
    }

    public void setAssign(IForeignAssign assign) {
        this.assign = assign;
    }
}
