package com.hubidaauto.servmarket.module.popularize.dao;

import com.hubidaauto.servmarket.module.popularize.entity.InviteOrderCondition;
import com.hubidaauto.servmarket.module.popularize.entity.InviteOrderVO;
import org.springframework.stereotype.Repository;
import org.welisdoon.web.common.dao.ITemplateDao;

/**
 * @Classname InviteOrderDao
 * @Description TODO
 * @Author wang.zhidong
 * @Date 2021/12/22 20:03
 */
@Repository
public interface InviteOrderDao extends ITemplateDao<Long, InviteOrderVO, InviteOrderCondition> {
}
