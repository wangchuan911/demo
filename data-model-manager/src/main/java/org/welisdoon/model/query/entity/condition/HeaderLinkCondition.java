package org.welisdoon.model.query.entity.condition;

import org.welisdoon.common.data.IData;
import org.welisdoon.model.query.entity.AbstractHeaderEntity;

import java.util.Arrays;

/**
 * @Classname QueryCondition
 * @Description TODO
 * @Author Septem
 * @Date 16:25
 */
public class HeaderLinkCondition {
    Long[] linkIds;
    Long parentId, queryId;

    public HeaderLinkCondition setLinkIds(Long[] linkIds) {
        this.linkIds = linkIds;
        return this;
    }

    public HeaderLinkCondition setLinkIds(AbstractHeaderEntity... headerEntities) {
        this.linkIds = Arrays.stream(headerEntities).map(IData.DataObject::getId).toArray(Long[]::new);
        return this;
    }

    public Long[] getLinkIds() {
        return linkIds;
    }

    public Long getParentId() {
        return parentId;
    }

    public HeaderLinkCondition setParentId(Long parentId) {
        this.parentId = parentId;
        return this;
    }

    public Long getQueryId() {
        return queryId;
    }

    public HeaderLinkCondition setQueryId(Long queryId) {
        this.queryId = queryId;
        return this;
    }
}
