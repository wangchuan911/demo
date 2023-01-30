package com.hubidaauto.servmarket.module.order.entity;

import com.hubidaauto.servmarket.common.entity.AbstractBaseCondition;
import io.vertx.codegen.annotations.DataObject;
import io.vertx.core.json.JsonObject;

import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.Map;

/**
 * @Classname OrderVO
 * @Description TODO
 * @Author wang.zhidong
 * @Date 2021/8/13 12:14
 */
public class OrderCondition<T extends OrderVO> extends AbstractBaseCondition<Long, T> {
    int typeId;
    Long flowId, classId, custId, statusId;
    String query, code;
    static final private String[] QUERY = {"pay", "running", "comment", "finish", "dispatching", "dispatched"};

    public int getTypeId() {
        return typeId;
    }

    public void setTypeId(int typeId) {
        this.typeId = typeId;
    }

    public Long getFlowId() {
        return flowId;
    }

    public OrderCondition<T> setFlowId(Long flowId) {
        this.flowId = flowId;
        return this;
    }

    public Long getClassId() {
        return classId;
    }

    public void setClassId(Long classId) {
        this.classId = classId;
    }

    public Long getCustId() {
        return custId;
    }

    public void setCustId(Long custId) {
        this.custId = custId;
    }

    public Long getStatusId() {
        return statusId;
    }

    public void setStatusId(Long statusId) {
        this.statusId = statusId;
    }

    public String getQuery() {
        return query;
    }

    public void setQuery(String query) {
        if (Arrays.stream(QUERY).anyMatch(s -> s.equals(query)))
            this.query = query;
        else
            throw new RuntimeException("not support!");
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }
}
