package com.hubidaauto.servmarket.module.workorder.entity;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

import java.util.Arrays;

public class ServiceClassWorkOrderCondition extends WorkOrderCondition {
    static private String[] UPDATES = {"finish", "start", "skip"};
    static private String[] QUERY = {"all", "doing"};
    String update;
    String query;
    JSONObject data;

    public String getUpdate() {
        return update;
    }

    public ServiceClassWorkOrderCondition setUpdate(String update) {
        if (Arrays.stream(UPDATES).anyMatch(s -> s.equals(update)))
            this.update = update;
        else
            throw new RuntimeException("not support!");
        return this;
    }

    public String getQuery() {
        return query;
    }

    public ServiceClassWorkOrderCondition setQuery(String query) {
        if (Arrays.stream(QUERY).anyMatch(s -> s.equals(query)))
            this.query = query;
        else
            throw new RuntimeException("not support!");
        return this;
    }

    public JSONObject getData() {
        return data;
    }

    public ServiceClassWorkOrderCondition setData(JSONObject data) {
        this.data = data;
        return this;
    }
}
