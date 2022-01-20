package com.hubidaauto.servmarket.module.message.config;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.util.TypeUtils;
import com.baomidou.dynamic.datasource.annotation.DS;
import com.hubidaauto.servmarket.common.utils.JsonUtils;
import com.hubidaauto.servmarket.module.message.entity.MagicKey;
import com.hubidaauto.servmarket.module.message.entity.MessagePushVO;
import com.hubidaauto.servmarket.module.message.model.MessageEvent;
import com.hubidaauto.servmarket.module.order.entity.OrderVO;
import com.hubidaauto.servmarket.module.order.service.BaseOrderService;
import com.hubidaauto.servmarket.module.order.service.FlowProxyService;
import com.hubidaauto.servmarket.module.user.dao.AppUserDao;
import com.hubidaauto.servmarket.module.workorder.dao.ServiceClassWorkOrderDao;
import com.hubidaauto.servmarket.module.workorder.entity.ServiceClassWorkOrderVO;
import com.hubidaauto.servmarket.weapp.ServiceMarketConfiguration;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.eventbus.MessageConsumer;
import io.vertx.ext.web.client.WebClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.welisdoon.flow.module.flow.entity.Stream;
import org.welisdoon.web.common.ApplicationContextProvider;
import org.welisdoon.web.common.config.AbstractWechatConfiguration;
import org.welisdoon.web.vertx.annotation.VertxConfiguration;
import org.welisdoon.web.vertx.annotation.VertxRegister;
import org.welisdoon.web.vertx.verticle.WorkerVerticle;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * @Classname MessagePushService
 * @Description TODO
 * @Author wang.zhidong
 * @Date 2022/1/7 15:43
 */
@Component
@VertxConfiguration
public class MessagePushConfiguration {
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
        MessagePushConfiguration messagePushConfiguration = ApplicationContextProvider.getBean(MessagePushConfiguration.class);
        return (Consumer<Vertx>) vertx1 -> {
            MessageConsumer</*Long*/String> consumer = vertx1.eventBus().consumer(String.format("app[%s]-%s", configuration.getAppID(), "workorder_ready"));
            consumer.handler(message -> {
                Object body = JSONObject.parseObject(message.body(), MessageEvent.class).toBodyString()/*messagePushConfiguration.workorderEventBody(message.body())*/;
                if (body == null && !(body instanceof MessagePushVO)) return;
                String jsonString = JSONObject.toJSONString(body);
                logger.warn(jsonString);
                webClient.postAbs("https://www.hubidaauto.cn/aczl/p")
                        .timeout(3000)
                        .sendBuffer(Buffer.buffer(jsonString))
                        .onFailure(e -> {
                            logger.error(e.getMessage(), e);
                        });
            });
        };
    }

    @DS("shop")
    @Transactional(rollbackFor = Throwable.class)
    public String workorderEventBody(Long workorderid) {
        ServiceClassWorkOrderVO workOrderVO = workOrderDao.get(workorderid);
        Stream stream = this.flowService.getStream(workOrderVO.getStreamId());

        if (stream == null || stream.getValueId() == null || stream.getValue() == null) return null;
        JSONObject valueJson = stream.getValue().jsonValue();
        if (valueJson == null || valueJson.size() == 0 || !valueJson.containsKey("tplt")) return null;
        valueJson = valueJson.getJSONObject("tplt");
        String url = valueJson.getString("url");
        if (StringUtils.isEmpty(url)) return null;
        OrderVO orderVO = baseOrderService.get(workOrderVO.getOrderId());
        JSONObject params = new JSONObject(), map = new JSONObject();
//        toLongKeyMap(map, "order", (JSONObject) JSONObject.toJSON(orderVO));
//        toLongKeyMap(map, "workorder", (JSONObject) JSONObject.toJSON(workOrderVO));
        map.put("order", orderVO);
        map.put("workorder", workOrderVO);

        Arrays.stream(url.split("&")).forEach(s -> {
            int i = s.indexOf("=");
            if (i < 0) return;
            String key = s.substring(0, i);
            String valueKey = (i == s.length() - 1) ? "" : s.substring(i + 1);
            MagicKey magicKey = MagicKey.getMagic(valueKey);
//            if (map.containsKey(valueKey = magicKey.getValue(valueKey)))
//                params.put(key, magicKey.format(map.get(valueKey)));
//            else
//                params.put(key, valueKey);
            valueKey = magicKey.getValue(valueKey);
            params.put(key, magicKey.format(JsonUtils.toLongKeyMap(map, valueKey)));
        });
        String jsonString = JSONObject.toJSONString(new MessagePushVO()
                .setCode(appUserDao.get(workOrderVO.getStaffId()).getUnionId())
                .setTemplateId(valueJson.getString("tpid")).setParams(params));
        logger.warn(jsonString);
        return jsonString;
    }




}
