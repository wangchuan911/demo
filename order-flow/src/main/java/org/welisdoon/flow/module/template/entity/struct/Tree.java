package org.welisdoon.flow.module.template.entity.struct;

import com.alibaba.fastjson.annotation.JSONField;
import com.fasterxml.jackson.annotation.JsonIgnore;
import org.welisdoon.flow.module.template.entity.LinkShow;

import java.util.List;

/**
 * @Classname Tree
 * @Description TODO
 * @Author wang.zhidong
 * @Date 2021/8/19 23:36
 */
public abstract class Tree<T> {
    Long id, superId, functionId, nodeId, valueId, showId;
    int seq;
    String name;
    List<T> subTree;
    LinkShow show;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getSuperId() {
        return superId;
    }

    public void setSuperId(Long superId) {
        this.superId = superId;
    }


    public Integer getSeq() {
        return seq;
    }

    public void setSeq(Integer seq) {
        this.seq = seq;
    }

    public Long getFunctionId() {
        return functionId;
    }

    public void setFunctionId(Long functionId) {
        this.functionId = functionId;
    }

    @JsonIgnore
    @JSONField(deserialize = false, serialize = false)
    public List<T> getSubTree() {
        return subTree;
    }

    public void setSubTree(List<T> subTree) {
        this.subTree = subTree;
    }

    public boolean isLeaf() {
        return this.subTree == null || this.subTree.size() == 0;
    }

    public Long getNodeId() {
        return nodeId;
    }

    public void setNodeId(Long nodeId) {
        this.nodeId = nodeId;
    }

    public void setSeq(int seq) {
        this.seq = seq;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }


    public Long getValueId() {
        return valueId;
    }

    public void setValueId(Long valueId) {
        this.valueId = valueId;
    }

    public Long getShowId() {
        return showId;
    }

    public void setShowId(Long showId) {
        this.showId = showId;
    }

    public LinkShow getShow() {
        return show;
    }

    public void setShow(LinkShow show) {
        this.show = show;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("Tree{");
        sb.append("id=").append(id);
        sb.append(", superId=").append(superId);
        sb.append(", functionId=").append(functionId);
        sb.append(", nodeId=").append(nodeId);
        sb.append(", seq=").append(seq);
        sb.append(", name='").append(name).append('\'');
        sb.append(", subTree=").append(subTree);
        sb.append('}');
        return sb.toString();
    }
}
