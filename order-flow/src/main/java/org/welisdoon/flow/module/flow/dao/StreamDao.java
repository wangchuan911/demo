package org.welisdoon.flow.module.flow.dao;

import org.springframework.stereotype.Repository;
import org.welisdoon.flow.module.flow.entity.FlowCondition;
import org.welisdoon.flow.module.flow.entity.Stream;
import org.welisdoon.web.common.dao.ITemplateDao;

/**
 * @Classname FlowDao
 * @Description TODO
 * @Author wang.zhidong
 * @Date 2021/8/17 00:03
 */
@Repository
public interface StreamDao extends ITemplateDao<Long, Stream, FlowCondition> {
}
