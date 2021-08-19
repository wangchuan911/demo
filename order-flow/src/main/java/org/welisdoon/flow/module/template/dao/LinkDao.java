package org.welisdoon.flow.module.template.dao;

import org.springframework.stereotype.Repository;
import org.welisdoon.flow.module.template.entity.FlowCondition;
import org.welisdoon.flow.module.template.entity.Link;
import org.welisdoon.flow.module.template.entity.Template;
import org.welisdoon.web.common.dao.ITemplateDao;

/**
 * @Classname FlowDao
 * @Description TODO
 * @Author wang.zhidong
 * @Date 2021/8/17 00:03
 */
@Repository
public interface LinkDao extends ITemplateDao<Long, Link, FlowCondition> {
}
