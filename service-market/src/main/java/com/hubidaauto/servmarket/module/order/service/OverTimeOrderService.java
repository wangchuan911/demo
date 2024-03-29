package com.hubidaauto.servmarket.module.order.service;

import com.baomidou.dynamic.datasource.annotation.DS;
import com.hubidaauto.servmarket.module.flow.enums.OrderStatus;
import com.hubidaauto.servmarket.module.flow.enums.ServiceContent;
import com.hubidaauto.servmarket.module.goods.dao.ItemDao;
import com.hubidaauto.servmarket.module.goods.dao.ItemTypeDao;
import com.hubidaauto.servmarket.module.log.dao.OrderPayDaoLog;
import com.hubidaauto.servmarket.module.log.entity.OrderPayLogVO;
import com.hubidaauto.servmarket.module.order.annotation.OrderClass;
import com.hubidaauto.servmarket.module.order.dao.BaseOrderDao;
import com.hubidaauto.servmarket.module.order.dao.OverTimeOrderDao;
import com.hubidaauto.servmarket.module.order.entity.*;
import com.hubidaauto.servmarket.module.order.model.IOrderService;
import com.hubidaauto.servmarket.module.order.model.IOverTimeOperationable;
import com.hubidaauto.servmarket.module.popularize.service.InviteRebateOrderService;
import com.hubidaauto.servmarket.module.user.dao.AppUserDao;
import com.hubidaauto.servmarket.module.user.entity.AppUserVO;
import com.hubidaauto.servmarket.module.user.entity.UserCondition;
import com.hubidaauto.servmarket.module.workorder.entity.WorkOrderCondition;
import com.hubidaauto.servmarket.module.workorder.entity.WorkOrderVO;
import com.hubidaauto.servmarket.weapp.ServiceMarketConfiguration;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.MessageConsumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
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
import org.welisdoon.web.vertx.annotation.VertxRegister;
import org.welisdoon.web.vertx.verticle.WorkerVerticle;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.function.Consumer;

/**
 * @Classname OverTimeOrder
 * @Description TODO
 * @Author wang.zhidong
 * @Date 2021/11/29 23:22
 */
@Service
@DS("shop")
@Transactional(rollbackFor = Throwable.class)
@OrderClass(
        id = 1002L)
public class OverTimeOrderService implements IWechatPayHandler, IOrderService<OverTimeOrderCondtion, WorkOrderCondition> {
    AppUserDao appUserDao;
    BaseOrderDao baseOrderDao;
    OrderPayDaoLog orderPrePayDaoLog;
    OverTimeOrderDao orderDao;
    BaseOrderService orderService;
    ItemTypeDao itemTypeDao;
    ItemDao itemDao;

    private static final Logger logger = LoggerFactory.getLogger(OverTimeOrderService.class);

    @Autowired
    public OverTimeOrderService(ItemDao itemDao, ItemTypeDao itemTypeDao, BaseOrderService orderService, AppUserDao appUserDao, BaseOrderDao baseOrderDao, OrderPayDaoLog orderPrePayDaoLog, OverTimeOrderDao orderDao) {
        this.appUserDao = appUserDao;
        this.baseOrderDao = baseOrderDao;
        this.orderPrePayDaoLog = orderPrePayDaoLog;
        this.orderDao = orderDao;
        this.orderService = orderService;
        this.itemTypeDao = itemTypeDao;
        this.itemDao = itemDao;
    }

    @Override
    public Future<PayBillResponseMesseage> payOnCallBack(PayBillRequsetMesseage payBillRequsetMesseage) {
        OrderVO orderVO;

        OrderCondition<OrderVO> condition = new OrderCondition<>();
        condition.setCode(payBillRequsetMesseage.getOutTradeNo());
        AppUserVO userVO = appUserDao.find(new UserCondition().setOpenId(payBillRequsetMesseage.getOpenId()));
        condition.setCustId(userVO.getId());
        orderVO = baseOrderDao.find(condition);


        PayBillResponseMesseage payBillResponseMesseage = new PayBillResponseMesseage();
        if (orderVO != null) {
            orderPrePayDaoLog.put(new OrderPayLogVO().setOrderId(orderVO.getId()).setTransactionId(payBillRequsetMesseage.getTransactionId()));
            payBillResponseMesseage.ok();
            this.start((OverTimeOrderCondtion) new OverTimeOrderCondtion().setId(orderVO.getId()));
        } else {
            payBillResponseMesseage.fail("定单不存在");
        }
        return Future.succeededFuture(payBillResponseMesseage);
    }

    @Override
    public Future<PrePayRequsetMesseage> payOnRequest(WeChatPayOrder weChatPayOrder) {
        OrderVO orderVO = baseOrderDao.get(Long.parseLong(weChatPayOrder.getId()));
        AbstractWechatConfiguration customWeChatAppConfiguration = AbstractWechatConfiguration.getConfig(ServiceMarketConfiguration.class);
        PrePayRequsetMesseage messeage = new PrePayRequsetMesseage()
                .setBody(String.format("%s-服务加时费用结算:\n定单编号：%s\n金额%s", customWeChatAppConfiguration.getAppName(), orderVO.getCode(), OrderUtils.priceFormat(orderVO.getPrice().intValue(), ' ', true, false)))
                .setOutTradeNo(orderVO.getCode())
                .setTotalFee(orderVO.getPrice().intValue())
                .setOpenid(weChatPayOrder.getUserId());
        return Future.succeededFuture(messeage);
    }


    @Override
    public void start(OverTimeOrderCondtion condition) {

        OverTimeOrderVO orderVO = orderDao.get(condition.getId());
        OrderVO relaOrder = baseOrderDao.get(orderVO.getRelaOrderId());
        Object service = BaseOrderService.ORDER_CLASSES.get(relaOrder.getClassId());
        if (!IOverTimeOperationable.class.isAssignableFrom(ApplicationContextProvider.getRealClass(service.getClass()))) {
            throw new RuntimeException(String.format("该类型[%s]单子不允许加时", relaOrder.getClassId()));
        }
        ((IOverTimeOperationable) service).overtime(orderVO);
        orderVO.setStatusId(OrderStatus.COMPLETE.statusId());
        baseOrderDao.put(orderVO);
    }

    @Override
    public OrderVO order(OverTimeOrderCondtion condition) {
        System.out.println(Thread.currentThread());
        OverTimeOrderVO orderVO = new OverTimeOrderVO(condition.getForm());
        OrderVO relaOrder = baseOrderDao.get(condition.getForm().getRelaOrderId());
        Object service = BaseOrderService.ORDER_CLASSES.get(relaOrder.getClassId());
        if (!IOverTimeOperationable.class.isAssignableFrom(ApplicationContextProvider.getRealClass(service.getClass()))) {
            throw new RuntimeException(String.format("该类型[%s]单子不允许加时", relaOrder.getClassId()));
        }

        orderVO.setId(condition.getId());
        orderVO.setClassId(condition.getClassId());
        orderVO.setRelaOrderId(condition.getForm().getRelaOrderId());


        orderVO.setCustId(relaOrder.getCustId());
        orderVO.setFlowId(-1L);
        orderVO.setStatusId(OrderStatus.PRE_PAY.statusId());
        orderVO.setDesc("增加服务时间");
        orderVO.setRegionId(relaOrder.getRegionId());
        baseOrderDao.add(orderVO);
        orderDao.add(orderVO);
        orderVO.setCode(OrderVO.generateCode(orderVO));
        baseOrderDao.put(orderVO);
        return orderVO;
    }

    @Override
    public void workOrder(WorkOrderCondition workOrderCondition) {
        UNSUPPORT_METHOD();
    }

    @Override
    public OrderVO getOrder(Long orderId) {
        OverTimeOrderVO orderVO = orderDao.get(orderId);
        orderVO.setItemType(itemTypeDao.get(orderVO.getItemTypeId()));
        orderVO.setItem(itemDao.get(orderVO.getItemType().getItemId()));
        return orderVO;
    }

    @Override
    public WorkOrderVO getWorkOrder(WorkOrderCondition workOrderCondition) {
        UNSUPPORT_METHOD();
        return null;
    }

    @Override
    public List<WorkOrderVO> getWorkOrders(WorkOrderCondition workOrderCondition) {
        UNSUPPORT_METHOD();
        return null;
    }


    @Override
    public List<ServiceContent> getServices(OrderVO orderVO) {
        return null;
    }

    @Override
    public List<DetailVO> orderDetail(Long orderId) {
        UNSUPPORT_METHOD();
        return null;
    }

    @Override
    public void dismiss(Long orderId) {
        if (orderDao.get(orderId) != null) {
            orderDao.delete(orderId);
        } else {
            for (OverTimeOrderVO overTimeOrderVO : orderDao.list(new OverTimeOrderCondtion().setRelaOrderId(orderId))) {
                orderDao.delete(overTimeOrderVO.getId());
            }
        }
    }

    @Override
    public void modifyOrder(OverTimeOrderCondtion condition) {
        UNSUPPORT_METHOD();
    }


    static void UNSUPPORT_METHOD() {
        throw new RuntimeException("不支持的方法");
    }

    @VertxRegister(WorkerVerticle.class)
    public Consumer<Vertx> message() {

        return (Consumer<Vertx>) vertx1 -> {
            AbstractWechatConfiguration configuration = AbstractWechatConfiguration.getConfig(ServiceMarketConfiguration.class);
            MessageConsumer<Long> consumer = vertx1.eventBus().consumer(String.format("app[%s]-%s", configuration.getAppID(), "orderDestroy"));
            consumer.handler(longMessage -> {
                logger.info("orderDestroy");
                this.dismiss(longMessage.body());
            });
        };
    }
}
