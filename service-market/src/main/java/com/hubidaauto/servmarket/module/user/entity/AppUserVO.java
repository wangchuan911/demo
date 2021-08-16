package com.hubidaauto.servmarket.module.user.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.hubidaauto.servmarket.module.goods.entity.ItemVO;
import org.welisdoon.web.entity.wechat.WeChatUser;

/**
 * @Classname User
 * @Description TODO
 * @Author wang.zhidong
 * @Date 2021/8/11 00:55
 */
public class AppUserVO {
    Long id, defAddrId;
    String appId, session, name;
    Integer type;

    public AppUserVO() {
    }

    public AppUserVO(WeChatUser weChatUser) {
        this.setAppId(weChatUser.getOpenId());
        this.setSession(weChatUser.getSessionKey());
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    @JsonIgnore
    public String getAppId() {
        return appId;
    }

    public AppUserVO setAppId(String appId) {
        this.appId = appId;
        return this;
    }

    public String getSession() {
        return session;
    }

    public AppUserVO setSession(String session) {
        this.session = session;
        return this;
    }

    public String getName() {
        return name;
    }

    public AppUserVO setName(String name) {
        this.name = name;
        return this;
    }

    public Integer getType() {
        return type;
    }

    public void setType(Integer type) {
        this.type = type;
    }

    public Long getDefAddrId() {
        return defAddrId;
    }

    public AppUserVO setDefAddrId(Long defAddrId) {
        this.defAddrId = defAddrId;
        return this;
    }
}
