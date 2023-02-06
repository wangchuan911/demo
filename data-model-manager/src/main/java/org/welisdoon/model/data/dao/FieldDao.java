package org.welisdoon.model.data.dao;

import org.springframework.stereotype.Repository;
import org.welisdoon.model.data.condition.ColumnCondition;
import org.welisdoon.model.data.condition.FieldCondition;
import org.welisdoon.model.data.entity.database.ColumnEntity;
import org.welisdoon.model.data.entity.database.FieldEntity;
import org.welisdoon.web.common.dao.ITemplateDao;

@Repository
public interface FieldDao extends ITemplateDao<Long, FieldEntity, FieldCondition> {
}
