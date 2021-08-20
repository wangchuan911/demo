package org.welisdoon.flow.module.flow.entity;

import org.welisdoon.flow.module.template.entity.struct.Tree;

/**
 * @Classname Stream
 * @Description TODO
 * @Author wang.zhidong
 * @Date 2021/8/16 23:35
 */
public class Stream extends Tree<Stream> {
    Long flowId, linkId, statusId;

    public Long getFlowId() {
        return flowId;
    }

    public void setFlowId(Long flowId) {
        this.flowId = flowId;
    }

    public Long getLinkId() {
        return linkId;
    }

    public void setLinkId(Long linkId) {
        this.linkId = linkId;
    }

    public Long getStatusId() {
        return statusId;
    }

    public void setStatusId(Long statusId) {
        this.statusId = statusId;
    }
}
