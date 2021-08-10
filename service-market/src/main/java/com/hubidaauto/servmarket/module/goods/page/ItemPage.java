package com.hubidaauto.servmarket.module.goods.page;

import com.hubidaauto.servmarket.module.goods.dao.ItemDao;
import com.hubidaauto.servmarket.module.goods.dao.ItemDetailDao;
import com.hubidaauto.servmarket.module.goods.dao.ItemTypeDao;
import com.hubidaauto.servmarket.module.goods.entity.ItemCondition;
import com.hubidaauto.servmarket.module.goods.entity.ItemVO;
import com.hubidaauto.servmarket.module.goods.service.ItemService;
import io.vertx.core.json.Json;
import io.vertx.ext.web.handler.BodyHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
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
    ItemDetailDao itemDetailDao;

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

    @Autowired
    public void setItemDetailDao(ItemDetailDao itemDetailDao) {
        this.itemDetailDao = itemDetailDao;
    }

    @VertxRouter(path = "/*", order = -1)
    public void all(RoutingContextChain chain) {
        chain.handler(BodyHandler.create());
    }


    @VertxRouter(path = "\\/(?<id>\\d+)",
            method = "GET",
            mode = VertxRouteType.PathRegex)
    public void getItem(RoutingContextChain chain) {
        chain.handler(routingContext -> {
            ItemVO itemVO = itemDao.get(Long.parseLong(routingContext.pathParam("id")));
            itemVO.setDetail(itemDetailDao.get(itemVO.getId()));
            routingContext.end(Json.encodeToBuffer(itemVO));
        });
    }

    @VertxRouter(path = "\\/list(?:\\/(?<page>\\d+))?",
            method = "POST",
            mode = VertxRouteType.PathRegex)
    public void listItem(RoutingContextChain chain) {
        chain.handler(routingContext -> {
            ItemCondition itemCondition = routingContext.getBody() == null ? new ItemCondition() :
                    routingContext.getBodyAsJson().mapTo(ItemCondition.class);
            String page = routingContext.pathParam("page");
            if (!StringUtils.isEmpty(page))
                itemCondition.page(Integer.parseInt(page));
            routingContext.end(Json.encodeToBuffer(itemDao.list(itemCondition)));
        });
    }


    @VertxRouter(path = "/type/list",
            method = "POST")
    public void listTypes(RoutingContextChain chain) {
        chain.handler(routingContext -> {
            ItemCondition itemCondition = routingContext.getBody() == null ? new ItemCondition() :
                    routingContext.getBodyAsJson().mapTo(ItemCondition.class);
            routingContext.end(Json.encodeToBuffer(itemTypeDao.list(itemCondition)));
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
