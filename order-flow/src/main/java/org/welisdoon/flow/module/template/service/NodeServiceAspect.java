package org.welisdoon.flow.module.template.service;


import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.*;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.welisdoon.flow.module.flow.entity.Stream;
import org.welisdoon.flow.module.flow.intf.FlowEvent;
import org.welisdoon.flow.module.template.service.AbstractNodeService;

/**
 * @Classname NodeAspect
 * @Description TODO
 * @Author wang.zhidong
 * @Date 2021/8/22 13:40
 */
@Configuration
@Aspect
@Transactional(rollbackFor = Throwable.class)
public class NodeServiceAspect {


    /*@Pointcut("execution( void *..*.start(org.welisdoon.flow.module.flow.entity.Stream)) && target(org.welisdoon.flow.module.template.service.AbstractNodeService)")
    public void test() {

    }

    @Before("test()")
    public void test1() {
        System.out.println("11111111111111");
    }*/

    @Pointcut("execution( void *..*.start(org.welisdoon.flow.module.flow.entity.Stream)) && target(org.welisdoon.flow.module.template.service.AbstractNodeService)")
    public void start() {

    }

    @Pointcut("execution( void *..*.finish(org.welisdoon.flow.module.flow.entity.Stream)) && target(org.welisdoon.flow.module.template.service.AbstractNodeService)")
    public void finish() {

    }

    @Before("start()")
    public void preStart(JoinPoint point) {
        Stream stream = (Stream) point.getArgs()[0];
        FlowEvent flowEvent = this.getFlowEvent(stream);
        flowEvent.onPreStart(stream);

    }

    @After("start()")
    public void started(JoinPoint point) {
        Stream stream = (Stream) point.getArgs()[0];
        FlowEvent flowEvent = this.getFlowEvent(stream);
        flowEvent.onStarted(stream);

    }

    @Before("finish()")
    public void preFinish(JoinPoint point) {
        Stream stream = (Stream) point.getArgs()[0];
        FlowEvent flowEvent = this.getFlowEvent(stream);
        flowEvent.onPreStart(stream);

    }


    @After("finish()")
    public void finished(JoinPoint point) {
        Stream stream = (Stream) point.getArgs()[0];
        FlowEvent flowEvent = this.getFlowEvent(stream);
        flowEvent.onFinished(stream);

    }


    FlowEvent getFlowEvent(Stream stream) {
        return AbstractNodeService.getFunctionObject(stream.getFlow().getFunctionId());
    }
}
