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
    void onPreCreate(Flow flow);

    void onCreated(Flow flow, Stream stream);

    void onFinished(Flow flow, Stream stream);

    void onPreStart(Stream stream);

    void onStarted(Stream stream);

    void onPreFinish(Stream stream);

    void onFinished(Stream stream);

    void onError(Flow flow, Stream stream, Throwable e);

    void onFlowStatus(Flow flow, Stream stream);

    void onStreamStatus(Flow flow, Stream stream);

}
