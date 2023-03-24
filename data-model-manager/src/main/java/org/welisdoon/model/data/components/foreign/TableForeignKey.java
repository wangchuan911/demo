package org.welisdoon.model.data.components.foreign;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.welisdoon.model.data.dao.TableDao;
import org.welisdoon.model.data.entity.database.ForeignEntity;
import org.welisdoon.model.data.entity.database.IForeignTarget;
import org.welisdoon.model.data.entity.database.TableEntity;
import org.welisdoon.model.data.service.DataBaseService;

/**
 * @Classname TableForeignKey
 * @Description TODO
 * @Author Septem
 * @Date 16:06
 */

@IForeignKeyOperator.ForeignKey(1100L)
@Component
public class TableForeignKey implements IForeignKeyOperator {
    DataBaseService baseService;
    ;

    @Autowired
    public void setBaseService(DataBaseService baseService) {
        this.baseService = baseService;
    }

    @Override
    public IForeignTarget getTarget(ForeignEntity entity) {
        return baseService.getTable(entity.getTargetId());
    }

}
