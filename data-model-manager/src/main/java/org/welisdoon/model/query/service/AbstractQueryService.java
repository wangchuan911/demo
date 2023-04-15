package org.welisdoon.model.query.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.welisdoon.model.query.dao.HeaderDao;
import org.welisdoon.model.query.entity.AbstractHeaderEntity;
import org.welisdoon.model.query.entity.condition.QueryHeaderCondition;
import org.welisdoon.model.query.entity.header.GroupHeaderEntity;
import org.welisdoon.model.query.entity.header.SimpleHeaderEntity;

import java.util.List;

/**
 * @Classname AbstractQueryService
 * @Description TODO
 * @Author Septem
 * @Date 12:56
 */
public abstract class AbstractQueryService {
    HeaderDao headerDao;

    @Autowired
    public void setHeaderDao(HeaderDao headerDao) {
        this.headerDao = headerDao;
    }

    public List<AbstractHeaderEntity> getHeaders(Long queryId) {
        List<AbstractHeaderEntity> entities = headerDao.list(new QueryHeaderCondition().setQueryId(queryId));
        for (AbstractHeaderEntity entity : entities) {
            setParent(entity, null);
        }
        return entities;
    }

    protected void setParent(AbstractHeaderEntity child, AbstractHeaderEntity parent) {
        child.setParent(parent);
        if (child instanceof GroupHeaderEntity) {
            for (AbstractHeaderEntity child2 : (((GroupHeaderEntity) child).getChildren())) {
                setParent(child2, child);
            }
        }
    }

    public abstract void query(Long queryId, SimpleHeaderEntity... inputs);
}
