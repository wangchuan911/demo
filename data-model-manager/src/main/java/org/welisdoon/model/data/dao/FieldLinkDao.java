package org.welisdoon.model.data.dao;

import org.springframework.stereotype.Repository;
import org.welisdoon.model.data.condition.FieldLinkCondition;
import org.welisdoon.model.data.entity.object.FieldLinkEntity;
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
