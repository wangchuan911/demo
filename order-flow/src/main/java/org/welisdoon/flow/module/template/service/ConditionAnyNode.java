package org.welisdoon.flow.module.template.service;

import org.springframework.stereotype.Service;
import org.welisdoon.flow.module.flow.entity.Stream;
import org.welisdoon.flow.module.flow.entity.StreamStatus;
import org.welisdoon.flow.module.template.annotation.NodeType;
import org.welisdoon.flow.module.template.entity.LinkFunction;
import org.welisdoon.web.common.ApplicationContextProvider;

import java.util.List;
import java.util.function.Predicate;

/**
 * @Classname ConditionNodeService
 * @Description TODO
 * @Author wang.zhidong
 * @Date 2021/8/20 15:46
 */
@NodeType(10004)
@Service
public class ConditionAnyNode extends ParallelAnyNode {



    @Override
    public void start(Stream stream) {
        List<Stream> subStreams = this.getSubStreams(stream);
        this.setStreamStatus(stream, StreamStatus.WAIT);

        LinkFunction function = this.getFunction(stream);
        StreamStatus status;
        boolean hasReady = false;
        Predicate<Stream> predicate = ApplicationContextProvider.getBean(function.targetClass());
        for (Stream subStream : subStreams) {
            status = predicate.test(subStream) ? StreamStatus.READY : StreamStatus.SKIP;
            switch (status) {
                case SKIP:
                    this.setStreamStatus(stream, status);
                    break;
                case READY:
                    hasReady = true;
                    AbstractNode abstractNodeService = getInstance(subStream.getNodeId());
                    abstractNodeService.start(subStream);
                    break;
            }

        }

    }

}
