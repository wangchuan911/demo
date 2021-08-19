package org.welisdoon.flow.module.template.entity;

import com.baomidou.dynamic.datasource.annotation.DS;

/**
 * @Classname Flow
 * @Description TODO
 * @Author wang.zhidong
 * @Date 2021/8/16 23:35
 */
public class Flow {
    Long id, templateId, statusId;

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
}
