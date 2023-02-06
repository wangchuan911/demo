package org.welisdoon.model.data.dao;

import org.springframework.stereotype.Repository;
import org.welisdoon.model.data.condition.ColumnCondition;
import org.welisdoon.model.data.condition.DataObjectCondition;
import org.welisdoon.model.data.entity.database.ColumnEntity;
import org.welisdoon.model.data.entity.database.DataObjectEntity;
import org.welisdoon.web.common.dao.ITemplateDao;

@Repository
public interface DataObjectDao extends ITemplateDao<Long, DataObjectEntity, DataObjectCondition> {
}
