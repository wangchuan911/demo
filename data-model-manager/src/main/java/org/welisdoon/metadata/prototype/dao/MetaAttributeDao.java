package org.welisdoon.metadata.prototype.dao;

import org.springframework.stereotype.Repository;
import org.welisdoon.metadata.prototype.define.MetaAttribute;
import org.welisdoon.web.common.dao.ITemplateDao;

/**
 * @Classname MetaAttributeDao
 * @Description TODO
 * @Author Septem
 * @Date 14:59
 */
@Repository
public interface MetaAttributeDao extends ITemplateDao<Long, MetaAttribute, MetaAttribute> {
}
