package org.welisdoon.flow.module.template.service;

import org.reflections.Reflections;
import org.springframework.beans.factory.annotation.Autowired;
import org.welisdoon.flow.module.flow.dao.FlowDao;
import org.welisdoon.flow.module.flow.dao.FlowValueDao;
import org.welisdoon.flow.module.flow.entity.*;
import org.welisdoon.flow.module.flow.intf.FlowEvent;
import org.welisdoon.flow.module.template.annotation.NodeType;
import org.welisdoon.flow.module.template.dao.*;
import org.welisdoon.flow.module.flow.dao.StreamDao;
import org.welisdoon.flow.module.template.entity.Link;
import org.welisdoon.flow.module.template.entity.LinkFunction;
import org.welisdoon.flow.module.template.entity.LinkValue;
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
    static LinkDao linkDao;
    static StreamDao streamDao;
    static NodeDao nodeDao;
    static LinkFunctionDao linkFunctionDao;
    static FlowValueDao flowValueDao;
    static LinkValueDao linkValueDao;
    static FlowDao flowDao;
    static Map<Long, AbstractNode> NODE_MAP;

    @Autowired
    public void initDao(LinkDao linkDao,
                        StreamDao streamDao,
                        NodeDao nodeDao,
                        LinkFunctionDao linkFunctionDao,
                        FlowValueDao flowValueDao,
                        LinkValueDao linkValueDao,
                        FlowDao flowDao) {

        AbstractNode.flowDao = setDao(flowDao, AbstractNode.flowDao);
        AbstractNode.linkDao = setDao(linkDao, AbstractNode.linkDao);
        AbstractNode.streamDao = setDao(streamDao, AbstractNode.streamDao);
        AbstractNode.nodeDao = setDao(nodeDao, AbstractNode.nodeDao);
        AbstractNode.linkFunctionDao = setDao(linkFunctionDao, AbstractNode.linkFunctionDao);
        AbstractNode.flowValueDao = setDao(flowValueDao, AbstractNode.flowValueDao);
        AbstractNode.linkValueDao = setDao(linkValueDao, AbstractNode.linkValueDao);
    }

    static <T> T setDao(T t, T t2) {
        if (t != null && t2 != t) {
            return t;
        }
        return t2;
    }

    public boolean isSimpleNode() {
        return this instanceof AbstractSimpleNode;
    }

    public boolean isComplexNode() {
        return this instanceof AbstractComplexNode;
    }


    public LinkFunctionDao getLinkFunctionDao() {
        return linkFunctionDao;
    }

    public FlowDao getFlowDao() {
        return flowDao;
    }


    public LinkDao getLinkDao() {
        return linkDao;
    }


    public StreamDao getStreamDao() {
        return streamDao;
    }


    public NodeDao getNodeDao() {
        return nodeDao;
    }


    public FlowValueDao getFlowValueDao() {
        return flowValueDao;
    }

    public LinkValueDao getLinkValueDao() {
        return linkValueDao;
    }

    public abstract void start(Stream stream);

    public abstract void finish(Stream stream);

    public boolean skip(Stream stream) {
        if (!this.isComplete(StreamStatus.getInstance(stream.getStatusId()))) {
            this.setStreamStatus(stream, StreamStatus.SKIP);
            return true;
        }
        return false;
    }

    protected abstract void undo(Stream stream, boolean propagation);

    public abstract void undo(Stream stream);

    public void dismiss(Stream stream) {
        getStreamDao().delete(stream.getId());
        if (stream.getValueId() != null)
            flowValueDao.delete(stream.getValueId());
    }

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
//        flowCondition.setShowTree(true);
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
        this.setValue(currentStream, currentLink);

        return List.of(currentStream);
    }

    public void setValue(Stream currentStream, Link currentLink) {
        if (currentLink.getValueId() != null) {
            LinkValue linkValue = this.getLinkValueDao().get(currentLink.getValueId());
            if (linkValue != null) {
                FlowValue flowValue = new FlowValue();
                flowValue.setFlowId(currentStream.getFlowId());
                flowValue.setValue(linkValue.getValue());
                /*this.getFlowValueDao().add(flowValue);
                currentStream.setValueId(flowValue.getId())*/
                currentStream.setValue(flowValue);
            }
        }
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
        this.getFlowDao().update(flowCondition);
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

    boolean isVirtual(Stream stream) {
        return stream.getValue() != null && stream.getValue().jsonValue().getBooleanValue("virtual");
    }

    boolean isComplete(StreamStatus status) {
        switch (status) {
            case SKIP:
            case COMPLETE:
                return true;
            default:
                return false;
        }
    }
    boolean isRunning(StreamStatus status) {
        switch (status) {
            case WAIT:
            case READY:
                return true;
            default:
                return false;
        }
    }
}
