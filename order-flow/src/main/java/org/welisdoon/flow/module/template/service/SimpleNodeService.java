package org.welisdoon.flow.module.template.service;

import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;
import org.welisdoon.flow.module.flow.entity.Stream;
import org.welisdoon.flow.module.flow.entity.StreamStatus;
import org.welisdoon.flow.module.template.annotation.NodeType;

/**
 * @Classname SimpleNodeService
 * @Description TODO
 * @Author wang.zhidong
 * @Date 2021/8/20 15:47
 */
@NodeType(10005)
@Service
@Primary
public class SimpleNodeService extends AbstractSimpleNode {
    @Override
    public void start(Stream stream) {
        setStreamStatus(stream, StreamStatus.READY);
    }

    @Override
    public void finish(Stream stream) {
        this.setStreamStatus(stream, StreamStatus.COMPLETE);
        Stream superStream = this.getSuperStream(stream);
        AbstractNode.getInstance(superStream.getNodeId()).finish(superStream);
    }
}
