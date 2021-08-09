package com.hubidaauto.servmarket.module.goods.page;

import com.github.pagehelper.PageHelper;
import com.hubidaauto.servmarket.module.goods.dao.ItemDao;
import com.hubidaauto.servmarket.module.goods.dao.ItemTypeDao;
import com.hubidaauto.servmarket.module.goods.entity.ItemCondition;
import com.hubidaauto.servmarket.module.goods.entity.ItemTypeCondition;
import com.hubidaauto.servmarket.module.goods.entity.ItemVO;
import com.hubidaauto.servmarket.module.goods.service.ItemService;
import io.vertx.core.json.Json;
import io.vertx.ext.web.handler.BodyHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.welisdoon.web.vertx.annotation.VertxConfiguration;
import org.welisdoon.web.vertx.annotation.VertxRoutePath;
import org.welisdoon.web.vertx.annotation.VertxRouter;
import org.welisdoon.web.vertx.enums.VertxRouteType;
import org.welisdoon.web.vertx.utils.RoutingContextChain;

import java.util.List;

/**
 * @author Septem
 */
@Component
@VertxConfiguration
@ConditionalOnProperty(prefix = "wechat-app-hubida", name = "appID")
@VertxRoutePath("{wechat-app-hubida.path.app}/goods")
public class ItemPage {
    ItemDao itemDao;
    ItemTypeDao itemTypeDao;
    ItemService itemService;

    @Autowired
    public void setItemDao(ItemDao itemDao) {
        this.itemDao = itemDao;
    }

    @Autowired
    public void setItemService(ItemService itemService) {
        this.itemService = itemService;
    }

    @Autowired
    public void setItemTypeDao(ItemTypeDao itemTypeDao) {
        this.itemTypeDao = itemTypeDao;
    }

    @VertxRouter(path = "/*", order = -1)
    public void all(RoutingContextChain chain) {
        chain.handler(BodyHandler.create());
    }


    @VertxRouter(path = "\\/(?<id>\\d+)",
            method = "GET",
            mode = VertxRouteType.PathRegex)
    public void get(RoutingContextChain chain) {
        chain.handler(routingContext -> {
            routingContext.end(Json.encodeToBuffer(itemDao.get(Long.parseLong(routingContext.pathParam("id")))));
        });
    }

    @VertxRouter(path = "\\/list(?:\\/(?<page>\\d+))?",
            method = "POST",
            mode = VertxRouteType.PathRegex)
    public void list(RoutingContextChain chain) {
        chain.handler(routingContext -> {
            ItemCondition itemCondition = routingContext.getBody() == null ? new ItemCondition() :
                    routingContext.getBodyAsJson().mapTo(ItemCondition.class);
            String page = routingContext.pathParam("page");
            if (!StringUtils.isEmpty(page))
                itemCondition.page(Integer.parseInt(page));
            routingContext.end(Json.encodeToBuffer(itemDao.list(itemCondition)));
        });
    }

    @VertxRouter(path = "\\/type\\/(?<id>\\d+)",
            method = "GET",
            mode = VertxRouteType.PathRegex)
    public void getType(RoutingContextChain chain) {
        chain.handler(routingContext -> {
            routingContext.end(Json.encodeToBuffer(itemTypeDao.get(Long.parseLong(routingContext.pathParam("id")))));
        });
    }

    @VertxRouter(path = "/type/list",
            method = "POST")
    public void listTypes(RoutingContextChain chain) {
        chain.handler(routingContext -> {
            System.out.println(routingContext.pathParam("page"));
            ItemTypeCondition itemTypeCondition = routingContext.getBody() == null ? new ItemTypeCondition() :
                    routingContext.getBodyAsJson().mapTo(ItemTypeCondition.class);
            routingContext.end(Json.encodeToBuffer(itemTypeDao.list(itemTypeCondition)));
        });
    }

    /*@VertxRouter(path = "/",
            method = "PUT")
    public void put(RoutingContextChain chain) {
        chain.handler(routingContext -> {
            routingContext.end(Json.encodeToBuffer(itemDao.put(routingContext.getBodyAsJson().mapTo(ItemVO.class))));
        });
    }*/

}
