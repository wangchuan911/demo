package org.welisdoon.model.query.dao;

import org.springframework.stereotype.Repository;
import org.welisdoon.model.query.entity.AbstractQueryEntity;
import org.welisdoon.model.query.entity.condition.HeaderLinkCondition;
import org.welisdoon.model.query.entity.condition.QueryObjectCondition;
import org.welisdoon.model.query.entity.query.HeaderLinkEntity;
import org.welisdoon.web.common.dao.ITemplateDao;

/**
 * @Classname HeaderDao
 * @Description TODO
 * @Author Septem
 * @Date 15:33
 */
@Repository
public interface HeaderLinkDao extends ITemplateDao<Long, HeaderLinkEntity, HeaderLinkCondition> {
}
