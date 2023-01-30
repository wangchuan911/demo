package com.hubidaauto.servmarket.module.user.entity;

import com.alibaba.fastjson.JSONObject;
import org.welisdoon.web.common.encrypt.WXBizMsgCrypt;

/**
 * @Classname EncryptedData
 * @Description TODO
 * @Author wang.zhidong
 * @Date 2022/1/7 09:46
 */
public class EncryptedData {
    String data, iv;

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    public String getIv() {
        return iv;
    }

    public void setIv(String iv) {
        this.iv = iv;
    }

    public String decrypt(String session) throws Throwable {
        return WXBizMsgCrypt.wxDecrypt(this.data, session, iv);
    }
}
