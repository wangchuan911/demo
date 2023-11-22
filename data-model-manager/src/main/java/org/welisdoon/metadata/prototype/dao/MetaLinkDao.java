package org.welisdoon.metadata.prototype.dao;

import org.springframework.stereotype.Repository;
import org.welisdoon.metadata.prototype.condition.MetaLinkCondition;
import org.welisdoon.metadata.prototype.define.MetaLink;
import org.welisdoon.web.common.dao.ITemplateDao;

/**
 * @Classname MetaLinkDao
 * @Description TODO
 * @Author Septem
 * @Date 14:59
 */
@Repository
public interface MetaLinkDao extends ITemplateDao<Long, MetaLink, MetaLinkCondition> {
}
