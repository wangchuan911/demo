package org.welisdoon.model.data.components.foreign;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.welisdoon.model.data.entity.database.ForeignEntity;
import org.welisdoon.model.data.entity.database.IForeignTarget;
import org.welisdoon.model.data.entity.database.TableEntity;
import org.welisdoon.model.data.entity.object.DataObjectEntity;
import org.welisdoon.model.data.service.DataBaseService;

/**
 * @Classname DateObjectForeignKey
 * @Description TODO
 * @Author Septem
 * @Date 16:06
 */

@Component
public class DataObjectForeignKey implements IForeignKeyOperator<DataObjectEntity> {
    DataBaseService baseService;

    @Autowired
    public void setBaseService(DataBaseService baseService) {
        this.baseService = baseService;
    }

    @Override
    public IForeignTarget getTarget(ForeignEntity entity) {
        return baseService.getDataObject(entity.getTargetId());
    }

    @Override
    public TableEntity getTable(DataObjectEntity objectEntity) {
        return objectEntity.getTable();
    }

}