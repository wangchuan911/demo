package org.welisdoon.flow.module.template.service;

import org.springframework.context.annotation.Primary;
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
@Primary
public class ParallelAllNode extends ParallelAnyNode {


    @Override
    public void finish(Stream stream) {

        List<Stream> subStreams = this.getSubStreams(stream);
        SubStreamStatusCount countResult = this.countStreamStatus(subStreams);
        if (countResult.COMPLETE != subStreams.size()) {
            return;
        }
        this.setStreamStatus(stream,StreamStatus.COMPLETE);
        Stream superStream = this.getStreamDao().get(stream.getSuperId());
        AbstractNodeService.getInstance(superStream.getNodeId()).finish(superStream);
    }
}
