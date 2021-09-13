package org.welisdoon.flow.module.template.service;

import org.reflections.Reflections;
import org.springframework.beans.factory.annotation.Autowired;
import org.welisdoon.flow.module.flow.dao.FlowDao;
import org.welisdoon.flow.module.flow.entity.*;
import org.welisdoon.flow.module.flow.intf.FlowEvent;
import org.welisdoon.flow.module.template.annotation.NodeType;
import org.welisdoon.flow.module.template.dao.LinkDao;
import org.welisdoon.flow.module.flow.dao.StreamDao;
import org.welisdoon.flow.module.template.dao.LinkFunctionDao;
import org.welisdoon.flow.module.template.dao.NodeDao;
import org.welisdoon.flow.module.template.entity.Link;
import org.welisdoon.flow.module.template.entity.LinkFunction;
import org.welisdoon.flow.module.template.entity.Node;
import org.welisdoon.flow.module.template.entity.struct.Tree;
import org.welisdoon.web.common.ApplicationContextProvider;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @Classname AbstractNodeService
 * @Description TODO
 * @Author wang.zhidong
 * @Date 2021/8/19 23:44
 */
public abstract class AbstractNode {
    LinkDao linkDao;
    StreamDao streamDao;
    static NodeDao nodeDao;
    static LinkFunctionDao linkFunctionDao;
    FlowDao flowDao;
    static Map<Long, AbstractNode> NODE_MAP;


    public boolean isSimpleNode() {
        return this instanceof AbstractSimpleNode;
    }

    public boolean isComplexNode() {
        return this instanceof AbstractComplexNode;
    }

    @Autowired
    public void setLinkFunctionDao(LinkFunctionDao linkFunctionDao) {
        if (AbstractNode.linkFunctionDao != linkFunctionDao)
            AbstractNode.linkFunctionDao = linkFunctionDao;
    }

    public LinkFunctionDao getLinkFunctionDao() {
        return linkFunctionDao;
    }

    public FlowDao getFlowDao() {
        return flowDao;
    }

    @Autowired
    public void setFlowDao(FlowDao flowDao) {
        this.flowDao = flowDao;
    }


    public LinkDao getLinkDao() {
        return linkDao;
    }

    @Autowired
    public void setLinkDao(LinkDao linkDao) {
        this.linkDao = linkDao;
    }

    public StreamDao getStreamDao() {
        return streamDao;
    }

    @Autowired
    public void setStreamDao(StreamDao streamDao) {
        this.streamDao = streamDao;
    }

    public NodeDao getNodeDao() {
        return nodeDao;
    }

    @Autowired
    public void setNodeDao(NodeDao nodeDao) {
        if (AbstractNode.nodeDao != nodeDao)
            AbstractNode.nodeDao = nodeDao;
    }

    public abstract void start(Stream stream);

    public abstract void finish(Stream stream);

    public static AbstractNode getInstance(Long nodeId) {
        return getInstance(AbstractNode.nodeDao.get(nodeId));
    }

    public static AbstractNode getInstance(Node node) {
        if (NODE_MAP == null) {
            NODE_MAP = ApplicationContextProvider.getApplicationContext()
                    .getBeansWithAnnotation(NodeType.class)
                    .entrySet().stream().map(stringObjectEntry ->
                            (AbstractNode) stringObjectEntry.getValue()
                    ).collect(Collectors
                            .toMap(abstractNode ->
                                            ApplicationContextProvider
                                                    .getRealClass(abstractNode.getClass()).getAnnotation(NodeType.class).value()
                                    , abstractNode -> abstractNode));
        }
        return NODE_MAP.get(node.getTypeId());
    }

    public List<Stream> getSubStreams(Stream stream) {
        FlowCondition flowCondition = new FlowCondition();
        flowCondition.setFlowId(stream.getFlowId());
        flowCondition.setSuperStreamId(stream.getId());
        List<Stream> subStreams = getStreamDao().list(flowCondition);
        for (Stream subStream : subStreams) {
            this.sync(stream, subStream);
        }
        return subStreams;
    }

    public List<Stream> createSubStreams(Stream superStream, Link currentLink) {
        Stream currentStream = new Stream();
        /*currentStream.setFlowId(superStream.getFlowId());
        currentStream.setNodeId(currentLink.getNodeId());
        currentStream.setFunctionId(currentLink.getFunctionId());
        currentStream.setSeq(currentLink.getSeq());
        currentStream.setSuperId(superStream.getId());
        currentStream.setName(currentLink.getName());*/
        currentStream.sync(superStream);
        currentStream.sync(currentLink);
        return List.of(currentStream);
    }

    public static class SubStreamStatusCount {
        final public int WAIT, READY, SKIP, FUTURE, COMPLETE;

        SubStreamStatusCount(int WAIT, int READY, int SKIP, int FUTURE, int COMPLETE) {
            this.WAIT = WAIT;
            this.READY = READY;
            this.SKIP = SKIP;
            this.FUTURE = FUTURE;
            this.COMPLETE = COMPLETE;
        }

    }

    protected final void setStreamStatus(Stream stream, StreamStatus status) {
        FlowCondition flowCondition = new FlowCondition();
        flowCondition.setStreamId(stream.getId());
        flowCondition.setStatusId(status.statusId());
        flowCondition.setUpdate("STREAM_STATUS");
        this.getStreamDao().update(flowCondition);
        stream.setStatusId(status.statusId());

        FlowEvent flowEvent = AbstractNode.getFunctionObject(stream.getFlow().getFunctionId());
        flowEvent.onStreamStatus(stream.getFlow(), stream);
    }

    protected final void setFlowStatus(Flow flow, FlowStatus status) {
        FlowCondition flowCondition = new FlowCondition();
        flowCondition.setFlowId(flow.getId());
        flowCondition.setStatusId(status.statusId());
        flowCondition.setUpdate("STREAM_STATUS");
        this.flowDao.update(flowCondition);
        flow.setStatusId(status.statusId());

        FlowEvent flowEvent = AbstractNode.getFunctionObject(flow.getFunctionId());
        flowEvent.onFlowStatus(flow);
        switch (status) {
            case COMPLETE:
            case DESTORY:
                flowEvent.onFinished(flow);
                break;
        }

    }


    LinkFunction getFunction(Tree<?> tree) {
        return linkFunctionDao.get(tree.getFunctionId());
    }

    public static <T> T getFunctionObject(Long functionId) {
        return ApplicationContextProvider.getBean(AbstractNode.linkFunctionDao.get(functionId).targetClass());
    }

    public Stream getSuperStream(Stream stream) {
        Stream superStream = this.getStreamDao().get(stream.getSuperId());
        this.sync(stream, superStream);
        return superStream;
    }

    void sync(Stream source, Stream target) {
        target.setFlow(source.getFlow());
    }

}
