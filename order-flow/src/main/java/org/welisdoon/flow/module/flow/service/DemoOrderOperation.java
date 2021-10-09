package org.welisdoon.flow.module.flow.service;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.welisdoon.flow.module.flow.entity.Flow;
import org.welisdoon.flow.module.flow.entity.Stream;
import org.welisdoon.flow.module.flow.intf.FlowEvent;
import org.welisdoon.flow.module.template.service.VirtualNode;

import java.util.List;

/**
 * @Classname Temp
 * @Description TODO
 * @Author wang.zhidong
 * @Date 2021/8/21 13:33
 */
@Component
@ConditionalOnProperty(prefix = "order-flow.demo", name = "router")
public class DemoOrderOperation implements VirtualNode.VirtualNodeInitializer, FlowEvent {
    @Override
    public List<Stream> onInstantiated(Stream templateStream) {
        templateStream.setNodeId(6L);
        templateStream.setFunctionId(null);
        return List.of(templateStream, templateStream);
    }

    @Override
    public void onStart(Stream templateStream) {

    }

    @Override
    public void onPreCreate(Flow flow) {
        System.out.println(Thread.currentThread().getStackTrace()[1].toString());
    }

    @Override
    public void onCreated(Flow flow, Stream stream) {
        System.out.println(Thread.currentThread().getStackTrace()[1].toString());
    }

    @Override
    public void onFinished(Flow flow) {
        System.out.println(Thread.currentThread().getStackTrace()[1].toString());
    }

    @Override
    public void onPreStart(Stream stream) {
        System.out.println(Thread.currentThread().getStackTrace()[1].toString());
    }

    @Override
    public void onStarted(Stream stream) {
        System.out.println(Thread.currentThread().getStackTrace()[1].toString());
    }

    @Override
    public void onPreFinish(Stream stream) {
        System.out.println(Thread.currentThread().getStackTrace()[1].toString());
    }

    @Override
    public void onFinished(Stream stream) {
        System.out.println(Thread.currentThread().getStackTrace()[1].toString());
    }

    @Override
    public void onError(Flow flow, Stream stream, Throwable e) {
        e.printStackTrace();
    }

    @Override
    public void onFlowStatus(Flow flow) {
        System.out.println(Thread.currentThread().getStackTrace()[1].toString());
    }

    @Override
    public void onStreamStatus(Flow flow, Stream stream) {
        System.out.println(Thread.currentThread().getStackTrace()[1].toString());
    }
}
