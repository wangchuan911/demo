package com.hubidaauto.servmarket.module.user.entity;

import com.hubidaauto.servmarket.module.goods.entity.ItemVO;

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

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

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

    public void setSession(String session) {
        this.session = session;
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
