package org.welisdoon.metadata.prototype.condition;

/**
 * @Classname MetaLinkCondition
 * @Description TODO
 * @Author Septem
 * @Date 18:08
 */
public class MetaLinkCondition {

    Long parentId;

    public MetaLinkCondition setParentId(Long parentId) {
        this.parentId = parentId;
        return this;
    }

    public Long getParentId() {
        return parentId;
    }
}
