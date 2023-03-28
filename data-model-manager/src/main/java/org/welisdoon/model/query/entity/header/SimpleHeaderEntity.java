package org.welisdoon.model.query.entity.header;

import org.welisdoon.model.data.entity.database.ColumnEntity;
import org.welisdoon.model.query.entity.AbstractHeaderEntity;

/**
 * @Classname FieldEntity
 * @Description TODO
 * @Author Septem
 * @Date 16:30
 */

public class SimpleHeaderEntity extends AbstractHeaderEntity {
    ColumnEntity column;

    public ColumnEntity getColumn() {
        return column;
    }

    public void setColumn(ColumnEntity column) {
        this.column = column;
    }
}
