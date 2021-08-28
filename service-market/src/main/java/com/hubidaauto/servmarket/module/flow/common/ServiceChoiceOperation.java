package com.hubidaauto.servmarket.module.flow.common;

import com.baomidou.dynamic.datasource.annotation.DS;
import com.hubidaauto.servmarket.module.order.consts.OrderFlowTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.welisdoon.flow.module.flow.entity.Stream;
import org.welisdoon.flow.module.flow.service.FlowService;
import org.welisdoon.flow.module.template.entity.Template;
import org.welisdoon.flow.module.template.service.VirtualNodeService;

import java.util.LinkedList;
import java.util.List;

/**
 * @Classname ServiceChoice
 * @Description TODO
 * @Author wang.zhidong
 * @Date 2021/8/24 00:52
 */
@Service
@Transactional(rollbackFor = Throwable.class)
public class ServiceChoiceOperation implements VirtualNodeService.VirtualNodeInitializer {

    FlowService flowService;

    @Autowired
    public void setFlowService(FlowService flowService) {
        this.flowService = flowService;
    }

    @Override
    @DS("shop")
    public List<Stream> onInstantiated(Stream templateStream) {
        List<Stream> list = new LinkedList();
        for (int i = 0; i < 2; i++) {
            Stream stream = new Stream();
            stream.setFlow(templateStream.getFlow());
            stream.setSuperId(templateStream.getSuperId());
            stream.setFlowId(templateStream.getFlowId());
            stream.setSeq(i + 1);
            stream.setFunctionId(4L);
            stream.setNodeId(1L);
            stream.setName(templateStream.getName());
            list.add(stream);
        }
        return list;
    }

    @Override
    public void onStart(Stream currentStream) {
        throw new RuntimeException("无效节点");
    }
}
