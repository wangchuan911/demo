package com.hubidaauto.servmarket.module.workorder.entity;

import java.util.Arrays;

public class ServiceClassWorkOrderCondition extends WorkOrderCondition<ServiceClassWorkOrderVO> {
    static private String[] UPDATES = {"finish", "start", "skip"};
    String update;

    public String getUpdate() {
        return update;
    }

    public void setUpdate(String update) {
        if (Arrays.stream(UPDATES).anyMatch(s -> s.equals(update)))
            this.update = update;
        else
            throw new RuntimeException("not support!");
    }
}
