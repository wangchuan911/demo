package org.welisdoon.flow.module.flow.entity;

import java.util.Arrays;
import java.util.Optional;

/**
 * @Classname StreamStatus
 * @Description TODO
 * @Author wang.zhidong
 * @Date 2021/8/20 15:04
 */
public enum StreamStatus {

    READY(11001), FUTURE(11000), COMPLETE(11002), WAIT(11003), SKIP(11004);

    StreamStatus(long statusId) {
        this.statusId = statusId;
    }

    final long statusId;

    public long statusId() {
        return statusId;
    }

    public static StreamStatus getInstance(long statusId) {
        Optional<StreamStatus> optionalStreamStatus = Arrays.stream(StreamStatus.values()).filter(streamStatus -> statusId == streamStatus.statusId).findFirst();
        return optionalStreamStatus.isPresent() ? optionalStreamStatus.get() : null;
    }

}
