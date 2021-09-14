package org.welisdoon.flow.module.flow.entity;

import org.welisdoon.flow.module.template.entity.Link;
import org.welisdoon.flow.module.template.entity.struct.Tree;

/**
 * @Classname Stream
 * @Description TODO
 * @Author wang.zhidong
 * @Date 2021/8/16 23:35
 */
public class Stream extends Tree<Stream> {
    Long flowId, statusId;

    Flow flow;
    FlowValue value;

    public void setFlow(Flow flow) {
        this.flow = flow;
    }

    public Flow getFlow() {
        return flow;
    }

    public Long getFlowId() {
        return flowId;
    }

    public void setFlowId(Long flowId) {
        this.flowId = flowId;
    }


    public Long getStatusId() {
        return statusId;
    }

    public void setStatusId(Long statusId) {
        this.statusId = statusId;
    }

    public void sync(Stream superStream) {
        this.setFlow(superStream.getFlow());
        this.setFlowId(superStream.getFlowId());
        this.setSuperId(superStream.getId());
    }

    public void sync(Link currentLink) {
        this.setNodeId(currentLink.getNodeId());
        this.setFunctionId(currentLink.getFunctionId());
        this.setValueId(currentLink.getValueId());
        this.setSeq(currentLink.getSeq());
        this.setName(currentLink.getName());

        /*currentStream.setFlowId(superStream.getFlowId());
        currentStream.setNodeId(currentLink.getNodeId());
        currentStream.setFunctionId(currentLink.getFunctionId());
        currentStream.setSeq(currentLink.getSeq());
        currentStream.setSuperId(superStream.getId());
        currentStream.setName(currentLink.getName());*/
    }

    public FlowValue getValue() {
        return value;
    }

    public void setValue(FlowValue value) {
        this.value = value;
    }
}
