package com.hubidaauto.servmarket.module.order.dao;

import com.hubidaauto.servmarket.module.order.entity.OverTimeOrderCondtion;
import com.hubidaauto.servmarket.module.order.entity.OverTimeOrderVO;
import org.springframework.stereotype.Repository;
import org.welisdoon.web.common.dao.ITemplateDao;

/**
 * @Classname OverTimeOrderDao
 * @Description TODO
 * @Author wang.zhidong
 * @Date 2021/11/29 23:37
 */
@Repository
public interface OverTimeOrderDao extends ITemplateDao<Long, OverTimeOrderVO, OverTimeOrderCondtion> {

}
