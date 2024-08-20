package org.welisdoon.metadata.prototype.condition;

import org.welisdoon.common.data.BaseCondition;
import org.welisdoon.metadata.prototype.define.MetaLink;
import org.welisdoon.metadata.prototype.define.MetaObject;

/**
 * @Classname MetaLinkCondition
 * @Description TODO
 * @Author Septem
 * @Date 18:08
 */
public class MetaLinkCondition extends BaseCondition<Long, MetaLink> {

    Long parentId;

    public MetaLinkCondition setParentId(Long parentId) {
        this.parentId = parentId;
        return this;
    }

    public Long getParentId() {
        return parentId;
    }
}
