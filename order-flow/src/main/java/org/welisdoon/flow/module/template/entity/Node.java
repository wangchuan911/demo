package org.welisdoon.flow.module.template.entity;

/**
 * @Classname Node
 * @Description TODO
 * @Author wang.zhidong
 * @Date 2021/8/16 23:30
 */
public class Node {
    Long id, typeId, executeId;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getTypeId() {
        return typeId;
    }

    public void setTypeId(Long typeId) {
        this.typeId = typeId;
    }

    public Long getExecuteId() {
        return executeId;
    }

    public void setExecuteId(Long executeId) {
        this.executeId = executeId;
    }
}
