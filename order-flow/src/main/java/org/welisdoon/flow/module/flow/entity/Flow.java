package org.welisdoon.flow.module.flow.entity;

/**
 * @Classname Flow
 * @Description TODO
 * @Author wang.zhidong
 * @Date 2021/8/16 23:35
 */
public class Flow {
    Long id, templateId, statusId, functionId;
    Stream start;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public Stream getStart() {
        return start;
    }

    public void setStart(Stream start) {
        this.start = start;
    }

    public Long getFunctionId() {
        return functionId;
    }

    public void setFunctionId(Long functionId) {
        this.functionId = functionId;
    }
}
