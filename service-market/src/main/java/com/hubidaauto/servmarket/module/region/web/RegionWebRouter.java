package com.hubidaauto.servmarket.module.region.web;

import com.alibaba.fastjson.JSONObject;
import com.hubidaauto.servmarket.module.region.dao.RegionDao;
import com.hubidaauto.servmarket.module.region.entity.RegionCondition;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.welisdoon.web.vertx.annotation.VertxConfiguration;
import org.welisdoon.web.vertx.annotation.VertxRoutePath;
import org.welisdoon.web.vertx.annotation.VertxRouter;
import org.welisdoon.web.vertx.enums.VertxRouteType;
import org.welisdoon.web.vertx.utils.RoutingContextChain;

/**
 * @Classname OrderWebRouter
 * @Description TODO
 * @Author wang.zhidong
 * @Date 2021/8/13 12:14
 */
@Component
@VertxConfiguration
@ConditionalOnProperty(prefix = "wechat-app-hubida", name = "appID")
@VertxRoutePath(prefix = "{wechat-app-hubida.path.app}/region")
public class RegionWebRouter {


    RegionDao regionDao;

    @Autowired
    public void setRegionDao(RegionDao regionDao) {
        this.regionDao = regionDao;
    }


    @VertxRouter(path = "\\/list\\/(?<id>\\d*)", mode = VertxRouteType.PathRegex,
            method = "GET")
    public void list(RoutingContextChain chain) {
        chain.handler(routingContext -> {
            RegionCondition condition = new RegionCondition();
            String id = routingContext.pathParam("id");
            if (!StringUtils.isEmpty(id))
                condition.setId(Long.parseLong(id));
            regionDao.list(condition);
            routingContext.end(JSONObject.toJSONString(regionDao.list(condition)));

        });
    }


}
