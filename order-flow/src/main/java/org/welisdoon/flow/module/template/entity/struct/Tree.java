package org.welisdoon.flow.module.template.entity.struct;

import java.util.List;

/**
 * @Classname Tree
 * @Description TODO
 * @Author wang.zhidong
 * @Date 2021/8/19 23:36
 */
public abstract class Tree<T> {
    Long id, superId,  seq, functionId;
    List<T> subTree;

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




    public Long getSeq() {
        return seq;
    }

    public void setSeq(Long seq) {
        this.seq = seq;
    }

    public Long getFunctionId() {
        return functionId;
    }

    public void setFunctionId(Long functionId) {
        this.functionId = functionId;
    }

    public List<T> getSubTree() {
        return subTree;
    }

    public void setSubTree(List<T> subTree) {
        this.subTree = subTree;
    }

    public boolean isLeaf() {
        return this.subTree == null || this.subTree.size() == 0;
    }
}
