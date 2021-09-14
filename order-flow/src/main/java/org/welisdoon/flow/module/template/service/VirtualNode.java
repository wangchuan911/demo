package org.welisdoon.flow.module.template.service;

import org.springframework.stereotype.Service;
import org.welisdoon.flow.module.flow.entity.Stream;
import org.welisdoon.flow.module.template.annotation.NodeType;
import org.welisdoon.flow.module.template.entity.Link;
import org.welisdoon.flow.module.template.entity.LinkFunction;
import org.welisdoon.web.common.ApplicationContextProvider;

import java.util.List;

/**
 * @Classname VirtualNodeService
 * @Description TODO
 * @Author wang.zhidong
 * @Date 2021/8/20 16:22
 */

@NodeType(10000)
@Service
public class VirtualNode extends SimpleNode {


    @Override
    public void start(Stream stream) {
        LinkFunction function = this.getFunction(stream);
        VirtualNodeInitializer processor = ApplicationContextProvider.getBean(function.targetClass());
        if (processor == null) {
            throw new RuntimeException("stream not allow link VirtualNode!");
        }
        processor.onStart(stream);
        AbstractNode nodeService = getInstance(stream.getNodeId());
        if (nodeService instanceof VirtualNode) {
            throw new RuntimeException("stream not allow link VirtualNode!");
        }

        this.getStreamDao().put(stream);
        nodeService.start(stream);
    }

    @Override
    public void finish(Stream stream) {
        throw new RuntimeException("stream not allow link VirtualNode!");
    }

    @Override
    public List<Stream> createSubStreams(Stream superStream, Link currentLink) {
        LinkFunction function = this.getFunction(currentLink);
        VirtualNodeInitializer processor = ApplicationContextProvider.getBean(function.targetClass());
        Stream currentStream = new Stream();
        currentStream.sync(superStream);
        currentStream.sync(currentLink);
        this.setValue(currentStream, currentLink);

        /*currentStream.setFlowId(superStream.getFlowId());
        currentStream.setNodeId(currentLink.getNodeId());
        currentStream.setFunctionId(currentLink.getFunctionId());
        currentStream.setSeq(currentLink.getSeq());
        currentStream.setSuperId(superStream.getId());
        currentStream.setStatusId(StreamStatus.FUTURE.statusId());
        currentStream.setName(currentLink.getName());*/
        return processor.onInstantiated(currentStream);
    }

    public interface VirtualNodeInitializer {
        List<Stream> onInstantiated(Stream templateStream);

        void onStart(Stream currentStream);
    }
}
