package org.welisdoon.flow.module.template.service;

import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;
import org.welisdoon.flow.module.flow.entity.Stream;
import org.welisdoon.flow.module.flow.entity.StreamStatus;
import org.welisdoon.flow.module.template.annotation.NodeType;
import org.welisdoon.web.common.ApplicationContextProvider;

import java.util.List;
import java.util.Optional;

/**
 * @Classname ParallelAnyNode
 * @Description TODO
 * @Author wang.zhidong
 * @Date 2021/8/20 15:45
 */
@NodeType(10003)
@Service
@Primary
public class ParallelAnyNode extends AbstractComplexNode {
    @Override
    public void start(Stream stream) {
        List<Stream> subStreams = this.getSubStreams(stream);
        AbstractNode abstractNodeService;
        this.setStreamStatus(stream, StreamStatus.WAIT);

        for (Stream subStream : subStreams) {
            abstractNodeService = getInstance(subStream.getNodeId());
            abstractNodeService.start(subStream);
        }
    }

    @Override
    public void finish(Stream stream) {
        List<Stream> subStreams = this.getSubStreams(stream);
        SubStreamStatusCount countResult = this.countStreamStatus(subStreams);
        if (countResult.COMPLETE == 0) {
            return;
        }
        for (Stream subStream : subStreams) {
            this.skip(subStream);
        }
        this.setStreamStatus(stream, StreamStatus.COMPLETE);
        Stream superStream = this.getSuperStream(stream);
        AbstractNode.getInstance(superStream.getNodeId()).finish(superStream);
    }

    @Override
    public Stream undo(Stream stream, boolean propagation) {
        StreamStatus status = StreamStatus.getInstance(stream.getStatusId());
        switch (status) {
            case COMPLETE:
            case SKIP:
                List<Stream> subStreams = this.getSubStreams(stream);
                if (isVirtual(stream)) {
                    ApplicationContextProvider.getBean(VirtualNode.class).rollback(stream);
                } else {
                    subStreams
                            .stream()
                            .filter(subStream -> subStream.getStatusId() != StreamStatus.FUTURE.statusId())
                            .forEach(subStream -> {
                                getInstance(subStream.getNodeId()).undo(subStream, false);
                            });


                    this.setStreamStatus(stream, StreamStatus.WAIT);
                }
                if (propagation) {
                    Stream superStream = getSuperStream(stream);
                    superStream = getInstance(superStream.getNodeId()).undo(superStream, true);
                    if (superStream != null)
                        return superStream;
                }
                return stream;
            default:
                return null;
        }
    }
}
