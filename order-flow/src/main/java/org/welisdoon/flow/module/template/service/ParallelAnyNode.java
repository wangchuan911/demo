package org.welisdoon.flow.module.template.service;

import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;
import org.welisdoon.flow.module.flow.entity.Stream;
import org.welisdoon.flow.module.flow.entity.StreamStatus;
import org.welisdoon.flow.module.template.annotation.NodeType;

import java.util.List;

/**
 * @Classname ParallelAnyNode
 * @Description TODO
 * @Author wang.zhidong
 * @Date 2021/8/20 15:45
 */
@NodeType(10003)
@Service
@Primary
public class ParallelAnyNode extends AbstractComplexNodeService {
    @Override
    public void start(Stream stream) {
        List<Stream> subStreams = stream.getSubTree();
        AbstractNodeService abstractNodeService;
        this.setStreamStatus(stream,StreamStatus.WAIT);

        for (Stream subStream : subStreams) {
            abstractNodeService = getInstance(subStream.getNodeId());
            abstractNodeService.start(subStream);
        }
    }

    @Override
    public void finish(Stream stream) {
        List<Stream> subStreams = this.getSubStreams(stream);
        SubStreamStatusCount countResult = this.countStreamStatus(subStreams);
        if (countResult.COMPLETE > 0) {
            return;
        }
        this.skip(subStreams);

        this.setStreamStatus(stream,StreamStatus.COMPLETE);
        Stream superStream = this.getStreamDao().get(stream.getSuperId());
        AbstractNodeService.getInstance(superStream.getNodeId()).finish(superStream);
    }
}
