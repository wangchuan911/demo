package com.hubidaauto.carservice.wxapp.entity;

import java.sql.Timestamp;

public class CouponVO {
    String id;
    String content;
    Timestamp expireDate;
    int type;
    int off;
    int level;

    public String getId() {
        return id;
    }

    public CouponVO setId(String id) {
        this.id = id;
        return this;
    }

    public String getContent() {
        return content;
    }

    public CouponVO setContent(String content) {
        this.content = content;
        return this;
    }

    public Timestamp getExpireDate() {
        return expireDate;
    }

    public CouponVO setExpireDate(Timestamp expireDate) {
        this.expireDate = expireDate;
        return this;
    }

    public int getType() {
        return type;
    }

    public CouponVO setType(int type) {
        this.type = type;
        return this;
    }

    public int getOff() {
        return off;
    }

    public CouponVO setOff(int off) {
        this.off = off;
        return this;
    }

    public int getLevel() {
        return level;
    }

    public CouponVO setLevel(int level) {
        this.level = level;
        return this;
    }
}
