package org.welisdoon.metadata.prototype.condition;

/**
 * @Classname MetaObjectCondition
 * @Description TODO
 * @Author Septem
 * @Date 18:04
 */
public class MetaAttributeCondition {
    Long parentId;
    Long objectId;

    public MetaAttributeCondition setParentId(Long parentId) {
        this.parentId = parentId;
        return this;
    }

    public Long getParentId() {
        return parentId;
    }

    public Long getObjectId() {
        return objectId;
    }

    public MetaAttributeCondition setObjectId(Long objectId) {
        this.objectId = objectId;
        return this;
    }
}
