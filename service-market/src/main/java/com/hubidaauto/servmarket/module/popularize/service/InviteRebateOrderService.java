package com.hubidaauto.servmarket.module.popularize.service;

import com.baomidou.dynamic.datasource.annotation.DS;
import com.hubidaauto.servmarket.module.common.dao.AppConfigDao;
import com.hubidaauto.servmarket.module.order.dao.BaseOrderDao;
import com.hubidaauto.servmarket.module.order.entity.OrderVO;
import com.hubidaauto.servmarket.module.order.service.BaseOrderService;
import com.hubidaauto.servmarket.module.popularize.dao.InviteRebateCountDao;
import com.hubidaauto.servmarket.module.popularize.dao.InviteRebateOrderDao;
import com.hubidaauto.servmarket.module.popularize.dao.InviteRebateRegistDao;
import com.hubidaauto.servmarket.module.popularize.entity.InviteRebateCountVO;
import com.hubidaauto.servmarket.module.popularize.entity.InviteRebateOrderCondition;
import com.hubidaauto.servmarket.module.popularize.entity.InviteRebateOrderVO;
import com.hubidaauto.servmarket.module.popularize.entity.InviteRebateRegistVO;
import com.hubidaauto.servmarket.module.popularize.model.IRebate;
import com.hubidaauto.servmarket.module.staff.dao.StaffJobDao;
import com.hubidaauto.servmarket.module.staff.entity.StaffJob;
import com.hubidaauto.servmarket.module.user.dao.AppUserDao;
import com.hubidaauto.servmarket.module.user.entity.AppUserVO;
import com.hubidaauto.servmarket.module.user.entity.UserCondition;
import com.hubidaauto.servmarket.module.user.service.AppUserService;
import com.hubidaauto.servmarket.weapp.ServiceMarketConfiguration;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.MessageConsumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.annotation.Transactional;
import org.welisdoon.web.common.ApplicationContextProvider;
import org.welisdoon.web.common.config.AbstractWechatConfiguration;
import org.welisdoon.web.entity.wechat.WeChatMarketTransferOrder;
import org.welisdoon.web.entity.wechat.WeChatPayOrder;
import org.welisdoon.web.entity.wechat.WeChatRefundOrder;
import org.welisdoon.web.entity.wechat.payment.requset.*;
import org.welisdoon.web.entity.wechat.payment.response.PayBillResponseMesseage;
import org.welisdoon.web.entity.wechat.payment.response.RefundReplyMesseage;
import org.welisdoon.web.service.wechat.intf.IWechatPayHandler;
import org.welisdoon.web.vertx.annotation.VertxConfiguration;
import org.welisdoon.web.vertx.annotation.VertxRegister;
import org.welisdoon.web.vertx.verticle.WorkerVerticle;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
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
@Transactional(rollbackFor = Throwable.class)
public class InviteRebateOrderService implements IWechatPayHandler {
    private static final Logger logger = LoggerFactory.getLogger(InviteRebateOrderService.class);

    BaseOrderDao baseOrderDao;
    InviteRebateOrderDao inviteOrderDao;
    AppUserDao appUserDao;
    AppConfigDao appConfigDao;
    BaseOrderService baseOrderService;
    InviteRebateCountDao inviteRebateCountDao;
    AppUserService appUserService;
    StaffJobDao staffJobDao;
    InviteRebateRegistDao inviteRebateRegistDao;

    @Autowired
    public InviteRebateOrderService(BaseOrderDao b1, InviteRebateOrderDao b2, AppUserDao b3, AppConfigDao b4, BaseOrderService b5, InviteRebateCountDao b6, AppUserService b7, StaffJobDao b8, InviteRebateRegistDao b9) {
        this.baseOrderDao = b1;
        this.inviteOrderDao = b2;
        this.appUserDao = b3;
        this.appConfigDao = b4;
        this.baseOrderService = b5;
        this.inviteRebateCountDao = b6;
        this.appUserService = b7;
        this.staffJobDao = b8;
        this.inviteRebateRegistDao = b9;
    }

    public void start(Long orderId) {
        OrderVO orderVO = baseOrderDao.get(orderId);

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
            inviteRebateCountDao.put(countVO);
        }
    }

    @VertxRegister(WorkerVerticle.class)
    public Consumer<Vertx> createAsyncServiceProxy() {

        return (Consumer<Vertx>) vertx1 -> {
            AbstractWechatConfiguration configuration = AbstractWechatConfiguration.getConfig(ServiceMarketConfiguration.class);
            MessageConsumer<Long> consumer = vertx1.eventBus().consumer(String.format("app[%s]-%s", configuration.getAppID(), "orderFinished"));
            consumer.handler(longMessage -> {
                logger.info("orderFinished");
                ApplicationContextProvider.getBean(InviteRebateOrderService.class).start(longMessage.body());
            });
        };
    }

    @Override
    public Future<PayBillResponseMesseage> payCallBack(PayBillRequsetMesseage payBillRequsetMesseage) {
        return null;
    }

/*    @Override
    public PrePayRequsetMesseage payRequset(WeChatPayOrder weChatPayOrder) {
        return null;
    }

    @Override
    public RefundReplyMesseage refundCallBack(RefundResultMesseage refundResultMesseage) {
        return null;
    }

    @Override
    public RefundRequestMesseage refundRequset(WeChatRefundOrder weChatPayOrder) {
        return null;
    }*/

    @Override
    public Future<MarketTransferRequsetMesseage> marketTransferRequset(WeChatMarketTransferOrder weChatPayOrder) {
        Long userid = Long.valueOf(weChatPayOrder.getUserId());
        InviteRebateCountVO countVO = inviteRebateCountDao.get(userid);
        AppUserVO userVO = appUserDao.get(userid);
        return Future.succeededFuture(new MarketTransferRequsetMesseage()
                .setPartnerTradeNo(String.format("%08dMT%020d", weChatPayOrder.getUserId(), System.currentTimeMillis()))
                .setOpenid(userVO.getAppId())
                .setCheckName("NO_CHECK")
                .setAmount(countVO.getTotalRebate() - countVO.getPaidRebate())
                .setNonceStr(WeChatPayOrder.generateNonce())
                .setDesc("推广费用"));
    }

    public void promoteJoin(UserCondition userCondition, InviteRebateRegistVO registVO) throws Throwable {

        userCondition.setRole(5L);
        appUserService.update(userCondition);
        registVO.setId(userCondition.getId());
        inviteRebateRegistDao.add(registVO);
        staffJobDao.add(new StaffJob().setStaffId(userCondition.getId()).setName("分销资格申请").setRegionId(450000L).setRoleId(5L));
    }
}
