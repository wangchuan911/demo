package com.hubidaauto.servmarket.module.message.entity;

import java.util.Map;

/**
 * @Classname MessagePushVO
 * @Description TODO
 * @Author wang.zhidong
 * @Date 2022/1/7 16:04
 */
public class MessagePushVO {
    String code, templateId;
    Map<String, Object> params;
    Mode mode;

    public String getCode() {
        return code;
    }

    public MessagePushVO setCode(String code) {
        this.code = code;
        return this;
    }

    public String getTemplateId() {
        return templateId;
    }

    public MessagePushVO setTemplateId(String templateId) {
        this.templateId = templateId;
        return this;
    }

    public Map<String, Object> getParams() {
        return params;
    }

    public MessagePushVO setParams(Map<String, Object> params) {
        this.params = params;
        return this;
    }

    public Mode getMode() {
        return mode;
    }

    public MessagePushVO setMode(Mode mode) {
        this.mode = mode;
        return this;
    }

    public enum Mode {
        WechatMiniApp, WechatOfficialAccounts;
    }

    public enum Reciver {
        Staff, Custom;
    }
}
