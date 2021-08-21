package org.welisdoon.flow.module.flow.entity;

import java.util.Arrays;
import java.util.Optional;

/**
 * @Classname FlowStatus
 * @Description TODO
 * @Author wang.zhidong
 * @Date 2021/8/22 00:12
 */
public enum FlowStatus {

    READY(11006), FUTURE(11005), COMPLETE(11007), DESTORY(11008);

    FlowStatus(long statusId) {
        this.statusId = statusId;
    }

    final long statusId;

    public long statusId() {
        return statusId;
    }

    public static FlowStatus getInstance(long statusId) {
        Optional<FlowStatus> flowStatusOptional = Arrays.stream(FlowStatus.values()).filter(flowStatus -> statusId == flowStatus.statusId).findFirst();
        return flowStatusOptional.isPresent() ? flowStatusOptional.get() : null;
    }
}
