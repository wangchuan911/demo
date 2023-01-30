package org.welisdoon.web.entity.wechat;

import java.util.Random;

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

    public static String generateNonce() {
        char[] chars = new char[32];
        Random random = new Random();
        int offset;
        char choffset;
        for (int i = 0; i < chars.length; i++) {
            offset = random.nextInt(61);
            if (offset >= 36) {
                choffset = 'A';
                offset -= 36;
            } else if (offset >= 10) {
                choffset = 'a';
                offset -= 10;
            } else {
                choffset = '0';
            }
            chars[i] = (char) (choffset + offset);
        }
        return new String(chars);
    }
}
