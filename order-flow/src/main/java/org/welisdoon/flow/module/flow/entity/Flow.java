package org.welisdoon.flow.module.flow.entity;

import com.alibaba.fastjson.annotation.JSONField;
import com.fasterxml.jackson.annotation.JsonIgnore;

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

    @JsonIgnore
    @JSONField(deserialize = false, serialize = false)
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

    public void sync(Flow flow){
        this.setTemplateId(flow.getTemplateId());
        this.setStatusId(flow.getStatusId());
        this.setFunctionId(flow.getFunctionId());
    }
}
