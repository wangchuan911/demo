package com.hubidaauto.servmarket.module.workorder.entity;

import com.hubidaauto.servmarket.common.entity.AbstractBaseCondition;

/**
 * @Classname WorkOrderCondition
 * @Description TODO
 * @Author wang.zhidong
 * @Date 2021/8/28 18:55
 */
public class WorkOrderCondition<T extends WorkOrderVO> extends AbstractBaseCondition<Long, T> {
    Long streamId;

    public Long getStreamId() {
        return streamId;
    }

    public void setStreamId(Long streamId) {
        this.streamId = streamId;
    }
}
