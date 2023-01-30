package org.welisdoon.flow.module.template.dao;

import org.springframework.stereotype.Repository;
import org.welisdoon.flow.module.template.entity.Link;
import org.welisdoon.flow.module.template.entity.LinkFunction;
import org.welisdoon.flow.module.template.entity.TemplateCondition;
import org.welisdoon.web.common.dao.ITemplateDao;

/**
 * @Classname LinkFunctionDao
 * @Description TODO
 * @Author wang.zhidong
 * @Date 2021/8/21 01:03
 */
@Repository
public interface LinkFunctionDao extends ITemplateDao<Long, LinkFunction, TemplateCondition> {
}
