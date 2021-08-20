package org.welisdoon.flow.module.template.service;

import org.springframework.stereotype.Service;
import org.welisdoon.flow.module.flow.entity.FlowCondition;
import org.welisdoon.flow.module.flow.entity.Stream;
import org.welisdoon.flow.module.flow.entity.StreamStatus;
import org.welisdoon.flow.module.template.annotation.NodeType;
import org.welisdoon.flow.module.template.entity.LinkFunction;

import java.util.List;

/**
 * @Classname ConditionNodeService
 * @Description TODO
 * @Author wang.zhidong
 * @Date 2021/8/20 15:46
 */
@NodeType(10004)
@Service
public class ConditionNodeService extends ParallelAnyNode {
    @Override
    public void start(Stream stream) {
        List<Stream> subStreams = this.getSubStreams(stream);
        stream.setStatusId(StreamStatus.WAIT.statusId());
        this.getStreamDao().put(stream);

        LinkFunction function = null;//= stream.getFunctionId();
        StreamStatus status;
        boolean hasReady = false;
        for (Stream subStream : subStreams) {
            status = function.getPredicate().test(subStream) ? StreamStatus.READY : StreamStatus.SKIP;
            switch (status) {
                case SKIP:
                    subStream.setStatusId(status.statusId());
                    this.getStreamDao().put(subStream);
                case READY:
                    hasReady = true;
                    AbstractNodeService abstractNodeService = getInstance(subStream.getNodeId());
                    abstractNodeService.start(subStream);

            }

        }

    }

    @Override
    public void finish(Stream stream) {

    }
}
