package org.welisdoon.flow.module.flow.intf;

import org.welisdoon.flow.module.flow.entity.Flow;
import org.welisdoon.flow.module.flow.entity.Stream;

/**
 * @Classname FlowEvent
 * @Description TODO
 * @Author wang.zhidong
 * @Date 2021/8/21 11:47
 */
public interface FlowEvent {
    void onCreateBefore(Flow flow);

    void onCreated(Flow flow, Stream stream);

    void onFinish(Flow flow, Stream stream);

    void onStartBefore(Stream stream);

    void onStartAfter(Stream stream);

    void onFinishBefore(Stream stream);

    void onFinishAfter(Stream stream);

    void onError(Stream stream, Throwable e);
}
