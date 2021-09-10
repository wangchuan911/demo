package com.hubidaauto.servmarket.module.workorder.entity;

import java.util.Arrays;

public class ServiceClassWorkOrderCondition extends WorkOrderCondition {
    static private String[] UPDATES = {"finish", "start", "skip"};
    static private String[] QUERY = {"all","doing"};
    String update;
    String query ;

    public String getUpdate() {
        return update;
    }

    public void setUpdate(String update) {
        if (Arrays.stream(UPDATES).anyMatch(s -> s.equals(update)))
            this.update = update;
        else
            throw new RuntimeException("not support!");
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
}
