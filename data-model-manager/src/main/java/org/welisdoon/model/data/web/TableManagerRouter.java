package org.welisdoon.model.data.web;

import com.alibaba.fastjson.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.welisdoon.model.data.dao.ColumnDao;
import org.welisdoon.model.data.dao.TableDao;
import org.welisdoon.web.vertx.annotation.VertxConfiguration;
import org.welisdoon.web.vertx.annotation.VertxRoutePath;
import org.welisdoon.web.vertx.annotation.VertxRouter;
import org.welisdoon.web.vertx.enums.VertxRouteType;
import org.welisdoon.web.vertx.utils.RoutingContextChain;

/**
 * @Classname TableManagerRouter
 * @Description TODO
 * @Author Septem
 * @Date 16:02
 */

@Component
@VertxConfiguration
@VertxRoutePath(prefix = "/database", requestBodyEnable = true)
public class TableManagerRouter {
    TableDao tableDao;
    ColumnDao columnDao;

    @Autowired
    void setValue(TableDao tableDao, ColumnDao columnDao) {
        this.columnDao = columnDao;
        this.tableDao = tableDao;
    }

    @VertxRouter(path = "\\/table\\/(?<id>\\d+)",
            method = "GET",
            mode = VertxRouteType.PathRegex)
    public void listUser(RoutingContextChain chain) {
        chain.handler(routingContext -> {
            routingContext.end(JSONObject.toJSONString(tableDao.get(Long.valueOf(routingContext.pathParam("id")))));
        });
    }
}
