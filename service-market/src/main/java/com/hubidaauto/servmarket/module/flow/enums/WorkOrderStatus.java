package com.hubidaauto.servmarket.module.flow.enums;

/**
 * @Classname WorkOrderStatus
 * @Description TODO
 * @Author wang.zhidong
 * @Date 2021/8/30 22:08
 */
public enum WorkOrderStatus {
    READY(11001), COMPLETE(11002), SKIP(11003);
    long id;

    WorkOrderStatus(long id) {
        this.id = id;
    }

    public long statusId() {
        return id;
    }
}
