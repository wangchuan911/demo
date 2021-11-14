package com.hubidaauto.servmarket.module.log.entity;

import java.sql.Timestamp;

/**
 * @Classname PrePayVO
 * @Description TODO
 * @Author wang.zhidong
 * @Date 2021/11/11 17:33
 */
public class OrderPayLogVO {
    String prepayId;
    String Nonce;
    Long timeStamp;
    Timestamp createDate;
    Long orderId;
    String sign;
    String transactionId;

    public String getPrepayId() {
        return prepayId;
    }

    public OrderPayLogVO setPrepayId(String prepayId) {
        this.prepayId = prepayId;
        return this;
    }

    public String getNonce() {
        return Nonce;
    }

    public OrderPayLogVO setNonce(String nonce) {
        Nonce = nonce;
        return this;
    }

    public Long getTimeStamp() {
        return timeStamp;
    }

    public OrderPayLogVO setTimeStamp(Long timeStamp) {
        this.timeStamp = timeStamp;
        return this;
    }

    public Timestamp getCreateDate() {
        return createDate;
    }

    public OrderPayLogVO setCreateDate(Timestamp createDate) {
        this.createDate = createDate;
        return this;
    }

    public Long getOrderId() {
        return orderId;
    }

    public OrderPayLogVO setOrderId(Long orderId) {
        this.orderId = orderId;
        return this;
    }

    public OrderPayLogVO setSign(String sign) {
        this.sign = sign;
        return this;
    }

    public String getSign() {
        return sign;
    }

    public String getTransactionId() {
        return transactionId;
    }

    public OrderPayLogVO setTransactionId(String transactionId) {
        this.transactionId = transactionId;
        return this;
    }
}
