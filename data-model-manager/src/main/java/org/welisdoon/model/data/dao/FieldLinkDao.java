package org.welisdoon.model.data.dao;

import org.springframework.stereotype.Repository;
import org.welisdoon.model.data.condition.DataSourceCondition;
import org.welisdoon.model.data.condition.FieldLinkCondition;
import org.welisdoon.model.data.entity.database.DataSourceEntity;
import org.welisdoon.model.data.entity.link.FieldLinkEntity;
import org.welisdoon.web.common.dao.ITemplateDao;

/**
 * @Classname FieldLinkDao
 * @Description TODO
 * @Author Septem
 * @Date 18:37
 */
@Repository
public interface FieldLinkDao extends ITemplateDao<Long, FieldLinkEntity, FieldLinkCondition> {
}
