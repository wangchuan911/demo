package org.welisdoon.flow.module.template.service;

import org.reflections.ReflectionUtils;
import org.reflections.Reflections;
import org.springframework.beans.factory.annotation.Autowired;
import org.welisdoon.flow.module.flow.dao.FlowDao;
import org.welisdoon.flow.module.flow.entity.*;
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
import java.util.Set;

/**
 * @Classname AbstractNodeService
 * @Description TODO
 * @Author wang.zhidong
 * @Date 2021/8/19 23:44
 */
public abstract class AbstractNodeService {
    static Reflections reflections;
    LinkDao linkDao;
    StreamDao streamDao;
    static NodeDao nodeDao;
    static LinkFunctionDao linkFunctionDao;
    FlowDao flowDao;

    public static Reflections getReflections() {
        return reflections;
    }

    public boolean isSimpleNode() {
        return this instanceof AbstractSimpleNodeSerivce;
    }

    public boolean isComplexNode() {
        return this instanceof AbstractComplexNodeService;
    }

    @Autowired
    public void setLinkFunctionDao(LinkFunctionDao linkFunctionDao) {
        if (AbstractNodeService.linkFunctionDao == null) {
            AbstractNodeService.linkFunctionDao = linkFunctionDao;
        }
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

    @Autowired
    public void setReflections(Reflections reflections) {
        if (AbstractNodeService.reflections == null) {
            AbstractNodeService.reflections = reflections;
        }
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
        if (AbstractNodeService.nodeDao == null) {
            AbstractNodeService.nodeDao = nodeDao;
        }
    }

    public abstract void start(Stream stream);

    public abstract void finish(Stream stream);

    public static AbstractNodeService getInstance(Long nodeId) {
        return getInstance(nodeDao.get(nodeId));
    }

    public static AbstractNodeService getInstance(Node node) {
        return (AbstractNodeService) ApplicationContextProvider.getApplicationContext().getBeansWithAnnotation(NodeType.class).entrySet().stream().filter(stringObjectEntry -> {
            Class<?> aClass = ApplicationContextProvider
                    .getRealClass(stringObjectEntry.getValue().getClass());
            return AbstractNodeService.class.isAssignableFrom(aClass)
                    && aClass.getAnnotation(NodeType.class).value() == node.getTypeId();

        }).findFirst().get().getValue();
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

    public void setStreamStatus(Stream stream, StreamStatus status) {
        FlowCondition flowCondition = new FlowCondition();
        flowCondition.setStreamId(stream.getId());
        flowCondition.setStatusId(status.statusId());
        flowCondition.setUpdate("STREAM_STATUS");
        this.getStreamDao().update(flowCondition);
        stream.setStatusId(status.statusId());
    }

    public void setFlowStatus(Flow flow, FlowStatus status) {
        FlowCondition flowCondition = new FlowCondition();
        flowCondition.setFlowId(flow.getId());
        flowCondition.setStatusId(status.statusId());
        flowCondition.setUpdate("STREAM_STATUS");
        this.flowDao.update(flowCondition);
        flow.setStatusId(status.statusId());
    }


    LinkFunction getFunction(Tree<?> tree) {
        return linkFunctionDao.get(tree.getFunctionId());
    }

    public static <T> T getFunctionObject(Long functionId) {
        return ApplicationContextProvider.getBean(linkFunctionDao.get(functionId).targetClass());
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
