package org.welisdoon.model.data.dao;

import org.springframework.stereotype.Repository;
import org.welisdoon.model.data.entity.express.ExpressEntity;
import org.welisdoon.web.common.dao.ITemplateDao;

/**
 * @Classname ExpressDao
 * @Description TODO
 * @Author Septem
 * @Date 14:59
 */
@Repository
public interface ExpressDao extends ITemplateDao<Long, ExpressEntity, ExpressEntity> {
}
