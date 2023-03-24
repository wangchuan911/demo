package org.welisdoon.model.data.components.foreign;

import com.alibaba.fastjson.util.TypeUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.welisdoon.model.data.dao.ColumnDao;
import org.welisdoon.model.data.dao.DataObjectDao;
import org.welisdoon.model.data.entity.database.ColumnEntity;
import org.welisdoon.model.data.entity.database.ForeignEntity;
import org.welisdoon.model.data.entity.database.IForeignTarget;
import org.welisdoon.model.data.entity.database.TableEntity;
import org.welisdoon.model.data.entity.object.DataObjectEntity;
import org.welisdoon.model.data.service.DataBaseService;
import org.welisdoon.model.data.utils.TableResultUtils;
import org.welisdoon.web.common.ApplicationContextProvider;

import java.util.Arrays;

/**
 * @Classname ColumnForeignKey
 * @Description TODO
 * @Author Septem
 * @Date 16:05
 */

@IForeignKeyOperator.ForeignKey(1102L)
@Component
public class ColumnForeignKey implements IForeignKeyOperator {

    @Override
    public IForeignTarget getTarget(ForeignEntity entity) {
        return Arrays.stream(entity.getColumn().getTable().getColumns()).filter(columnEntity -> columnEntity.getId().equals(entity.getTargetId())).findFirst().get();
    }


}
