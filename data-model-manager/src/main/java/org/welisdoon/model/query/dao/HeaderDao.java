package org.welisdoon.model.query.dao;

import org.springframework.stereotype.Repository;
import org.welisdoon.model.query.entity.AbstractHeaderEntity;
import org.welisdoon.model.query.entity.condition.QueryHeaderCondition;
import org.welisdoon.web.common.dao.ITemplateDao;

/**
 * @Classname HeaderDao
 * @Description TODO
 * @Author Septem
 * @Date 15:33
 */
@Repository
public interface HeaderDao extends ITemplateDao<Long, AbstractHeaderEntity, QueryHeaderCondition> {
}
