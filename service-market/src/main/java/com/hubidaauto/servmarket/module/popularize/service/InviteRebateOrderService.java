package com.hubidaauto.servmarket.module.popularize.service;

import com.baomidou.dynamic.datasource.annotation.DS;
import com.hubidaauto.servmarket.module.common.dao.AppConfigDao;
import com.hubidaauto.servmarket.module.order.dao.BaseOrderDao;
import com.hubidaauto.servmarket.module.order.entity.OrderVO;
import com.hubidaauto.servmarket.module.order.service.BaseOrderService;
import com.hubidaauto.servmarket.module.popularize.dao.InviteRebateCountDao;
import com.hubidaauto.servmarket.module.popularize.dao.InviteRebateOrderDao;
import com.hubidaauto.servmarket.module.popularize.entity.InviteRebateCountVO;
import com.hubidaauto.servmarket.module.popularize.entity.InviteRebateOrderVO;
import com.hubidaauto.servmarket.module.popularize.model.IRebate;
import com.hubidaauto.servmarket.module.user.dao.AppUserDao;
import com.hubidaauto.servmarket.module.user.entity.AppUserVO;
import com.hubidaauto.servmarket.weapp.ServiceMarketConfiguration;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.MessageConsumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.welisdoon.web.common.config.AbstractWechatConfiguration;
import org.welisdoon.web.vertx.annotation.VertxConfiguration;
import org.welisdoon.web.vertx.annotation.VertxRegister;
import org.welisdoon.web.vertx.verticle.WorkerVerticle;

import java.util.function.Consumer;

/**
 * @Classname InviteOrderService
 * @Description TODO
 * @Author wang.zhidong
 * @Date 2021/12/22 19:34
 */
@Configuration
@VertxConfiguration
@ConditionalOnProperty(prefix = "wechat-app-hubida", name = "appID")
@DS("shop")
public class InviteRebateOrderService {
    private static final Logger logger = LoggerFactory.getLogger(InviteRebateOrderService.class);

    BaseOrderDao baseOrderDao;
    InviteRebateOrderDao inviteOrderDao;
    AppUserDao appUserDao;
    AppConfigDao appConfigDao;
    BaseOrderService baseOrderService;
    InviteRebateCountDao inviteRebateCountDao;

    @Autowired
    public InviteRebateOrderService(BaseOrderDao b1, InviteRebateOrderDao b2, AppUserDao b3, AppConfigDao b4, BaseOrderService b5, InviteRebateCountDao b6) {
        this.baseOrderDao = b1;
        this.inviteOrderDao = b2;
        this.appUserDao = b3;
        this.appConfigDao = b4;
        this.baseOrderService = b5;
        this.inviteRebateCountDao = b6;
    }

    @VertxRegister(WorkerVerticle.class)
    public Consumer<Vertx> createAsyncServiceProxy() {

        return (Consumer<Vertx>) vertx1 -> {
            AbstractWechatConfiguration configuration = AbstractWechatConfiguration.getConfig(ServiceMarketConfiguration.class);
            MessageConsumer<Long> consumer = vertx1.eventBus().consumer(String.format("app[%s]-%s", configuration.getAppID(), "orderFinished"));
            consumer.handler(longMessage -> {
                logger.info("orderFinished");
                OrderVO orderVO = baseOrderDao.get(longMessage.body());

                Object service = BaseOrderService.getOrderService(orderVO.getClassId());
                if (!(service instanceof IRebate)) return;
                AppUserVO appUserVO = appUserDao.get(orderVO.getCustId());
                if (appUserVO.getInviteUser() == null)
                    return;
                InviteRebateOrderVO inviteRebateOrderVO = (InviteRebateOrderVO) new InviteRebateOrderVO().
                        setInviteMan(appUserVO.getInviteUser())
                        .setRebate(((IRebate) service).rebate(orderVO))
                        .setId(orderVO.getId());
                this.inviteOrderDao.add(inviteRebateOrderVO);
                InviteRebateCountVO countVO = inviteRebateCountDao.get(inviteRebateOrderVO.getInviteMan());
                if (countVO == null) {
                    countVO = new InviteRebateCountVO().setUserId(appUserVO.getInviteUser()).setTotalRebate(inviteRebateOrderVO.getRebate());
                    inviteRebateCountDao.add(countVO);
                } else {
                    countVO.setTotalRebate(countVO.getTotalRebate() + inviteRebateOrderVO.getRebate());
                    inviteRebateCountDao.update(countVO);
                }
            });
        };
    }
}
