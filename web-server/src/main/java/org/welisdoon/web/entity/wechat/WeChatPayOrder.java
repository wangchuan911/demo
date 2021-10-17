package org.welisdoon.web.entity.wechat;

public class WeChatPayOrder {
    String id;
    String userId;
    String timeStamp;
    String nonce;
    String payClass;

    public String getId() {
        return id;
    }

    public WeChatPayOrder setId(String id) {
        this.id = id;
        return this;
    }

    public String getUserId() {
        return userId;
    }

    public WeChatPayOrder setUserId(String userId) {
        this.userId = userId;
        return this;
    }

    public String getTimeStamp() {
        return timeStamp;
    }

    public WeChatPayOrder setTimeStamp(String timeStamp) {
        this.timeStamp = timeStamp;
        return this;
    }

    public String getNonce() {
        return nonce;
    }

    public WeChatPayOrder setNonce(String nonce) {
        this.nonce = nonce;
        return this;
    }

    public String getPayClass() {
        return payClass;
    }

    public WeChatPayOrder setPayClass(String payClass) {
        this.payClass = payClass;
        return this;
    }
}
