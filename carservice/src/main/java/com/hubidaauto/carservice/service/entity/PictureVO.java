package com.hubidaauto.carservice.service.entity;

import java.sql.Timestamp;

public class PictureVO {
    private Integer pictrueId;
    private byte[] data;
    private String name;
    private Timestamp createDate;
    private Integer orderId;
    private Integer tacheId;

    public Integer getPictrueId() {
        return pictrueId;
    }

    public PictureVO setPictrueId(Integer pictrueId) {
        this.pictrueId = pictrueId;
        return this;
    }

    public byte[] getData() {
        return data;
    }

    public PictureVO setData(byte[] data) {
        this.data = data;
        return this;
    }

    public String getName() {
        return name;
    }

    public PictureVO setName(String name) {
        this.name = name;
        return this;
    }

    public Timestamp getCreateDate() {
        return createDate;
    }

    public PictureVO setCreateDate(Timestamp createDate) {
        this.createDate = createDate;
        return this;
    }

    public Integer getOrderId() {
        return orderId;
    }

    public PictureVO setOrderId(Integer orderId) {
        this.orderId = orderId;
        return this;
    }

    public Integer getTacheId() {
        return tacheId;
    }

    public PictureVO setTacheId(Integer tacheId) {
        this.tacheId = tacheId;
        return this;
    }
}
