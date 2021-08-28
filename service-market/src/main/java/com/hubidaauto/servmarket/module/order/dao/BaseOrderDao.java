package com.hubidaauto.servmarket.module.order.dao;

import com.baomidou.dynamic.datasource.annotation.DS;
import com.hubidaauto.servmarket.module.order.entity.OrderCondition;
import com.hubidaauto.servmarket.module.order.entity.OrderVO;
import com.hubidaauto.servmarket.module.order.entity.ServiceClassOrderVO;
import org.springframework.stereotype.Repository;
import org.welisdoon.web.common.dao.ITemplateDao;

/**
 * @Classname AllClassOrderDao
 * @Description TODO
 * @Author wang.zhidong
 * @Date 2021/8/27 10:12
 */
@Repository
@DS("shop")
public interface BaseOrderDao extends ITemplateDao<Long, OrderVO, OrderCondition<OrderVO>> {

}
