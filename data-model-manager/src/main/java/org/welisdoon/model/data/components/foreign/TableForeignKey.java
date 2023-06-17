package org.welisdoon.model.data.components.foreign;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.welisdoon.model.data.entity.database.ForeignEntity;
import org.welisdoon.model.data.entity.database.IForeignTarget;
import org.welisdoon.model.data.entity.database.TableEntity;
import org.welisdoon.model.data.service.DataTableService;

/**
 * @Classname TableForeignKey
 * @Description TODO
 * @Author Septem
 * @Date 16:06
 */

@Component
public class TableForeignKey implements IForeignKeyOperator<TableEntity> {
    DataTableService baseService;
    ;

    @Autowired
    public void setBaseService(DataTableService baseService) {
        this.baseService = baseService;
    }

    @Override
    public IForeignTarget getTarget(ForeignEntity entity) {
        return baseService.getTable(entity.getTargetId());
    }

    @Override
    public TableEntity getTable(TableEntity tableEntity) {
        return tableEntity;
    }

}
