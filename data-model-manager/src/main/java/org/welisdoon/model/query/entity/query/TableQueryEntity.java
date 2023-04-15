package org.welisdoon.model.query.entity.query;

import org.welisdoon.model.data.entity.database.TableEntity;
import org.welisdoon.model.data.entity.object.DataObjectEntity;
import org.welisdoon.model.query.entity.AbstractQueryEntity;

/**
 * @Classname QueryEntity
 * @Description TODO
 * @Author Septem
 * @Date 16:29
 */
public class TableQueryEntity extends AbstractQueryEntity<TableEntity> {
    Long tableId;

    public void setTableId(Long tableId) {
        this.tableId = tableId;
    }

    public Long getTableId() {
        return tableId;
    }
}
