package org.welisdoon.flow.module.flow.dao;

import org.springframework.stereotype.Repository;
import org.welisdoon.flow.module.flow.entity.FlowValue;
import org.welisdoon.web.common.dao.ITemplateDao;

/**
 * @Classname FlowValue
 * @Description TODO
 * @Author wang.zhidong
 * @Date 2021/9/14 10:01
 */
@Repository
public interface FlowValueDao extends ITemplateDao<Long, FlowValue, FlowValue> {
}
