package org.welisdoon.flow.module.template.service;

import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;
import org.welisdoon.flow.module.flow.entity.FlowCondition;
import org.welisdoon.flow.module.flow.entity.FlowStatus;
import org.welisdoon.flow.module.flow.entity.Stream;
import org.welisdoon.flow.module.flow.entity.StreamStatus;
import org.welisdoon.flow.module.template.annotation.NodeType;
import org.welisdoon.web.common.ApplicationContextProvider;

import java.util.List;
import java.util.Optional;

/**
 * @Classname SerialNodeSerice
 * @Description TODO
 * @Author wang.zhidong
 * @Date 2021/8/20 15:43
 */
@NodeType(10001)
@Service
@Primary
public class SerialNode extends AbstractComplexNode {

    @Override
    public void start(Stream stream) {
        List<Stream> subStreams = this.getSubStreams(stream);
        AbstractNode abstractNodeService;
        this.setStreamStatus(stream, StreamStatus.WAIT);
        if (stream.getSuperId() == null)
            this.setFlowStatus(stream.getFlow(), FlowStatus.READY);

        Stream subStream = subStreams.get(0);
        abstractNodeService = getInstance(subStream.getNodeId());
        abstractNodeService.start(subStream);

    }

    @Override
    public void finish(Stream stream) {
        List<Stream> subStreams = this.getSubStreams(stream);
        Optional<Stream> optional = subStreams.stream().filter(subStream -> subStream.getStatusId() == StreamStatus.FUTURE.statusId()).findFirst();
        if (optional.isPresent()) {
            Stream nextStream = optional.get();
            AbstractNode.getInstance(nextStream.getNodeId()).start(nextStream);
            return;
        }
        this.setStreamStatus(stream, StreamStatus.COMPLETE);
        FlowCondition flowCondition = new FlowCondition();
        flowCondition.setFlowId(stream.getFlowId());
        if (stream.getSuperId() == null) {
            this.setFlowStatus(stream.getFlow(), FlowStatus.COMPLETE);
            return;
        }
        Stream superStream = this.getSuperStream(stream);
        AbstractNode.getInstance(superStream.getNodeId()).finish(superStream);

    }

    @Override
    public void undo(Stream stream, boolean propagation) {
        StreamStatus status = StreamStatus.getInstance(stream.getStatusId());
        switch (status) {
            case COMPLETE:
            case SKIP:
                List<Stream> subStreams = this.getSubStreams(stream);
                if (isVirtual(stream)) {
                    ApplicationContextProvider.getBean(VirtualNode.class).rollback(stream);
                } else {
                    boolean flag = false, isCoplete;
                    for (Stream subStream : subStreams) {
                        isCoplete = isComplete(StreamStatus.getInstance(subStream.getStatusId()));
                        if (flag) {
                            if (isCoplete)
                                getInstance(subStream.getNodeId()).undo(subStream, false);
                        } else {
                            flag = !isCoplete;
                        }
                    }
                    this.setStreamStatus(stream, StreamStatus.WAIT);
                }
                if (propagation) {
                    Stream superStream = getSuperStream(stream);
                    getInstance(superStream.getNodeId()).undo(superStream, true);
                }
            default:
        }
    }
}
