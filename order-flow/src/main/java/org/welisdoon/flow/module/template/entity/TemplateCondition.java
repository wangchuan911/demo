package org.welisdoon.flow.module.template.entity;

/**
 * @Classname FlowCondition
 * @Description TODO
 * @Author wang.zhidong
 * @Date 2021/8/17 19:35
 */
public class TemplateCondition {
    Long nodeId, superLinkId, linkId, templateId, functionId;

    public Long getNodeId() {
        return nodeId;
    }

    public void setNodeId(Long nodeId) {
        this.nodeId = nodeId;
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


    public Long getTemplateId() {
        return templateId;
    }

    public void setTemplateId(Long templateId) {
        this.templateId = templateId;
    }

    public Long getFunctionId() {
        return functionId;
    }

    public void setFunctionId(Long functionId) {
        this.functionId = functionId;
    }
}
