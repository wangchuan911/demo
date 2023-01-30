package com.hubidaauto.servmarket.module.message.entity;

import com.alibaba.fastjson.JSONObject;
import com.hubidaauto.servmarket.module.message.model.MessageEvent;

import java.util.Map;

/**
 * @Classname MesseageTemplateVO
 * @Description TODO
 * @Author wang.zhidong
 * @Date 2022/1/17 21:26
 */
public class MesseageTemplateAttribute {
    Long msgId;
    String name;
    String value;

    public Long getMsgId() {
        return msgId;
    }

    public void setMsgId(Long msgId) {
        this.msgId = msgId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String toValue(JSONObject params) {
        StringBuilder _value = new StringBuilder(this.value);
        String valueKey;
        MessageEvent.MagicKey magicKey;
        int head, foot;
        while ((head = _value.indexOf("{{")) >= 0 && head < (foot = _value.indexOf("}}"))) {
            magicKey = MessageEvent.getMagic(valueKey = _value.substring(head + 2, foot));
            valueKey = magicKey.getValue(valueKey);
            _value.replace(head, foot + 2, magicKey.format(MessageEvent.toLongKeyMap(params, valueKey)));
        }
        return _value.toString();
    }
}
