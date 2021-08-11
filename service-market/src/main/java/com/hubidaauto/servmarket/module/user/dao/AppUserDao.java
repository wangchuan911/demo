package com.hubidaauto.servmarket.module.user.dao;

import com.baomidou.dynamic.datasource.annotation.DS;
import com.hubidaauto.servmarket.module.user.entity.AppUserVO;
import com.hubidaauto.servmarket.module.user.entity.UserCondition;
import org.springframework.stereotype.Repository;
import org.welisdoon.web.common.dao.ITemplateDao;

/**
 * @Classname AppUserDap
 * @Description TODO
 * @Author wang.zhidong
 * @Date 2021/8/11 00:59
 */
@Repository
@DS("shop")
public interface AppUserDao extends ITemplateDao<Long, AppUserVO, UserCondition> {

}
