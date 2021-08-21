package org.welisdoon.flow.module.template.service;

import org.springframework.stereotype.Service;
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
public class SerialNodeSerice extends AbstractComplexNodeService {

    @Override
    public void start(Stream stream) {
        List<Stream> subStreams = stream.getSubTree();
        AbstractNodeService abstractNodeService;
        stream.setStatusId(StreamStatus.WAIT.statusId());
        this.getStreamDao().put(stream);

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
        stream.setStatusId(StreamStatus.COMPLETE.statusId());
        this.getStreamDao().put(stream);
        Stream superStream = this.getStreamDao().get(stream.getSuperId());
        AbstractNodeService.getInstance(superStream.getNodeId()).finish(superStream);

    }
}
