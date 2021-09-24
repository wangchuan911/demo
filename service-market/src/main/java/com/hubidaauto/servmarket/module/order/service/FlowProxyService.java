package com.hubidaauto.servmarket.module.order.service;

import com.baomidou.dynamic.datasource.annotation.DS;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.welisdoon.flow.module.flow.entity.Flow;
import org.welisdoon.flow.module.flow.entity.FlowValue;
import org.welisdoon.flow.module.flow.entity.Stream;
import org.welisdoon.flow.module.flow.service.FlowService;
import org.welisdoon.flow.module.template.entity.Template;

/**
 * @Classname FlowProxyService
 * @Description TODO
 * @Author wang.zhidong
 * @Date 2021/8/28 17:19
 */
@Service
@DS("flow")
@Transactional(rollbackFor = Throwable.class, propagation = Propagation.REQUIRES_NEW)
public class FlowProxyService {
    FlowService flowService;

    @Autowired
    public void setFlowService(FlowService flowService) {
        this.flowService = flowService;
    }

    @DS("flow")
    public Flow start(Long flowId) {
        return this.flowService.start(flowId);
    }

    @DS("flow")
    public void flow(Flow flow) {
        this.flowService.flow(flow);
    }

    @DS("flow")
    public Stream stream(Long streamId) {
        return this.flowService.stream(streamId);
    }

    @DS("flow")
    public Stream getStream(Long streamId) {
        return this.flowService.getStream(streamId);
    }

    @DS("flow")
    public void streamExpandByTemplate(Template template, Stream superStream) {
        this.flowService.streamExpandByTemplate(template, superStream);
    }

    @DS("flow")
    public FlowValue getValue(Long valueId) {
        return this.flowService.getValue(valueId);
    }


    @DS("flow")
    public void dismiss(Flow flow) {
        this.flowService.dismiss(flow);
    }

    /*@DS("flow")
    public boolean setValue(FlowValue flowValue) {
        return this.flowService.setValue(flowValue);
    }

    @DS("flow")
    public boolean addValue(FlowValue flowValue) {
        return this.flowService.addValue(flowValue);
    }*/
}
