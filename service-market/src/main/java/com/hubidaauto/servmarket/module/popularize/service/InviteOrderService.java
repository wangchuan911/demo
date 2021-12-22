package com.hubidaauto.servmarket.module.popularize.service;

import com.hubidaauto.servmarket.module.order.dao.BaseOrderDao;
import com.hubidaauto.servmarket.module.order.entity.OrderVO;
import com.hubidaauto.servmarket.module.popularize.dao.InviteOrderDao;
import com.hubidaauto.servmarket.module.popularize.entity.InviteOrderVO;
import com.hubidaauto.servmarket.module.user.dao.AppUserDao;
import com.hubidaauto.servmarket.module.user.entity.AppUserVO;
import com.hubidaauto.servmarket.weapp.ServiceMarketConfiguration;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.eventbus.MessageConsumer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Service;
import org.welisdoon.web.common.config.AbstractWechatConfiguration;
import org.welisdoon.web.vertx.annotation.VertxConfiguration;
import org.welisdoon.web.vertx.annotation.VertxRegister;
import org.welisdoon.web.vertx.verticle.StandaredVerticle;

import java.util.function.Consumer;

/**
 * @Classname InviteOrderService
 * @Description TODO
 * @Author wang.zhidong
 * @Date 2021/12/22 19:34
 */
@Configuration
public class InviteOrderService {

    BaseOrderDao baseOrderDao;
    InviteOrderDao inviteOrderDao;
    AppUserDao appUserDao;

    @Autowired
    public InviteOrderService(BaseOrderDao b1, InviteOrderDao b2, AppUserDao b3) {
        this.baseOrderDao = b1;
        this.inviteOrderDao = b2;
        this.appUserDao = b3;
    }

    @VertxRegister(StandaredVerticle.class)
    public Consumer<Vertx> createAsyncServiceProxy() {
        return (Consumer<Vertx>) vertx1 -> {
            AbstractWechatConfiguration configuration = AbstractWechatConfiguration.getConfig(ServiceMarketConfiguration.class);
            MessageConsumer<Long> consumer = vertx1.eventBus().consumer(String.format("app[%s]-%s", configuration.getAppID(), "orderFinished"));
            consumer.handler(longMessage -> {
                OrderVO orderVO = baseOrderDao.get(longMessage.body());
                AppUserVO appUserVO = appUserDao.get(orderVO.getCustId());
                if (appUserVO.getInviteUser() == null)
                    return;
                this.inviteOrderDao.add((InviteOrderVO) new InviteOrderVO().setInviteMan(appUserVO.getInviteUser()).setRebate(orderVO.getTotalPrice().intValue()).setId(orderVO.getId()));
            });
        };
    }
}
