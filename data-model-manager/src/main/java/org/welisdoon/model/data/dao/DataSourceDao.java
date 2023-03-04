package org.welisdoon.model.data.dao;

import org.springframework.stereotype.Repository;
import org.welisdoon.model.data.condition.DataSourceCondition;
import org.welisdoon.model.data.entity.database.DataSourceEntity;
import org.welisdoon.web.common.dao.ITemplateDao;
@Repository
public interface DataSourceDao extends ITemplateDao<Long, DataSourceEntity, DataSourceCondition> {
}
