package org.welisdoon.model.query.entity.condition;

import org.welisdoon.model.query.entity.AbstractHeaderEntity;

/**
 * @Classname HeadCondition
 * @Description TODO
 * @Author Septem
 * @Date 15:36
 */
public class QueryHeaderCondition {


    Long parentHeadId, queryId;

    public QueryHeaderCondition setParentHeadId(Long parentHeadId) {
        this.parentHeadId = parentHeadId;
        return this;
    }

    public Long getParentHeadId() {
        return parentHeadId;
    }

    public Long getQueryId() {
        return queryId;
    }

    public QueryHeaderCondition setQueryId(Long queryId) {
        this.queryId = queryId;
        return this;
    }
}
