package com.hubidaauto.servmarket.module.user.entity;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.annotation.JSONField;
import com.fasterxml.jackson.annotation.JsonIgnore;
import org.springframework.util.StringUtils;
import org.welisdoon.web.common.encrypt.WXBizMsgCrypt;
import org.welisdoon.web.entity.wechat.WeChatUser;

/**
 * @Classname User
 * @Description TODO
 * @Author wang.zhidong
 * @Date 2021/8/11 00:55
 */
public class AppUserVO {
    Long id, defAddrId, inviteUser, defCarId;
    String appId, session, name, phone, unionId;
    Integer type;

    public AppUserVO() {
    }

    public AppUserVO weChatUser(WeChatUser weChatUser){
        this.setSession(weChatUser.getSessionKey());
        this.setUnionId(weChatUser.getUnionid());
        return this;
    }
    public AppUserVO(WeChatUser weChatUser) {
        this.setAppId(weChatUser.getOpenId());
        weChatUser(weChatUser);
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    @JsonIgnore
    @JSONField(deserialize = false, serialize = false)
    public String getAppId() {
        return appId;
    }

    public AppUserVO setAppId(String appId) {
        this.appId = appId;
        return this;
    }

    @JsonIgnore
    @JSONField(deserialize = false, serialize = false)
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

    public Long getInviteUser() {
        return inviteUser;
    }

    public AppUserVO setInviteUser(Long inviteUser) {
        this.inviteUser = inviteUser;
        return this;
    }

    public Long getDefCarId() {
        return defCarId;
    }

    public void setDefCarId(Long defCarId) {
        this.defCarId = defCarId;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    @JsonIgnore
    @JSONField(deserialize = false, serialize = false)
    public String getUnionId() {
        return unionId;
    }

    public void setUnionId(String unionId) {
        this.unionId = unionId;
    }

    /*public AppUserVO userDecrypted(String userEncryptedData, String iv) throws Throwable {
        JSONObject jsonObject = JSONObject.parseObject(WXBizMsgCrypt.wxDecrypt(userEncryptedData, this.getSession(), iv));
        this.setUnionid(jsonObject.getString("unionId"));
        return this;
    }

    public AppUserVO phoneDecrypted(String phoneEncryptedData, String iv) throws Throwable {
        JSONObject jsonObject = JSONObject.parseObject(WXBizMsgCrypt.wxDecrypt(phoneEncryptedData, this.getSession(), iv));
        this.phone = jsonObject.getString("phoneNumber");
        if (StringUtils.isEmpty(this.phone))
            this.phone = jsonObject.getString("purePhoneNumber");
        return this;
    }*/
}
