package org.welisdoon.model.query.dao;

import org.welisdoon.model.query.entity.AbstractQueryEntity;
import org.welisdoon.model.query.entity.condition.QueryObjectCondition;
import org.welisdoon.web.common.dao.ITemplateDao;

/**
 * @Classname HeaderDao
 * @Description TODO
 * @Author Septem
 * @Date 15:33
 */

public interface QueryDao extends ITemplateDao<Long, AbstractQueryEntity, QueryObjectCondition> {
}
