package org.welisdoon.flow.module.template.service;

import org.springframework.stereotype.Service;
import org.welisdoon.flow.module.flow.entity.FlowCondition;
import org.welisdoon.flow.module.flow.entity.Stream;
import org.welisdoon.flow.module.flow.entity.StreamStatus;
import org.welisdoon.flow.module.template.annotation.NodeType;

import java.util.List;

/**
 * @Classname ParallelAllNode
 * @Description TODO
 * @Author wang.zhidong
 * @Date 2021/8/20 15:45
 */
@NodeType(10002)
@Service
public class ParallelAllNode extends AbstractComplexNodeService {
    @Override
    public void start(Stream stream) {
        List<Stream> subStreams = stream.getSubTree();
        AbstractNodeService abstractNodeService;
        stream.setStatusId(StreamStatus.WAIT.statusId());
        this.getStreamDao().put(stream);

        for (Stream subStream : subStreams) {
            abstractNodeService = getInstance(subStream.getNodeId());
            abstractNodeService.start(subStream);
        }
    }

    @Override
    public void finish(Stream stream) {

    }
}
