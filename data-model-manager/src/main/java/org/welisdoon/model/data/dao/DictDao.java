package org.welisdoon.model.data.dao;

import org.springframework.stereotype.Repository;
import org.welisdoon.model.data.entity.common.DictEntity;
import org.welisdoon.web.common.dao.ITemplateDao;

/**
 * @Classname DictDao
 * @Description TODO
 * @Author Septem
 * @Date 9:53
 */
@Repository
public interface DictDao extends ITemplateDao<Long, DictEntity, DictEntity> {
}
