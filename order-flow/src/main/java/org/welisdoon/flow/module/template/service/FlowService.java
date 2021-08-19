package org.welisdoon.flow.module.template.service;

import com.alibaba.fastjson.JSONArray;
import com.baomidou.dynamic.datasource.annotation.DS;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.welisdoon.flow.module.template.dao.FlowDao;
import org.welisdoon.flow.module.template.dao.LinkDao;
import org.welisdoon.flow.module.template.dao.NodeDao;
import org.welisdoon.flow.module.template.dao.StreamDao;
import org.welisdoon.flow.module.template.entity.Flow;
import org.welisdoon.flow.module.template.entity.FlowCondition;
import org.welisdoon.flow.module.template.entity.Link;
import org.welisdoon.flow.module.template.entity.Stream;

import javax.annotation.PostConstruct;
import java.util.LinkedList;
import java.util.List;

/**
 * @Classname FlowService
 * @Description TODO
 * @Author wang.zhidong
 * @Date 2021/8/17 19:56
 */
@Service
@DS("flow")
@Transactional
public class FlowService {

    FlowDao flowDao;
    NodeDao nodeDao;
    StreamDao streamDao;
    LinkDao linkDao;

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

    @PostConstruct
    public void initFlow() {
        FlowCondition condition = new FlowCondition();
        condition.setFlowId(1L);
        condition.setTemplateId(1L);
        condition.setShowTree(true);
        Link rootLink = linkDao.list(condition).get(0);
        Stream stream = new Stream();
        stream.setFlowId(0L);
        stream.setLinkId(rootLink.getId());
        stream.setFunctionId(rootLink.getFunctionId());
        stream.setSeq(rootLink.getSeq());
        streamDao.add(stream);
        this.initStreamData(rootLink, stream);

        List<Stream> b = streamDao.list(condition);
        System.out.println(JSONArray.toJSONString(rootLink));
        System.out.println(JSONArray.toJSONString(b));
        streamDao.clear(condition);
    }

    public void initStreamData(Link superLink, Stream superStream) {
        Stream currentStream;
        superStream.setSubTree(new LinkedList<>());
        for (Link currentLink : superLink.getSubTree()) {
            currentStream = new Stream();
            currentStream.setFlowId(superStream.getFlowId());
            currentStream.setLinkId(currentLink.getId());
            currentStream.setFunctionId(currentLink.getFunctionId());
            currentStream.setSeq(currentLink.getSeq());
            currentStream.setSuperId(superStream.getId());
            streamDao.add(currentStream);
            superStream.getSubTree().add(currentStream);
            this.initStreamData(currentLink, currentStream);
        }
    }

    public Flow flow(Flow flow) {
        flow.setTemplateId(0L);
        /*flowDao.add(flow);

        flow.getTemplateId();

        List<Link> lists = this.getLinks(flow.getTemplateId(), null);
        Link link = lists.get(0);
        Stream stream = new Stream();
        stream.setLinkId(link.getId());
        streamDao.add(stream);*/

        return flow;
    }


    public Stream stream(Stream stream) {
        /*stream.setStatusId(0L);
        Link link = linkDao.get(stream.getLinkId());
//        List<Link> sublists = this.getLinks(link.getTemplateId(), link.getNodeId());

        //同级别节点
        List<Link> braotherLinks = this.getLinks(link.getTemplateId(), link.getSuperId());
        long working = braotherLinks.stream().filter(brotherLink -> {
            if (brotherLink.getId().equals(link.getId())) {
                return false;
            }
            FlowCondition flowCondition = new FlowCondition();
            flowCondition.setLinkId(brotherLink.getId());
            List<Stream> otherStreams = streamDao.list(flowCondition);
            if (!CollectionUtils.isEmpty(otherStreams)) {
                //其他stream也完成了
                if (otherStreams.get(0).getStatusId().equals(stream.getStatusId())) {
                    return false;
                }
            }

            return true;
        }).count();

        if (working == 0L) {
            //当前 link.getSuperLinkId() 下的节点都完成了
            //找更上一级别的link节点
            Link superLink = linkDao.get(link.getId());
            List<Link> parantLinks = this.getLinks(link.getTemplateId(), link.getSuperId());
            if (superLink.getSuperId() == null) {
                //是顶级节点
                for (int i = 0; i < parantLinks.size(); i++) {
                    if (parantLinks.get(i).getSeq().equals(superLink.getSeq())) {
                        if (i + 1 < parantLinks.size()) {
                            //开始下一节点
                            Link nextLink = parantLinks.get(i + 1);
                            List<Link> nextLinks = this.getLinks(nextLink.getTemplateId(), nextLink.getId());
                        } else {
                            //没有后续了，结束
                            Flow flow = flowDao.get(stream.getFlowId());
                            flow.setStatusId(0L);
                            flowDao.put(flow);
                        }
                    }
                }
            }
        }
*/
        return stream;
    }

    List<Link> getLinks(Long templateId, Long superLinkId) {
        FlowCondition flowCondition = new FlowCondition();
        flowCondition.setTemplateId(templateId);
        flowCondition.setSuperLinkId(superLinkId);
        return linkDao.list(flowCondition);
    }


}
