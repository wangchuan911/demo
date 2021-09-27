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
        if (status == StreamStatus.FUTURE) return;
        boolean isVirtual = isVirtual(stream);
        List<Stream> subStreams = this.getSubStreams(stream);

        if (!propagation) {
            if (isVirtual)
                ApplicationContextProvider.getBean(VirtualNode.class).rollback(stream);
            else {
                for (Stream subStream : subStreams) {
                    getInstance(subStream.getNodeId()).undo(subStream, false);
                }
                this.setStreamStatus(stream, StreamStatus.FUTURE);
            }
        } else if (this.isComplete(StreamStatus.getInstance(stream.getStatusId()))) {
            boolean flag = false, isCoplete;
            for (Stream subStream : subStreams) {
                isCoplete = isComplete(StreamStatus.getInstance(subStream.getStatusId()));
                if (flag) {
                    if (!StreamStatus.FUTURE.equals(stream.getStatusId()))
                        getInstance(subStream.getNodeId()).undo(subStream, false);
                } else {
                    flag = !isCoplete;
                }
            }
            this.setStreamStatus(stream, StreamStatus.WAIT);
            Stream superStream = getSuperStream(stream);
            if (superStream != null)
                getInstance(superStream.getNodeId()).undo(superStream, true);
        }
    }


}
