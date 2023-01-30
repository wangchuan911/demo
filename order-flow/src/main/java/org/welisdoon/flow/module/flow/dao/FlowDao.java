package org.welisdoon.flow.module.flow.dao;

import org.springframework.stereotype.Repository;
import org.welisdoon.flow.module.flow.entity.Flow;
import org.welisdoon.flow.module.flow.entity.FlowCondition;
import org.welisdoon.web.common.dao.ITemplateDao;

/**
 * @Classname FlowDao
 * @Description TODO
 * @Author wang.zhidong
 * @Date 2021/8/17 00:03
 */
@Repository
public interface FlowDao extends ITemplateDao<Long, Flow, FlowCondition> {
}
