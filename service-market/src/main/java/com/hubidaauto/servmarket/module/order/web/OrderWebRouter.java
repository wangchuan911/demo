package com.hubidaauto.servmarket.module.order.web;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.hubidaauto.carservice.wxapp.core.config.CustomWeChatAppConfiguration;
import com.hubidaauto.servmarket.module.log.dao.OrderPayDaoLog;
import com.hubidaauto.servmarket.module.log.entity.OrderPayLogVO;
import com.hubidaauto.servmarket.module.order.entity.OrderCondition;
import com.hubidaauto.servmarket.module.order.model.IBaseOrderService;
import com.hubidaauto.servmarket.module.user.dao.AppUserDao;
import com.hubidaauto.servmarket.module.user.entity.AppUserVO;
import io.vertx.ext.web.handler.BodyHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.welisdoon.web.common.config.AbstractWechatConfiguration;
import org.welisdoon.web.entity.wechat.WeChatPayOrder;
import org.welisdoon.web.entity.wechat.WeChatRefundOrder;
import org.welisdoon.web.entity.wechat.payment.response.PrePayResponseMesseage;
import org.welisdoon.web.vertx.annotation.VertxConfiguration;
import org.welisdoon.web.vertx.annotation.VertxRoutePath;
import org.welisdoon.web.vertx.annotation.VertxRouter;
import org.welisdoon.web.vertx.enums.VertxRouteType;
import org.welisdoon.web.vertx.utils.RoutingContextChain;

import java.sql.Timestamp;
import java.time.LocalDateTime;

/**
 * @Classname OrderWebRouter
 * @Description TODO
 * @Author wang.zhidong
 * @Date 2021/8/13 12:14
 */
@Component
@VertxConfiguration
@ConditionalOnProperty(prefix = "wechat-app-hubida", name = "appID")
@VertxRoutePath("{wechat-app-hubida.path.app}/order")
public class OrderWebRouter {
    AbstractWechatConfiguration customWeChatAppConfiguration;
    AppUserDao appUserDao;
    IBaseOrderService orderService;
    OrderPayDaoLog orderPrePayDaoLog;

    @Autowired
    public void setOrderPrePayDaoLog(OrderPayDaoLog orderPrePayDaoLog) {
        this.orderPrePayDaoLog = orderPrePayDaoLog;
    }

    @Autowired
    public void setAppUserDao(AppUserDao appUserDao) {
        this.appUserDao = appUserDao;
    }

    @Autowired
    public void setAbstractWechatConfiguration(CustomWeChatAppConfiguration customWeChatAppConfiguration) {
        this.customWeChatAppConfiguration = customWeChatAppConfiguration;
    }

    @Autowired
    public void setOrderService(IBaseOrderService orderService) {
        this.orderService = orderService;
    }

    @VertxRouter(path = "/*", order = -1)
    public void all(RoutingContextChain chain) {
        chain.handler(BodyHandler.create());
    }


    @VertxRouter(path = "\\/pay\\/(?<payTarget>\\w+)\\/(?<userId>\\d+)\\/(?<orderId>\\d+)",
            method = "GET",
            mode = VertxRouteType.PathRegex)
    public void payment(RoutingContextChain chain) {
        chain.handler(routingContext -> {
            String path = routingContext.request().path();
            System.out.println(path);
            Long orderId = Long.parseLong(routingContext.pathParam("orderId"));
            OrderPayLogVO log = orderPrePayDaoLog.get(orderId);
            if (log != null) {
                routingContext.response().setStatusCode(500).end(!StringUtils.isEmpty(log.getTransactionId()) ? "定单已支付" : "正在支付，请稍后再试");
                return;
            }
            AppUserVO userVO = appUserDao.get(Long.parseLong(routingContext.pathParam("userId")));
            Long timeStamp = System.currentTimeMillis() / 1000;
            WeChatPayOrder weChatPayOrder = new WeChatPayOrder()
                    .setId(orderId.toString())
                    .setUserId(userVO.getAppId())
                    .setNonce(WeChatRefundOrder.generateNonce())
                    .setTimeStamp(String.valueOf(timeStamp))
                    .setPayClass(this.customWeChatAppConfiguration.getPath()
                            .getPays()
                            .entrySet()
                            .stream()
                            .filter(stringStringEntry -> path.indexOf(stringStringEntry.getValue()) >= 0)
                            .findFirst()
                            .get()
                            .getKey());
            this.customWeChatAppConfiguration.wechatPayRequst(weChatPayOrder).onSuccess(jsonObject -> {
                orderPrePayDaoLog.add(
                        new OrderPayLogVO()
                                .setOrderId(orderId)
                                .setNonce(weChatPayOrder.getNonce())
                                .setTimeStamp(timeStamp)
                                .setSign(jsonObject.getString("sign"))
                                .setPrepayId(jsonObject.getString("prepayId")));
                routingContext.end(jsonObject.toBuffer());
            }).onFailure(throwable -> {
                routingContext.response().setStatusCode(500).end(throwable.getMessage());
            });
        });
    }

    @VertxRouter(path = "\\/repay\\/(?<payTarget>\\w+)\\/(?<userId>\\d+)\\/(?<orderId>\\d+)",
            method = "GET",
            mode = VertxRouteType.PathRegex)
    public void rePayment(RoutingContextChain chain) {
        chain.handler(routingContext -> {
            Long orderId = Long.parseLong(routingContext.pathParam("orderId"));
            OrderPayLogVO logVO = orderPrePayDaoLog.get(orderId);
            if (logVO != null && logVO.getCreateDate().compareTo(Timestamp.valueOf(LocalDateTime.now())) > 0) {
                routingContext.end(JSONObject.toJSONString(logVO.setSign(PrePayResponseMesseage.generateSign(
                        this.customWeChatAppConfiguration.getAppID()
                        , logVO.getNonce()
                        , logVO.getPrepayId()
                        , logVO.getTimeStamp().toString()
                        , this.customWeChatAppConfiguration.getMerchant().getMchKey()))));
                return;
            }
            routingContext.response().setStatusCode(500).end("超过支付时间，单子失效！");
        });
    }


    @VertxRouter(path = "\\/pay\\/(?<payTarget>\\w+)",
            method = "POST",
            mode = VertxRouteType.PathRegex)
    public void paymentCallBack(RoutingContextChain chain) {
        chain.handler(this.customWeChatAppConfiguration::wechatPayResult);
    }

    @VertxRouter(path = "/",
            method = "POST")
    public void order(RoutingContextChain chain) {
        chain.handler(routingContext -> {
            System.out.println(routingContext.getBodyAsString());
            orderService.order(routingContext.getBodyAsString()).onSuccess(value -> {
                routingContext.end(JSONObject.toJSONString(value));
            }).onFailure(throwable -> {
                routingContext.response().setStatusCode(500).end(throwable.getMessage());
            });
        });
    }


    //    @VertxRouter(path = "\\/stream\\/(?<id>\\d+)", method = "GET", mode = VertxRouteType.PathRegex)
    @VertxRouter(path = "/workOrder", method = "POST")
    public void workOrder(RoutingContextChain context) {
        context.handler(routingContext -> {
            try {
                orderService
                        .workOrder(routingContext.getBodyAsString())
                        .onComplete(stringAsyncResult -> {
                            if (stringAsyncResult.succeeded()) {
                                routingContext.end("成功");
                            } else {
                                routingContext.response().setStatusCode(500).end(stringAsyncResult.cause().getMessage());
                            }
                        });
                ;
            } catch (Throwable e) {
                e.printStackTrace();
                routingContext.fail(e.getCause());
            }
        });
    }

    //    @VertxRouter(path = "\\/start\\/(?<id>\\d+)", method = "GET", mode = VertxRouteType.PathRegex)
    @VertxRouter(path = "/start", method = "POST")
    public void start(RoutingContextChain context) {

        context.handler(routingContext -> {
            try {
                orderService
                        .start(routingContext.getBodyAsString())
                        .onComplete(stringAsyncResult -> {
                            if (stringAsyncResult.succeeded()) {
                                routingContext.end("成功");
                            } else {
                                routingContext.response().setStatusCode(500).end(stringAsyncResult.cause().getMessage());
                            }
                        });
                ;
            } catch (Throwable e) {
                e.printStackTrace();
                routingContext.fail(e.getCause());
            }
        });
    }

    @VertxRouter(path = "\\/list\\/(?<page>\\d+)", method = "POST", mode = VertxRouteType.PathRegex)
    public void list(RoutingContextChain context) {
        context.handler(routingContext -> {
            try {
                OrderCondition condition = JSONObject
                        .parseObject(routingContext.getBodyAsString())
                        .toJavaObject(OrderCondition.class);
                condition.page(Integer.parseInt(routingContext.pathParam("page")));
                orderService
                        .list(condition)
                        .onSuccess(orders -> {
                            routingContext.end(JSONArray.toJSONString(orders));
                        })
                        .onFailure(throwable -> {
                            throwable.printStackTrace();
                            routingContext.response().setStatusCode(500).end(throwable.getMessage());
                        });
            } catch (Throwable e) {
                e.printStackTrace();
                routingContext.response().setStatusCode(500).end(e.getMessage());
            }
        });
    }

    @VertxRouter(path = "/getWorkOrder")
    public void getWorkOrder(RoutingContextChain context) {
        context.handler(routingContext -> {
            try {
                orderService
                        .getWorkOrder(routingContext.getBodyAsString())
                        .onComplete(stringAsyncResult -> {
                            if (stringAsyncResult.succeeded()) {
                                routingContext.end(JSONArray.toJSONString(stringAsyncResult.result()));
                            } else {
                                routingContext.response().setStatusCode(500).end(stringAsyncResult.cause().getMessage());
                            }
                        });
            } catch (Throwable e) {
                e.printStackTrace();
                routingContext.response().setStatusCode(500).end(e.getMessage());
            }
        });
    }

    @VertxRouter(path = "/getWorkOrders", method = "POST")
    public void getWorkOrders(RoutingContextChain context) {
        context.handler(routingContext -> {
            try {
                orderService
                        .getWorkOrders(routingContext.getBodyAsString())
                        .onComplete(stringAsyncResult -> {
                            if (stringAsyncResult.succeeded()) {
                                routingContext.end(JSONArray.toJSONString(stringAsyncResult.result()));
                            } else {
                                routingContext.response().setStatusCode(500).end(stringAsyncResult.cause().getMessage());
                            }
                        });
            } catch (Throwable e) {
                e.printStackTrace();
                routingContext.response().setStatusCode(500).end(e.getMessage());
            }
        });
    }

    @VertxRouter(path = "\\/detail\\/(?<orderId>\\d+)", method = "GET", mode = VertxRouteType.PathRegex)
    public void detail(RoutingContextChain context) {
        context.handler(routingContext -> {
            try {
                orderService
                        .orderDetail(Long.parseLong(routingContext.pathParam("orderId")))
                        .onComplete(stringAsyncResult -> {
                            if (stringAsyncResult.succeeded()) {
                                routingContext.end(JSONArray.toJSONString(stringAsyncResult.result()));
                            } else {
                                routingContext.response().setStatusCode(500).end(stringAsyncResult.cause().getMessage());
                            }
                        });
            } catch (Throwable e) {
                e.printStackTrace();
                routingContext.response().setStatusCode(500).end(e.getMessage());
            }
        });
    }

    @VertxRouter(path = "\\/dismiss\\/(?<orderId>\\d+)", method = "GET", mode = VertxRouteType.PathRegex)
    public void dismiss(RoutingContextChain context) {
        context.handler(routingContext -> {
            try {
                orderService
                        .dismiss(Long.parseLong(routingContext.pathParam("orderId")))
                        .onComplete(stringAsyncResult -> {
                            if (stringAsyncResult.succeeded()) {
                                routingContext.end("成功");
                            } else {
                                routingContext.response().setStatusCode(500).end(stringAsyncResult.cause().getMessage());
                            }
                        });
            } catch (Throwable e) {
                e.printStackTrace();
                routingContext.response().setStatusCode(500).end(e.getMessage());
            }
        });
    }

    @VertxRouter(path = "\\/(?<orderId>\\d+)", method = "GET", mode = VertxRouteType.PathRegex)
    public void getOrder(RoutingContextChain context) {
        context.handler(routingContext -> {
            try {
                orderService
                        .get(Long.parseLong(routingContext.pathParam("orderId")))
                        .onComplete(orderResult -> {
                            if (orderResult.succeeded()) {
                                routingContext.end(JSONObject.toJSONString(orderResult.result()));
                            } else {
                                routingContext.response().setStatusCode(500).end(orderResult.cause().getMessage());
                            }
                        });
            } catch (Throwable e) {
                e.printStackTrace();
                routingContext.response().setStatusCode(500).end(e.getMessage());
            }
        });
    }


    @VertxRouter(path = "\\/refund\\/(?<payTarget>\\w+)\\/(?<orderId>\\d+)",
            method = "GET",
            mode = VertxRouteType.PathRegex)
    public void refund(RoutingContextChain chain) {
        chain.handler(routingContext -> {
            String path = routingContext.request().path();
            System.out.println(path);
            WeChatRefundOrder weChatPayOrder = (WeChatRefundOrder) new WeChatRefundOrder()
                    .setId(routingContext.pathParam("orderId"))
                    .setPayClass(this.customWeChatAppConfiguration.getPath()
                            .getRefunds()
                            .entrySet()
                            .stream()
                            .filter(stringStringEntry -> path.indexOf(stringStringEntry.getValue()) >= 0)
                            .findFirst()
                            .get()
                            .getKey())
                    .setNonce(WeChatRefundOrder.generateNonce())
                    .setTimeStamp(String.valueOf(System.currentTimeMillis() / 1000));
            this.customWeChatAppConfiguration.wechatRefundRequest(weChatPayOrder).onSuccess(jsonObject -> {
                routingContext.end(jsonObject.toBuffer());
            }).onFailure(throwable -> {
                routingContext.response().setStatusCode(500).end(throwable.getMessage());
            });
        });
    }

    @VertxRouter(path = "\\/refund\\/(?<payTarget>\\w+)",
            method = "POST",
            mode = VertxRouteType.PathRegex)
    public void refundCallBack(RoutingContextChain chain) {
        chain.handler(this.customWeChatAppConfiguration::weChatRefundCallBack);
    }
}
