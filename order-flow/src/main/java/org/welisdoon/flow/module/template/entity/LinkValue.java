package org.welisdoon.flow.module.template.entity;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

/**
 * @Classname LinkValue
 * @Description TODO
 * @Author wang.zhidong
 * @Date 2021/9/14 10:03
 */
public class LinkValue {
    Long id;
    String value;
    JSONObject jsonValue;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public JSONObject jsonValue() {
        return this.value == null ? (this.jsonValue = new JSONObject()) : (this.jsonValue = JSONObject.parseObject(this.value));
    }

    public LinkValue saveJsonValue() {
        if (jsonValue != null)
            this.value = jsonValue.toJSONString();
        return this;
    }
}
