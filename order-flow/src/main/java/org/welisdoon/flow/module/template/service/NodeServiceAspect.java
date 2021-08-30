package org.welisdoon.flow.module.template.service;


import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.*;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.welisdoon.flow.module.flow.entity.Flow;
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


    @Pointcut("execution( void *..*.flow(org.welisdoon.flow.module.flow.entity.Flow)) && target(org.welisdoon.flow.module.flow.service.FlowService)")
    public void flow() {

    }

    /*@Pointcut("execution( void *..*.setFlowStatus(org.welisdoon.flow.module.flow.entity.Flow,org.welisdoon.flow.module.flow.entity.FlowStatus)) " +
            "&& target(org.welisdoon.flow.module.template.service.AbstractNodeService)")
    public void flowStatusChange() {

    }
    @Pointcut("execution( void *..*.setStreamStatus(org.welisdoon.flow.module.flow.entity.Stream,org.welisdoon.flow.module.flow.entity.StreamStatus)) " +
            "&& target(org.welisdoon.flow.module.template.service.AbstractNodeService)")
    public void streamStatusChange() {

    }

    @After("flowStatusChange()")
    public void afterFlowStatusChange(JoinPoint point) throws Throwable {
        Stream stream = (Stream) point.getArgs()[0];
        FlowEvent flowEvent = this.getFlowEvent(stream);
        flowEvent.onFlowStatus(stream.getFlow(), stream);
    }
    @After("streamStatusChange()")
    public void afterStreamStatusChange(JoinPoint point) throws Throwable {
        Stream stream = (Stream) point.getArgs()[0];
        FlowEvent flowEvent = this.getFlowEvent(stream);
        flowEvent.onStreamStatus(stream.getFlow(), stream);
    }*/


    @Around("start()") //around 与 下面参数名around对应
    public void aroundStart(ProceedingJoinPoint point) throws Throwable {
        Stream stream = (Stream) point.getArgs()[0];
        FlowEvent flowEvent = this.getFlowEvent(stream);
        try {
            flowEvent.onPreStart(stream);
            point.proceed(); //调用目标方法
            flowEvent.onStarted(stream);
        } catch (Throwable e) {
            flowEvent.onError(stream.getFlow(), stream, e);
            throw e;
        }
    }

    @Around("finish()") //around 与 下面参数名around对应
    public void aroundFinish(ProceedingJoinPoint point) throws Throwable {
        Stream stream = (Stream) point.getArgs()[0];
        FlowEvent flowEvent = this.getFlowEvent(stream);
        try {
            flowEvent.onPreFinish(stream);
            point.proceed(); //调用目标方法
            flowEvent.onFinished(stream);
        } catch (Throwable e) {
            flowEvent.onError(stream.getFlow(), stream, e);
            throw e;
        }
    }

    @Around("flow()") //around 与 下面参数名around对应
    public void aroundFlow(ProceedingJoinPoint point) throws Throwable {
        Flow flow = (Flow) point.getArgs()[0];
        FlowEvent flowEvent = AbstractNodeService.getFunctionObject(flow.getFunctionId());
        try {
            flowEvent.onPreCreate(flow);
            point.proceed(); //调用目标方法
            flowEvent.onCreated(flow, flow.getStart());
        } catch (Throwable e) {
            flowEvent.onError(flow, null, e);
            throw e;
        }
    }


    /*@Before("start()")
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
        flowEvent.onPreFinish(stream);

    }


    @After("finish()")
    public void finished(JoinPoint point) {
        Stream stream = (Stream) point.getArgs()[0];
        FlowEvent flowEvent = this.getFlowEvent(stream);
        flowEvent.onFinished(stream);

    }*/


    FlowEvent getFlowEvent(Stream stream) {
        return AbstractNodeService.getFunctionObject(stream.getFlow().getFunctionId());
    }
}
