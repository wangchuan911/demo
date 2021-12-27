package com.hubidaauto.servmarket.module.popularize.web;

import com.alibaba.fastjson.JSON;
import com.hubidaauto.servmarket.common.utils.JsonUtils;
import com.hubidaauto.servmarket.module.common.dao.AppConfigDao;
import com.hubidaauto.servmarket.module.order.dao.BaseOrderDao;
import com.hubidaauto.servmarket.module.popularize.dao.InviteRebateCountDao;
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
//@ConditionalOnProperty(prefix = "wechat-app-hubida", name = "appID")
@VertxRoutePath("{wechat-app-hubida.path.app}/invite")
public class InviteRebateOrderRouter {

    InviteRebateOrderDao inviteOrderDao;
    BaseOrderDao baseOrderDao;
    InviteRebateCountDao inviteRebateCountDao;

    @Autowired
    public void setBaseOrderDao(BaseOrderDao baseOrderDao) {
        this.baseOrderDao = baseOrderDao;
    }

    @Autowired
    public void setInviteOrderDao(InviteRebateOrderDao inviteOrderDao) {
        this.inviteOrderDao = inviteOrderDao;
    }

    @Autowired
    public void setInviteRebateCountDao(InviteRebateCountDao inviteRebateCountDao) {
        this.inviteRebateCountDao = inviteRebateCountDao;
    }

    @VertxRouter(path = "/*", order = -1)
    public void all(RoutingContextChain chain) {
        chain.handler(BodyHandler.create());
    }


    @VertxRouter(path = "\\/orders\\/(?<userId>\\d+)\\/(?<page>\\d+)",
            method = "GET",
            mode = VertxRouteType.PathRegex)
    public void listUser(RoutingContextChain chain) {
        chain.blockingHandler(routingContext -> {
            InviteRebateOrderCondition condition = new InviteRebateOrderCondition();
            condition.setInviteMan(Long.valueOf(routingContext.pathParam("userId")));
            String page = routingContext.pathParam("page");
            condition.page(Integer.parseInt(page));
            try {

                List<InviteRebateOrderVO> inviteOrders = inviteOrderDao.list(condition);
                routingContext.end(JSON.toJSONString(inviteOrders));
            } catch (Throwable e) {
                e.printStackTrace();
                routingContext.response().setStatusCode(404).end("[]");
            }

        });
    }

    @VertxRouter(path = "\\/order\\/(?<orderId>\\d+)",
            method = "POST",
            mode = VertxRouteType.PathRegex)
    public void order(RoutingContextChain chain) {
        chain.blockingHandler(routingContext -> {
            AbstractWechatConfiguration configuration = AbstractWechatConfiguration.getConfig(ServiceMarketConfiguration.class);
            WorkerVerticle.pool().getOne().eventBus().send(String.format("app[%s]-%s", configuration.getAppID(), "orderFinished"), Long.valueOf(routingContext.pathParam("orderId")));
            routingContext.end();
        });
    }

    @VertxRouter(path = "\\/count\\/(?<userId>\\d+)",
            method = "POST",
            mode = VertxRouteType.PathRegex)
    public void count(RoutingContextChain chain) {
        chain.blockingHandler(routingContext -> {
            routingContext.end(JSON.toJSONString(inviteOrderDao.get(Long.valueOf(routingContext.pathParam("userId")))));
        });
    }
}
