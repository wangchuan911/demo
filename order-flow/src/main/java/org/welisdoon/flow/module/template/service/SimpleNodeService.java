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
        setStreamStatus(stream, StreamStatus.READY);
    }

    @Override
    public void finish(Stream stream) {
        stream.setStatusId(StreamStatus.COMPLETE.statusId());
        this.getStreamDao().put(stream);
        Stream superStream = this.getStreamDao().get(stream.getSuperId());
        AbstractNodeService.getInstance(superStream.getNodeId()).finish(superStream);
    }
}
