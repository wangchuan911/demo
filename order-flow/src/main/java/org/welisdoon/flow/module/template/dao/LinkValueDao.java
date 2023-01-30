package org.welisdoon.flow.module.template.dao;

import org.springframework.stereotype.Repository;
import org.welisdoon.flow.module.template.entity.LinkValue;
import org.welisdoon.web.common.dao.ITemplateDao;

/**
 * @Classname LinkValueDao
 * @Description TODO
 * @Author wang.zhidong
 * @Date 2021/9/14 10:01
 */
@Repository
public interface LinkValueDao extends ITemplateDao<Long, LinkValue, LinkValue> {
}
