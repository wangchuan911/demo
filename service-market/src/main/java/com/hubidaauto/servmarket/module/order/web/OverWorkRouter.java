package com.hubidaauto.servmarket.module.order.web;

import com.alibaba.fastjson.JSONObject;
import com.hubidaauto.servmarket.module.goods.dao.ItemTypeDao;
import com.hubidaauto.servmarket.module.goods.dao.OverTimeItemLinkDao;
import com.hubidaauto.servmarket.module.goods.entity.ItemTypeVO;
import com.hubidaauto.servmarket.module.goods.entity.OverTimeItemVO;
import io.vertx.ext.web.handler.BodyHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.welisdoon.web.vertx.annotation.VertxConfiguration;
import org.welisdoon.web.vertx.annotation.VertxRoutePath;
import org.welisdoon.web.vertx.annotation.VertxRouter;
import org.welisdoon.web.vertx.enums.VertxRouteType;
import org.welisdoon.web.vertx.utils.RoutingContextChain;

/**
 * @Classname OverWorkRouter
 * @Description TODO
 * @Author wang.zhidong
 * @Date 2021/12/2 21:41
 */
@Component
@VertxConfiguration
@ConditionalOnProperty(prefix = "wechat-app-hubida", name = "appID")
@VertxRoutePath(prefix = "{wechat-app-hubida.path.app}/overtime")
public class OverWorkRouter {
    ItemTypeDao itemTypeDao;
    OverTimeItemLinkDao overTimeItemLinkDao;

    @Autowired
    public void setItemTypeDao(ItemTypeDao itemTypeDao) {
        this.itemTypeDao = itemTypeDao;
    }

    @Autowired
    public void setOverTimeItemLinkDao(OverTimeItemLinkDao overTimeItemLinkDao) {
        this.overTimeItemLinkDao = overTimeItemLinkDao;
    }
    @VertxRouter(path = "/*", order = -1)
    public void all(RoutingContextChain chain) {
        chain.handler(BodyHandler.create());
    }


    @VertxRouter(path = "\\/item\\/(?<itemTypeId>\\d+)",
            method = "GET",
            mode = VertxRouteType.PathRegex)
    public void item(RoutingContextChain chain) {
        chain.handler(routingContext -> {
            ItemTypeVO itemType = itemTypeDao.get(Long.valueOf(routingContext.pathParam("itemTypeId")));
            OverTimeItemVO overTimeItem = overTimeItemLinkDao.get(itemType.getItemId());
            routingContext.end(JSONObject.toJSONString(overTimeItem));

        });
    }
}
