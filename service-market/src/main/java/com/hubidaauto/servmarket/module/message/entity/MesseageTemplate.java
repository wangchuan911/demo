package com.hubidaauto.servmarket.module.message.entity;

import com.alibaba.fastjson.JSONObject;

import java.util.Map;

/**
 * @Classname MesseageTemplateVO
 * @Description TODO
 * @Author wang.zhidong
 * @Date 2022/1/17 21:26
 */
public class MesseageTemplate {
    Long id;
    String code, mode;
    MesseageTemplateAttribute[] attributes;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getMode() {
        return mode;
    }

    public void setMode(String mode) {
        this.mode = mode;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public MesseageTemplateAttribute[] getAttributes() {
        return attributes;
    }

    public void setAttributes(MesseageTemplateAttribute[] attributes) {
        this.attributes = attributes;
    }

    public Map<String, Object> toMap(JSONObject params) {
        JSONObject result = new JSONObject();
        for (MesseageTemplateAttribute attribute : attributes) {
            result.put(attribute.name, attribute.toValue(params));
        }
        return result;
    }
}
