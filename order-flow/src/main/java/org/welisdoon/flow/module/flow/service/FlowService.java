package org.welisdoon.flow.module.flow.service;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.welisdoon.flow.module.flow.dao.FlowDao;
import org.welisdoon.flow.module.flow.dao.FlowValueDao;
import org.welisdoon.flow.module.flow.entity.*;
import org.welisdoon.flow.module.template.dao.LinkDao;
import org.welisdoon.flow.module.template.dao.NodeDao;
import org.welisdoon.flow.module.flow.dao.StreamDao;
import org.welisdoon.flow.module.template.entity.Link;
import org.welisdoon.flow.module.template.entity.Template;
import org.welisdoon.flow.module.template.entity.TemplateCondition;
import org.welisdoon.flow.module.template.service.AbstractNode;

import java.util.LinkedList;

/**
 * @Classname FlowService
 * @Description TODO
 * @Author wang.zhidong
 * @Date 2021/8/17 19:56
 */
@Service
@Transactional(rollbackFor = Throwable.class)
public class FlowService {

    FlowDao flowDao;
    NodeDao nodeDao;
    StreamDao streamDao;
    LinkDao linkDao;
    FlowValueDao flowValueDao;

   /* @Autowired
    public void setFlowDao(FlowDao flowDao) {
        this.flowDao = flowDao;
    }*/

    @Autowired
    public void setLinkDao(LinkDao linkDao) {
        this.linkDao = linkDao;
    }

    @Autowired
    public void setNodeDao(NodeDao nodeDao) {
        this.nodeDao = nodeDao;
    }

    @Autowired
    public void setStreamDao(StreamDao streamDao) {
        this.streamDao = streamDao;
    }

    @Autowired
    public void setFlowDao(FlowDao flowDao) {
        this.flowDao = flowDao;
    }

    @Autowired
    public void setFlowValueDao(FlowValueDao flowValueDao) {
        this.flowValueDao = flowValueDao;
    }

    //    @PostConstruct
//    public void a() {
//        /*Flow flow = new Flow();
//        flow.setId(1L);
//        Stream stream = this.initFlow(flow);
//        stream.setStatusId(StreamStatus.READY.statusId());
//        AbstractNodeService abstractNodeService = AbstractNodeService.getInstance(stream.getNodeId());
//        abstractNodeService.start(stream);
//
//        FlowCondition condition1 = new FlowCondition();
//        condition1.setFlowId(1L);
//        condition1.setShowTree(true);
//        List<Stream> b = streamDao.list(condition1);
//        System.out.println(JSONArray.toJSONString(b));
//        streamDao.clear(condition1);*/
//        this.flow(new Flow());
//
//        hehe();
//    }

    public void flow(Flow flow) {
        flow.setStatusId(FlowStatus.FUTURE.statusId());
        flowDao.add(flow);

    }

    void initStreamData(Link superLink, Stream superStream) {
        superStream.setSubTree(new LinkedList<>());
        System.out.println(superLink);
        for (Link currentLink : superLink.getSubTree()) {
            for (Stream currentStream : AbstractNode.getInstance(currentLink.getNodeId()).createSubStreams(superStream, currentLink)) {
                currentStream.setStatusId(StreamStatus.FUTURE.statusId());
                currentStream.setFlow(superStream.getFlow());
                if (currentStream.getValue() != null) {
                    this.flowValueDao.add(currentStream.getValue());
                    currentStream.setValueId(currentStream.getValue().getId());
                }
                this.streamDao.add(currentStream);
                superStream.getSubTree().add(currentStream);
                this.initStreamData(currentLink, currentStream);
            }

        }
    }

    public void streamExpandByTemplate(Template template, Stream superStream) {
        TemplateCondition condition = new TemplateCondition();
        condition.setTemplateId(template.getId());
        condition.setShowTree(true);
        Link rootLink = linkDao.find(condition);
        superStream.setNodeId(rootLink.getNodeId());
        superStream.setFunctionId(rootLink.getFunctionId());
        this.initStreamData(rootLink, superStream);
    }


    public Flow start(Long flowId) {
        /*Stream stream = this.getStartStream(flow.getId());
        stream.setFlow(this.flowDao.get(stream.getFlowId()));*/
        /*if (CollectionUtils.isEmpty(stream.getSubTree())) {
            flowCondition.setSuperStreamId(stream.getId());
            flowCondition.setShowTree(true);
            flowCondition.setFlowId(stream.getFlowId());
            stream.setSubTree(this.streamDao.list(flowCondition));
        }*/
        Flow flow = this.flowDao.get(flowId);
        if (flow.getStatusId() == FlowStatus.FUTURE.statusId()
                && CollectionUtils.isEmpty(this.streamDao.list(new FlowCondition().setFlowId(flow.getId())))) {

            TemplateCondition condition = new TemplateCondition();
            condition.setTemplateId(flow.getTemplateId());
            condition.setShowTree(true);
            Link rootLink = linkDao.find(condition);
            Stream stream = new Stream();
            stream.setFlowId(flow.getId());
            /*stream.setNodeId(rootLink.getNodeId());
            stream.setFunctionId(rootLink.getFunctionId());
            stream.setSeq(rootLink.getSeq());*/
            stream.setStatusId(StreamStatus.FUTURE.statusId());
            stream.setFlow(flow);
            stream.sync(rootLink);
            streamDao.add(stream);
            this.initStreamData(rootLink, stream);
            System.out.println(JSONArray.toJSONString(rootLink));
            flow.setStart(stream);

            AbstractNode.getInstance(stream.getNodeId()).start(stream);
        } else {
            throw new RuntimeException("当前环节已结束");
        }
        return flow;
    }

    public Stream stream(Long id) {
        Stream stream = streamDao.get(id);
        if (stream.getStatusId() != StreamStatus.READY.statusId()) {
            throw new RuntimeException("当前环节已结束");
        }
        stream.setFlow(this.flowDao.get(stream.getFlowId()));
        /*if (CollectionUtils.isEmpty(stream.getSubTree())) {
            FlowCondition flowCondition = new FlowCondition();
            flowCondition.setSuperStreamId(stream.getId());
            flowCondition.setFlowId(stream.getFlowId());
            flowCondition.setShowTree(true);
            stream.setSubTree(this.streamDao.list(flowCondition));
        }*/
        AbstractNode.getInstance(stream.getNodeId()).finish(stream);
        return stream;
    }

    Stream getStartStream(long flowId) {
        FlowCondition flowCondition = new FlowCondition();
        flowCondition.setFlowId(flowId);
        return this.streamDao.find(flowCondition);
    }

    public Stream getStream(Long streamId) {
        return streamDao.get(streamId);
    }

    public FlowValue getValue(Long valueId) {
        return flowValueDao.get(valueId);
    }

    /*public boolean setValue(FlowValue flowValue) {
        return flowValueDao.put(flowValue) > 0;
    }

    public boolean addValue(FlowValue flowValue) {
        return flowValueDao.add(flowValue) > 0;
    }*/

    public void clear(Flow flow) {
        FlowCondition condition = new FlowCondition().setDelete("dismiss").setFlowId(flow.getId());
        flowDao.clear(condition);
        streamDao.clear(condition);
        FlowValue flowValue = new FlowValue();
        flowValue.setFlowId(flow.getId());
        flowValueDao.clear(flowValue);
    }

    public void undo(Stream stream) {
        AbstractNode node = AbstractNode.getInstance(stream.getNodeId());
        node.undo(stream, true);
        node.start(stream);
    }
}
