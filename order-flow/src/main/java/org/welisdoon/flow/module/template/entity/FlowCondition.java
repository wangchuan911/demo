package org.welisdoon.flow.module.template.entity;

/**
 * @Classname FlowCondition
 * @Description TODO
 * @Author wang.zhidong
 * @Date 2021/8/17 19:35
 */
public class FlowCondition {
    Long nodeId, flowId, superLinkId, linkId, streamId, templateId, statusId, superStreamId;
    boolean showTree;

    public boolean isShowTree() {
        return showTree;
    }

    public void setShowTree(boolean showTree) {
        this.showTree = showTree;
    }

    public Long getNodeId() {
        return nodeId;
    }

    public void setNodeId(Long nodeId) {
        this.nodeId = nodeId;
    }

    public Long getFlowId() {
        return flowId;
    }

    public void setFlowId(Long flowId) {
        this.flowId = flowId;
    }

    public Long getSuperLinkId() {
        return superLinkId;
    }

    public void setSuperLinkId(Long superLinkId) {
        this.superLinkId = superLinkId;
    }

    public Long getLinkId() {
        return linkId;
    }

    public void setLinkId(Long linkId) {
        this.linkId = linkId;
    }

    public Long getStreamId() {
        return streamId;
    }

    public void setStreamId(Long streamId) {
        this.streamId = streamId;
    }

    public Long getTemplateId() {
        return templateId;
    }

    public void setTemplateId(Long templateId) {
        this.templateId = templateId;
    }

    public Long getStatusId() {
        return statusId;
    }

    public void setStatusId(Long statusId) {
        this.statusId = statusId;
    }

    public Long getSuperStreamId() {
        return superStreamId;
    }

    public void setSuperStreamId(Long superStreamId) {
        this.superStreamId = superStreamId;
    }
}
