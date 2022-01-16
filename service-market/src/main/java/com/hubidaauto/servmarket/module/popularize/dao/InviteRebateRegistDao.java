package com.hubidaauto.servmarket.module.popularize.dao;

import com.baomidou.dynamic.datasource.annotation.DS;
import com.hubidaauto.servmarket.module.popularize.entity.InviteRebateRegistVO;
import org.springframework.stereotype.Repository;
import org.welisdoon.web.common.dao.ITemplateDao;

/**
 * @Classname InviteRebateRegistDao
 * @Description TODO
 * @Author wang.zhidong
 * @Date 2022/1/15 22:15
 */
@Repository
@DS("shop")
public interface InviteRebateRegistDao extends ITemplateDao<Long, InviteRebateRegistVO, InviteRebateRegistVO> {
}
