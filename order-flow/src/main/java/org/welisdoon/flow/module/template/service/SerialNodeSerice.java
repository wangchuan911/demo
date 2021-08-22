package org.welisdoon.flow.module.template.service;

import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;
import org.welisdoon.flow.module.flow.entity.FlowCondition;
import org.welisdoon.flow.module.flow.entity.FlowStatus;
import org.welisdoon.flow.module.flow.entity.Stream;
import org.welisdoon.flow.module.flow.entity.StreamStatus;
import org.welisdoon.flow.module.template.annotation.NodeType;
import org.welisdoon.flow.module.template.entity.Link;

import java.util.Iterator;
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
public class SerialNodeSerice extends AbstractComplexNodeService {

    @Override
    public void start(Stream stream) {
        List<Stream> subStreams = this.getSubStreams(stream);
        AbstractNodeService abstractNodeService;
        this.setStreamStatus(stream, StreamStatus.WAIT);

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
            AbstractNodeService.getInstance(nextStream.getNodeId()).start(nextStream);
            return;
        }
        this.setStreamStatus(stream, StreamStatus.COMPLETE);
        FlowCondition flowCondition = new FlowCondition();
        flowCondition.setFlowId(stream.getFlowId());
        if (stream.getSuperId() == null) {
            this.setFlowStatus(flowDao.get(stream.getFlowId()), FlowStatus.COMPLETE);
            return;
        }
        Stream superStream = this.getSuperStream(stream);
        AbstractNodeService.getInstance(superStream.getNodeId()).finish(superStream);

    }
}
