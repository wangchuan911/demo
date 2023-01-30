package org.welisdoon.flow.module.flow.demo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.welisdoon.flow.module.flow.entity.Stream;
import org.welisdoon.flow.module.flow.service.FlowService;
import org.welisdoon.flow.module.template.entity.Template;
import org.welisdoon.flow.module.template.service.VirtualNode;

import java.util.List;

/**
 * @Classname DemoVirualInstantiated
 * @Description TODO
 * @Author wang.zhidong
 * @Date 2022/2/18 23:01
 */

@Component
@ConditionalOnProperty(prefix = "order-flow.demo", name = "router")
public class DemoVirtualStart implements VirtualNode.OnStart {
    FlowService flowService;

    @Autowired
    public void setFlowService(FlowService flowService) {
        this.flowService = flowService;
    }

    @Override
    public void apply(Stream currentStream) {
        Template template = new Template();
        template.setId(101L);
        flowService.streamExpandByTemplate(template, currentStream);
    }
}
