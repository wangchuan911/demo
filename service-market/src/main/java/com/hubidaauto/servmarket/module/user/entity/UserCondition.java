package com.hubidaauto.servmarket.module.user.entity;

import com.hubidaauto.servmarket.common.entity.AbstractBaseCondition;

/**
 * @Classname UserCondition
 * @Description TODO
 * @Author wang.zhidong
 * @Date 2021/8/11 09:38
 */
public class UserCondition extends AbstractBaseCondition<Long, AppUserVO> {
    String openId;

    public String getOpenId() {
        return openId;
    }

    public UserCondition setOpenId(String openId) {
        this.openId = openId;
        return this;
    }
}
