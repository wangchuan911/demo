package org.welisdoon.flow.module.template.dao;

import org.springframework.stereotype.Repository;
import org.welisdoon.flow.module.template.entity.LinkShow;
import org.welisdoon.web.common.dao.ITemplateDao;

/**
 * @Classname LinkShowDao
 * @Description TODO
 * @Author wang.zhidong
 * @Date 2021/9/14 10:02
 */
@Repository
public interface LinkShowDao extends ITemplateDao<Long, LinkShow, LinkShow> {
}
