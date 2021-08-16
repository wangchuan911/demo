package com.hubidaauto.servmarket.module.common.entity;

/**
 * @Classname ContentVO
 * @Description TODO
 * @Author wang.zhidong
 * @Date 2021/8/11 20:00
 */
public abstract class ContentVO {
    Long id, type;

    String content, refTable;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }


    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Long getType() {
        return type;
    }

    public void setType(Long type) {
        this.type = type;
    }

    public String getRefTable() {
        return refTable;
    }

    public void setRefTable(String refTable) {
        this.refTable = refTable;
    }
}
