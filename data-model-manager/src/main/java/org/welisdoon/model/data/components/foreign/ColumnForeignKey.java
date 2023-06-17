package org.welisdoon.model.data.components.foreign;

import com.alibaba.fastjson.util.TypeUtils;
import org.springframework.stereotype.Component;
import org.welisdoon.model.data.entity.database.ColumnEntity;
import org.welisdoon.model.data.entity.database.ForeignEntity;
import org.welisdoon.model.data.entity.database.IForeignTarget;
import org.welisdoon.model.data.entity.database.TableEntity;
import org.welisdoon.model.data.entity.object.DataObjectEntity;
import org.welisdoon.model.data.service.DataObjectService;
import org.welisdoon.model.data.service.DataTableService;
import org.welisdoon.model.data.utils.TableResultUtils;
import org.welisdoon.web.common.ApplicationContextProvider;

import java.util.Arrays;

/**
 * @Classname ColumnForeignKey
 * @Description TODO
 * @Author Septem
 * @Date 16:05
 */

@Component
public class ColumnForeignKey implements IForeignKeyOperator<ColumnEntity> {

    @Override
    public IForeignTarget getTarget(ForeignEntity entity) {
        return Arrays.stream(entity.getColumn().getTable().getColumns()).filter(columnEntity -> columnEntity.getId().equals(entity.getTargetId())).findFirst().get();
    }

    @Override
    public TableEntity getTable(ColumnEntity columnEntity) {
        IForeignTarget iForeignTarget = columnEntity.getForeign().getTarget();
        if (iForeignTarget instanceof ColumnEntity) {
            if (((ColumnEntity) iForeignTarget).getValue() == null) {
                ((ColumnEntity) iForeignTarget).setValue(TableResultUtils.queryForObject(columnEntity.getTable().getPrimary().getValue(), Object.class, (ColumnEntity) iForeignTarget));
            }
            Object typeId = ((ColumnEntity) iForeignTarget).getValue();

            if ((iForeignTarget = ((ColumnEntity) iForeignTarget).getForeign().getTarget()) instanceof ColumnEntity) {
                return this.getTable(((ColumnEntity) iForeignTarget).setValue(TableResultUtils.queryForObject(typeId, Object.class, (ColumnEntity) iForeignTarget)));
            }
            if (iForeignTarget instanceof DataObjectEntity) {
                iForeignTarget = ApplicationContextProvider.getBean(DataObjectService.class).getDataObject(TypeUtils.castToLong(typeId)).getTable();
            }
            if (iForeignTarget instanceof TableEntity) {
                return (TableEntity) iForeignTarget;
            }
        }

        return ApplicationContextProvider.getBean(iForeignTarget.getClass().getAnnotation(ForeignKey.class).operator()).getTable(iForeignTarget);
    }


}
