package org.welisdoon.metadata.prototype.router;

import com.alibaba.fastjson.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.welisdoon.model.data.dao.TableDao;
import org.welisdoon.model.data.service.DataTableService;
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
@VertxRoutePath(prefix = "/md", requestBodyEnable = true)
public class QueryManagerRouter {


    @VertxRouter(path = "\\/obj\\/(?<id>\\d+)",
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
