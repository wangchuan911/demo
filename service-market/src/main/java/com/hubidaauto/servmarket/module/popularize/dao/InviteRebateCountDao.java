package com.hubidaauto.servmarket.module.popularize.dao;

import com.baomidou.dynamic.datasource.annotation.DS;
import com.hubidaauto.servmarket.module.popularize.entity.InviteRebateCountVO;
import org.springframework.stereotype.Repository;
import org.welisdoon.web.common.dao.ITemplateDao;

/**
 * @Classname InviteRebateCountDao
 * @Description TODO
 * @Author wang.zhidong
 * @Date 2021/12/27 15:55
 */
@Repository
@DS("shop")
public interface InviteRebateCountDao extends ITemplateDao<Long, InviteRebateCountVO, InviteRebateCountVO> {
}
