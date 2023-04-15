package org.welisdoon.model.query.web;

import com.alibaba.fastjson.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.welisdoon.model.data.dao.TableDao;
import org.welisdoon.model.data.service.DataBaseService;
import org.welisdoon.model.query.dao.HeaderDao;
import org.welisdoon.model.query.dao.HeaderLinkDao;
import org.welisdoon.model.query.entity.condition.QueryHeaderCondition;
import org.welisdoon.web.vertx.annotation.VertxConfiguration;
import org.welisdoon.web.vertx.annotation.VertxRoutePath;
import org.welisdoon.web.vertx.annotation.VertxRouter;
import org.welisdoon.web.vertx.enums.VertxRouteType;
import org.welisdoon.web.vertx.utils.RoutingContextChain;

/**
 * @Classname QueryManagerRouter
 * @Description TODO
 * @Author Septem
 * @Date 16:13
 */
@Component
@VertxConfiguration
@VertxRoutePath(prefix = "/dm/query", requestBodyEnable = true)
public class QueryManagerRouter {
    HeaderDao headerDao;
    HeaderLinkDao headerLinkDao;
    DataBaseService baseService;
    TableDao tableDao;

    @Autowired
    void setValue(HeaderDao headerDao, HeaderLinkDao headerLinkDao, DataBaseService baseService, TableDao tableDao) {
        this.headerDao = headerDao;
        this.headerLinkDao = headerLinkDao;
        this.baseService = baseService;
        this.tableDao = tableDao;
    }

    @VertxRouter(path = "\\/(?<type>\\w+)\\/(?<qid>\\d+)",
            method = "GET",
            mode = VertxRouteType.PathRegex)
    public void query(RoutingContextChain chain) {
        chain.handler(routingContext -> {
            long qid = Long.valueOf(routingContext.pathParam("qid"));
            switch (routingContext.pathParam("type")) {
                case "header": {
                    routingContext.end(JSONObject.toJSONString(headerDao.list(new QueryHeaderCondition().setQueryId(qid))));
                    return;
                }
                case "query": {

                    return;
                }
                case "input": {

                }
                break;
                default:
            }

        });
    }

}
