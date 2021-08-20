package org.welisdoon.flow.module.template.service;

import org.springframework.stereotype.Service;
import org.welisdoon.flow.module.flow.entity.FlowCondition;
import org.welisdoon.flow.module.flow.entity.Stream;
import org.welisdoon.flow.module.flow.entity.StreamStatus;
import org.welisdoon.flow.module.template.annotation.NodeType;
import org.welisdoon.flow.module.template.entity.TemplateCondition;

import java.util.List;

/**
 * @Classname SimpleNodeService
 * @Description TODO
 * @Author wang.zhidong
 * @Date 2021/8/20 15:47
 */
@NodeType(10005)
@Service
public class SimpleNodeService extends AbstractSimpleNodeSerivce {
    @Override
    public void start(Stream stream) {
        FlowCondition condition = new FlowCondition();
        condition.setStreamId(stream.getId());
        condition.setStatusId(StreamStatus.READY.statusId());
        this.getStreamDao().update(condition);
    }

    @Override
    public void finish(Stream stream) {

    }
}
