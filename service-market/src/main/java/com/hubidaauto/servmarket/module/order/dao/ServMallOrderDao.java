package com.hubidaauto.servmarket.module.order.dao;

import com.baomidou.dynamic.datasource.annotation.DS;
import com.hubidaauto.servmarket.module.order.entity.OrderCondition;
import com.hubidaauto.servmarket.module.order.entity.OrderVO;
import org.springframework.stereotype.Repository;
import org.welisdoon.web.common.dao.ITemplateDao;

/**
 * @Classname ServMallOrderDao
 * @Description TODO
 * @Author wang.zhidong
 * @Date 2021/8/13 14:58
 */
@Repository
@DS("shop")
public interface ServMallOrderDao extends ITemplateDao<Long, OrderVO, OrderCondition> {

}