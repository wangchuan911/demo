package com.hubidaauto.servmarket.module.order.entity;

import com.hubidaauto.servmarket.module.flow.enums.OrderStatus;
import com.hubidaauto.servmarket.module.order.annotation.OrderClass;
import io.vertx.codegen.annotations.DataObject;
import io.vertx.core.json.JsonObject;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.welisdoon.web.vertx.annotation.VertxConfiguration;
import org.welisdoon.web.vertx.annotation.VertxRoutePath;

import java.lang.reflect.InvocationTargetException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;

/**
 * @Classname OrderVO
 * @Description TODO
 * @Author wang.zhidong
 * @Date 2021/8/13 12:14
 */
public class OrderVO {
    Long id, flowId, statusId, custId, classId, regionId;
    Integer price, totalPrice;
    String code, desc;

    public Long getId() {
        return id;
    }

    public OrderVO setId(Long id) {
        this.id = id;
        return this;
    }

    public Integer getPrice() {
        return price;
    }

    public void setPrice(Integer price) {
        this.price = price;
    }

    public Long getFlowId() {
        return flowId;
    }

    public void setFlowId(Long flowId) {
        this.flowId = flowId;
    }

    public Long getStatusId() {
        return statusId;
    }

    public void setStatusId(Long statusId) {
        this.statusId = statusId;
    }

    public void setStatus(OrderStatus status) {
        setStatusId(status.statusId());
    }

    public Long getCustId() {
        return custId;
    }

    public void setCustId(Long custId) {
        this.custId = custId;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public Long getClassId() {
        return classId;
    }

    public void setClassId(Long classId) {
        this.classId = classId;
    }

    public Long getRegionId() {
        return regionId;
    }

    public void setRegionId(Long regionId) {
        this.regionId = regionId;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public Integer getTotalPrice() {
        return totalPrice == null ? this.price : totalPrice;
    }

    public void setTotalPrice(Integer totalPrice) {
        this.totalPrice = totalPrice;
    }

    public String getSymbol() {
        return null;
    }

    public static String generateCode(OrderVO orderVO) {
        return String.format("%6d%s%04d%s%09d", orderVO.getRegionId(), LocalDateTime.now().format(DateTimeFormatter.ofPattern("YYYYMMddHH")), orderVO.getCustId(), orderVO.getSymbol().toUpperCase().substring(0, 3), orderVO.getId());
    }
}
