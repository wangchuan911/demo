package com.hubidaauto.servmarket.module.popularize.web;

import com.alibaba.fastjson.JSON;
import com.hubidaauto.servmarket.common.utils.JsonUtils;
import com.hubidaauto.servmarket.module.order.dao.BaseOrderDao;
import com.hubidaauto.servmarket.module.popularize.dao.InviteRebateOrderDao;
import com.hubidaauto.servmarket.module.popularize.entity.InviteRebateOrderCondition;
import com.hubidaauto.servmarket.module.popularize.entity.InviteRebateOrderVO;
import com.hubidaauto.servmarket.weapp.ServiceMarketConfiguration;
import io.vertx.ext.web.handler.BodyHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.welisdoon.web.common.config.AbstractWechatConfiguration;
import org.welisdoon.web.vertx.annotation.VertxConfiguration;
import org.welisdoon.web.vertx.annotation.VertxRoutePath;
import org.welisdoon.web.vertx.annotation.VertxRouter;
import org.welisdoon.web.vertx.enums.VertxRouteType;
import org.welisdoon.web.vertx.utils.RoutingContextChain;
import org.welisdoon.web.vertx.verticle.WorkerVerticle;

import java.util.List;

/**
 * @author Septem
 */
@Component
@VertxConfiguration
@ConditionalOnProperty(prefix = "wechat-app-hubida", name = "appID")
@VertxRoutePath("{wechat-app-hubida.path.app}/invite")
public class InviteRebateOrderRouter {

    InviteRebateOrderDao inviteOrderDao;
    BaseOrderDao baseOrderDao;

    @Autowired
    public void setBaseOrderDao(BaseOrderDao baseOrderDao) {
        this.baseOrderDao = baseOrderDao;
    }

    @Autowired
    public void setInviteOrderDao(InviteRebateOrderDao inviteOrderDao) {
        this.inviteOrderDao = inviteOrderDao;
    }

    @VertxRouter(path = "/*", order = -1)
    public void all(RoutingContextChain chain) {
        chain.handler(BodyHandler.create());
    }


    @VertxRouter(path = "\\/orders(?:\\/(?<page>\\d+))?",
            method = "POST",
            mode = VertxRouteType.PathRegex)
    public void listUser(RoutingContextChain chain) {
        chain.blockingHandler(routingContext -> {
            InviteRebateOrderCondition condition = JsonUtils.jsonToObject(routingContext.getBodyAsString(), InviteRebateOrderCondition.class, () -> null);
            if (condition == null) {
                routingContext.response().setStatusCode(404).end("没有数据");
                return;
            }
            String page = routingContext.pathParam("page");
            condition.page(Integer.parseInt(page));
            List<InviteRebateOrderVO> inviteOrders = inviteOrderDao.list(condition);
            routingContext.end(JSON.toJSONString(inviteOrders));
        });
    }

    @VertxRouter(path = "\\/order(?:\\/(?<orderId>\\d+))?",
            method = "POST",
            mode = VertxRouteType.PathRegex)
    public void order(RoutingContextChain chain) {
        chain.blockingHandler(routingContext -> {
            AbstractWechatConfiguration configuration = AbstractWechatConfiguration.getConfig(ServiceMarketConfiguration.class);
            WorkerVerticle.getVertix().eventBus().send(String.format("app[%s]-%s", configuration.getAppID(), "orderFinished"), Long.valueOf(routingContext.pathParam("orderId")));
            routingContext.end();
        });
    }
}
