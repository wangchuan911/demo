package com.hubidaauto.servmarket.module.popularize.dao;

import com.baomidou.dynamic.datasource.annotation.DS;
import com.hubidaauto.servmarket.module.popularize.entity.InviteRebateOrderCondition;
import com.hubidaauto.servmarket.module.popularize.entity.InviteRebateOrderVO;
import org.springframework.stereotype.Repository;
import org.welisdoon.web.common.dao.ITemplateDao;

/**
 * @Classname InviteOrderDao
 * @Description TODO
 * @Author wang.zhidong
 * @Date 2021/12/22 20:03
 */
@Repository
@DS("shop")
public interface InviteRebateOrderDao extends ITemplateDao<Long, InviteRebateOrderVO, InviteRebateOrderCondition> {
}
