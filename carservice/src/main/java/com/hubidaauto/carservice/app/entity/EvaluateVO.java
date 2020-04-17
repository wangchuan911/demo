package com.hubidaauto.carservice.app.entity;

public class EvaluateVO {
    Integer id;
    Integer orderId;
    String userId;
    String remarks;
    Short star;

    public Integer getId() {
        return id;
    }

    public EvaluateVO setId(Integer id) {
        this.id = id;
        return this;
    }

    public Integer getOrderId() {
        return orderId;
    }

    public EvaluateVO setOrderId(Integer orderId) {
        this.orderId = orderId;
        return this;
    }

    public String getUserId() {
        return userId;
    }

    public EvaluateVO setUserId(String userId) {
        this.userId = userId;
        return this;
    }

    public String getRemarks() {
        return remarks;
    }

    public EvaluateVO setRemarks(String remarks) {
        this.remarks = remarks;
        return this;
    }

    public Short getStar() {
        return star;
    }

    public EvaluateVO setStar(Short star) {
        this.star = star;
        return this;
    }

}
