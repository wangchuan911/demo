package org.welisdoon.flow.module.template.entity;

import org.welisdoon.flow.module.template.entity.struct.Tree;

import java.util.List;

/**
 * @Classname Link
 * @Description TODO
 * @Author wang.zhidong
 * @Date 2021/8/17 19:27
 */
public class Link extends Tree<Link> {
    Long templateId, nodeId;

    public Long getTemplateId() {
        return templateId;
    }

    public void setTemplateId(Long templateId) {
        this.templateId = templateId;
    }

    public Long getNodeId() {
        return nodeId;
    }

    public void setNodeId(Long nodeId) {
        this.nodeId = nodeId;
    }
}
