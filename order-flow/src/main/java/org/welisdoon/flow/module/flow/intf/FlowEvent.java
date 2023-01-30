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
    default void onPreCreate(Flow flow) {

    }

    default void onCreated(Flow flow, Stream stream) {

    }

    default void onFinished(Flow flow) {

    }

    default void onPreStart(Stream stream) {

    }

    default void onStarted(Stream stream) {

    }

    default void onPreFinish(Stream stream) {

    }

    default void onFinished(Stream stream) {

    }

    default void onError(Flow flow, Stream stream, Throwable e) {

    }

    default void onFlowStatus(Flow flow) {

    }

    default void onStreamStatus(Flow flow, Stream stream) {

    }

}
