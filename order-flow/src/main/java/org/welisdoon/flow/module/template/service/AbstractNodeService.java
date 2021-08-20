package org.welisdoon.flow.module.template.service;

import org.reflections.ReflectionUtils;
import org.reflections.Reflections;
import org.springframework.beans.factory.annotation.Autowired;
import org.welisdoon.flow.module.flow.entity.FlowCondition;
import org.welisdoon.flow.module.flow.entity.Stream;
import org.welisdoon.flow.module.template.annotation.NodeType;
import org.welisdoon.flow.module.template.dao.LinkDao;
import org.welisdoon.flow.module.flow.dao.StreamDao;
import org.welisdoon.flow.module.template.dao.NodeDao;
import org.welisdoon.flow.module.template.entity.Link;
import org.welisdoon.flow.module.template.entity.Node;
import org.welisdoon.web.common.ApplicationContextProvider;

import java.util.List;
import java.util.Set;

/**
 * @Classname AbstractNodeService
 * @Description TODO
 * @Author wang.zhidong
 * @Date 2021/8/19 23:44
 */
public abstract class AbstractNodeService {
    private static Reflections reflections;
    LinkDao linkDao;
    StreamDao streamDao;
    NodeDao nodeDao;

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
    public static void setReflections(Reflections reflections) {
        AbstractNodeService.reflections = reflections;
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

    public void setNodeDao(NodeDao nodeDao) {
        this.nodeDao = nodeDao;
    }

    public abstract void start(Stream stream);

    public abstract void finish(Stream stream);

    public static AbstractNodeService getInstance(Long nodeId) {
        NodeDao nodeDao = ApplicationContextProvider.getBean(NodeDao.class);
        return getInstance(nodeDao.get(nodeId));
    }

    public static AbstractNodeService getInstance(Node node) {
        Reflections reflections = ApplicationContextProvider.getBean(Reflections.class);
        Set<Class<?>> classes = reflections.getTypesAnnotatedWith(NodeType.class);
        return (AbstractNodeService) ApplicationContextProvider.getBean(classes.stream().filter(aClass -> aClass.isAssignableFrom(AbstractNodeService.class) && aClass.getAnnotation(NodeType.class).value() == node.getTypeId()).findFirst().get());
    }

    public List<Stream> getSubStreams(Stream stream) {
        FlowCondition flowCondition = new FlowCondition();
        flowCondition.setFlowId(stream.getFlowId());
        flowCondition.setSuperStreamId(stream.getId());
        List<Stream> subStreams = getStreamDao().list(flowCondition);
        return subStreams;
    }

    public void createStream(Stream stream) {
        this.getStreamDao().add(stream);
    }
}
