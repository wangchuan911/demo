package org.welisdoon.metadata.prototype.dao;

import org.springframework.stereotype.Repository;
import org.welisdoon.metadata.prototype.condition.MetaObjectCondition;
import org.welisdoon.metadata.prototype.define.MetaObject;
import org.welisdoon.web.common.dao.ITemplateDao;

/**
 * @Classname MetaObjectDao
 * @Description TODO
 * @Author Septem
 * @Date 14:59
 */
@Repository
public interface MetaObjectDao extends ITemplateDao<Long, MetaObject, MetaObjectCondition> {
}
