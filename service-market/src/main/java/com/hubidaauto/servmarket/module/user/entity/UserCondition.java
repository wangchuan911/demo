package com.hubidaauto.servmarket.module.user.entity;

import com.alibaba.fastjson.JSONObject;
import com.hubidaauto.servmarket.common.entity.AbstractBaseCondition;
import org.welisdoon.web.common.encrypt.WXBizMsgCrypt;

/**
 * @Classname UserCondition
 * @Description TODO
 * @Author wang.zhidong
 * @Date 2021/8/11 09:38
 */
public class UserCondition extends AbstractBaseCondition<Long, AppUserVO> {
    String openId;
    EncryptedData phone, user;
    Long role;
    boolean mask;

    public String getOpenId() {
        return openId;
    }

    public UserCondition setOpenId(String openId) {
        this.openId = openId;
        return this;
    }

    public EncryptedData getPhone() {
        return phone;
    }

    public void setPhone(EncryptedData phone) {
        this.phone = phone;
    }

    public EncryptedData getUser() {
        return user;
    }

    public void setUser(EncryptedData user) {
        this.user = user;
    }

    public Long getRole() {
        return role;
    }

    public void setRole(Long role) {
        this.role = role;
    }

    public boolean isMask() {
        return mask;
    }

    public void setMask(boolean mask) {
        this.mask = mask;
    }
}
