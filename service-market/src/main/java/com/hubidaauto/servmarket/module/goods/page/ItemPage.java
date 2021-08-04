package com.hubidaauto.servmarket.module.goods.page;

import com.hubidaauto.servmarket.module.goods.dao.ItemDao;
import com.hubidaauto.servmarket.module.goods.entity.ItemCondition;
import com.hubidaauto.servmarket.module.goods.entity.ItemVO;
import io.vertx.core.json.Json;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;
import org.welisdoon.web.vertx.annotation.VertxConfiguration;
import org.welisdoon.web.vertx.annotation.VertxRoutePath;
import org.welisdoon.web.vertx.annotation.VertxRouter;
import org.welisdoon.web.vertx.enums.VertxRouteType;
import org.welisdoon.web.vertx.utils.RoutingContextChain;

import java.util.List;

@Component
@VertxConfiguration
@ConditionalOnProperty(prefix = "wechat-app-hubida", name = "appID")
@VertxRoutePath("{wechat-app-hubida.path.app}/goods")
public class ItemPage {
    @Autowired
    ItemDao itemDao;

    @VertxRouter(path = "/:id",
            method = "GET",
            order = Integer.MIN_VALUE)
    public void get(RoutingContextChain chain) {
        chain.handler(routingContext -> {
            routingContext.end(Json.encodeToBuffer(itemDao.get(Long.parseLong(routingContext.pathParam("id")))));
        });
    }

    @VertxRouter(path = "/list",
            method = "POST",
            order = Integer.MIN_VALUE)
    public void list(RoutingContextChain chain) {
        chain.handler(routingContext -> {
            routingContext.end(Json.encodeToBuffer(itemDao.list(routingContext.getBodyAsJson().mapTo(ItemCondition.class))));
        });
    }

}
