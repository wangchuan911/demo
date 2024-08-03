package org.welisdoon.model.query.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.welisdoon.model.query.dao.HeaderLinkDao;
import org.welisdoon.model.query.entity.header.SimpleHeaderEntity;

/**
 * @Classname QueryService
 * @Description TODO
 * @Author Septem
 * @Date 21:56
 */
@Service
public class TableQueryService extends AbstractQueryService {
    HeaderLinkDao headerLinkDao;

    @Autowired
    public void setQueryDao(HeaderLinkDao headerLinkDao) {
        this.headerLinkDao = headerLinkDao;
    }

    @Override
    public void query(Long queryId, SimpleHeaderEntity... inputs) {

    }

    /*@Override
    public void query(Long queryId, SimpleHeaderEntity... inputs) {
        SimpleHeaderEntity[] headers = this.headerDao.list(new QueryHeaderCondition().setQueryId(queryId)).stream().filter(abstractHeaderEntity -> abstractHeaderEntity instanceof SimpleHeaderEntity).map(abstractHeaderEntity -> (SimpleHeaderEntity) abstractHeaderEntity).toArray(SimpleHeaderEntity[]::new);
        List<HeaderLinkEntity> links = headerLinkDao.list(new HeaderLinkCondition().setQueryId(queryId));
        Map<Long, Object> map;
        getValue(map = new HashMap<>(), links.toArray(HeaderLinkEntity[]::new), inputs);

    }

    protected void getValue(Map<Long, Object> value, HeaderLinkEntity[] links, SimpleHeaderEntity[] inputs) {
        for (int i = 0; i < links.length; i++) {
            getChildrenValue(value, links[i], inputs);
        }
    }

    protected void getChildrenValue(Map<Long, Object> value, HeaderLinkEntity link, SimpleHeaderEntity[] inputs) {
        getValue(value,
                link.getChildren(),
                Arrays
                        .stream(inputs)
                        .filter(simpleHeaderEntity ->
                                Arrays.stream(link.getChildren())
                                        .filter(headerLinkEntity ->
                                                Objects.equals(headerLinkEntity.getId(), simpleHeaderEntity.getLinkId()))
                                        .findAny()
                                        .isPresent())
                        .toArray(SimpleHeaderEntity[]::new));
    }*/

}
