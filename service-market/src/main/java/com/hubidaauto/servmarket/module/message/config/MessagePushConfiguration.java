package com.hubidaauto.servmarket.module.message.config;

import com.alibaba.fastjson.JSONObject;
import com.hubidaauto.servmarket.module.message.entity.MessagePushVO;
import com.hubidaauto.servmarket.module.message.model.MessageEvent;
import com.hubidaauto.servmarket.module.order.service.BaseOrderService;
import com.hubidaauto.servmarket.module.order.service.FlowProxyService;
import com.hubidaauto.servmarket.module.user.dao.AppUserDao;
import com.hubidaauto.servmarket.module.workorder.dao.ServiceClassWorkOrderDao;
import com.hubidaauto.servmarket.weapp.ServiceMarketConfiguration;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.eventbus.MessageConsumer;
import io.vertx.ext.web.client.WebClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.welisdoon.web.common.CommonConst;
import org.welisdoon.web.common.config.AbstractWechatConfiguration;
import org.welisdoon.web.common.intf.Self;
import org.welisdoon.web.entity.wechat.push.SubscribeMessage;
import org.welisdoon.web.vertx.annotation.VertxConfiguration;
import org.welisdoon.web.vertx.annotation.VertxRegister;
import org.welisdoon.web.vertx.verticle.WorkerVerticle;

import java.util.Map;
import java.util.function.Consumer;

/**
 * @Classname MessagePushService
 * @Description TODO
 * @Author wang.zhidong
 * @Date 2022/1/7 15:43
 */
@Component
@VertxConfiguration
public class MessagePushConfiguration implements Self<MessagePushConfiguration> {
    final Logger logger = LoggerFactory.getLogger(MessagePushConfiguration.class);

    ServiceClassWorkOrderDao workOrderDao;
    FlowProxyService flowService;
    BaseOrderService baseOrderService;
    AppUserDao appUserDao;

    @Autowired
    public void setFlowService(FlowProxyService flowService) {
        this.flowService = flowService;
    }

    @Autowired
    public void setWorkOrderDao(ServiceClassWorkOrderDao workOrderDao) {
        this.workOrderDao = workOrderDao;
    }

    @Autowired
    public void setBaseOrderService(BaseOrderService baseOrderService) {
        this.baseOrderService = baseOrderService;
    }

    @Autowired
    public void setAppUserDao(AppUserDao appUserDao) {
        this.appUserDao = appUserDao;
    }

    @VertxRegister(WorkerVerticle.class)
    public Consumer<Vertx> event(Vertx vertx) {
        WebClient webClient = WebClient.create(vertx);
        AbstractWechatConfiguration configuration = AbstractWechatConfiguration.getConfig(ServiceMarketConfiguration.class);
        return (Consumer<Vertx>) vertx1 -> {
            MessageConsumer</*Long*/String> consumer = vertx1.eventBus().consumer(String.format("app[%s]-%s", configuration.getAppID(), "workorder_ready"));
            consumer.handler(message -> {
                Object[] bodys = JSONObject.parseObject(message.body(), MessageEvent.class).toBodyString();
                for (Object body : bodys) {
                    if (body == null || !(body instanceof MessagePushVO)) continue;
                    MessagePushVO vo = (MessagePushVO) body;
                    switch (vo.getMode()) {
                        case WechatMiniApp:
                            configuration.getWechatAsyncMeassger()
                                    .post(CommonConst.WecharUrlKeys.SUBSCRIBE_SEND, new SubscribeMessage()
                                            .setPage(configuration.getPath().getAppIndex())
                                            .setTemplateId(vo.getTemplateId())
                                            .addDatas(vo.getParams().entrySet().toArray(Map.Entry[]::new))
                                            .setTouser(vo.getCode()))
                                    .onSuccess(bufferHttpResponse -> {
                                        logger.warn(bufferHttpResponse.bodyAsString());
                                    })
                                    .onFailure(throwable -> logger.error(throwable.getMessage(), throwable));
                            break;
                        case WechatOfficialAccounts:
                            String jsonString = JSONObject.toJSONString(vo);
                            logger.warn(jsonString);
                            webClient.postAbs("https://www.hubidaauto.cn/aczl/p")
                                    .timeout(3000)
                                    .sendBuffer(Buffer.buffer(jsonString))
                                    .onFailure(e -> {
                                        logger.error(e.getMessage(), e);
                                    });
                            break;
                    }
                }
            });
        };
    }
}
