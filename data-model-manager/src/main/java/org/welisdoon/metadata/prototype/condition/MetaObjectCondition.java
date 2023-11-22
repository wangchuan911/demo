package org.welisdoon.metadata.prototype.condition;

/**
 * @Classname MetaObjectCondition
 * @Description TODO
 * @Author Septem
 * @Date 18:04
 */
public class MetaObjectCondition {
    Long parentId;

    public MetaObjectCondition setParentId(Long parentId) {
        this.parentId = parentId;
        return this;
    }

    public Long getParentId() {
        return parentId;
    }
}
