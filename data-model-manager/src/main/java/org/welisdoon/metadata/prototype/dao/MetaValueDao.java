package org.welisdoon.metadata.prototype.dao;

import org.springframework.stereotype.Repository;
import org.welisdoon.metadata.prototype.define.MetaValue;
import org.welisdoon.web.common.dao.ITemplateDao;

/**
 * @Classname MetaAttributeDao
 * @Description TODO
 * @Author Septem
 * @Date 14:59
 */
@Repository
public interface MetaValueDao extends ITemplateDao<Long, MetaValue, MetaValue> {
}
