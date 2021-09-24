package org.welisdoon.flow.module.template.service;

import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;
import org.welisdoon.flow.module.flow.entity.Stream;
import org.welisdoon.flow.module.flow.entity.StreamStatus;
import org.welisdoon.flow.module.template.annotation.NodeType;
import org.welisdoon.web.common.ApplicationContextProvider;

import java.util.List;

/**
 * @Classname SimpleNodeService
 * @Description TODO
 * @Author wang.zhidong
 * @Date 2021/8/20 15:47
 */
@NodeType(10005)
@Service
@Primary
public class SimpleNode extends AbstractSimpleNode {
    @Override
    public void start(Stream stream) {
        this.setStreamStatus(stream, StreamStatus.READY);
    }

    @Override
    public void finish(Stream stream) {
        this.setStreamStatus(stream, StreamStatus.COMPLETE);
        Stream superStream = this.getSuperStream(stream);
        AbstractNode.getInstance(superStream.getNodeId()).finish(superStream);
    }

    @Override
    public void undo(Stream stream) {
        if (isVirtual(stream)) {
            ApplicationContextProvider.getBean(VirtualNode.class).rollback(stream);
            this.getStreamDao().put(stream);
        }
        this.setStreamStatus(stream, StreamStatus.FUTURE);
    }

    @Override
    public void dismiss(Stream stream) {
        getStreamDao().delete(stream.getId());
    }
}
