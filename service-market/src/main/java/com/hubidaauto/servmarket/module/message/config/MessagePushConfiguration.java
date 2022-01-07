package com.hubidaauto.servmarket.module.message.config;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.util.TypeUtils;
import com.baomidou.dynamic.datasource.annotation.DS;
import com.hubidaauto.servmarket.module.message.entity.MessagePushVO;
import com.hubidaauto.servmarket.module.message.entity.WorkOrderReadyEvent;
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
import org.springframework.context.annotation.Configuration;
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
import java.util.HashMap;
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
                String body = JSONObject.parseObject(message.body(), WorkOrderReadyEvent.class).toBodyString()/*messagePushConfiguration.workorderEventBody(message.body())*/;
                if (StringUtils.isEmpty(body)) return;
                webClient.postAbs("https://www.hubidaauto.cn/aczl/p")
                        .sendBuffer(Buffer.buffer(body))
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
            MagicKey magicKey = getMagic(valueKey);
//            if (map.containsKey(valueKey = magicKey.getValue(valueKey)))
//                params.put(key, magicKey.format(map.get(valueKey)));
//            else
//                params.put(key, valueKey);
            valueKey = magicKey.getValue(valueKey);
            params.put(key, magicKey.format(toLongKeyMap(map, valueKey)));
        });
        String jsonString = JSONObject.toJSONString(new MessagePushVO()
                .setCode(appUserDao.get(workOrderVO.getStaffId()).getUnionId())
                .setTemplateId(valueJson.getString("tpid")).setParams(params));
        logger.warn(jsonString);
        return jsonString;
    }

    MagicKey getMagic(String value) {
        return MagicKey.getInstance(value);
    }

    enum MagicKey {
        DATE("@date@", (o) -> {
            if (o == null) return "";
            Date date = TypeUtils.castToDate(o);
            return new SimpleDateFormat("yyyy年MM月dd日HH时mm分").format(date);
        }), MONEY("@money@", (o) -> {
            if (o == null) return "0.00元";
            StringBuilder sb = new StringBuilder(o.toString());
            sb.insert(sb.length() - 2, ".");
            return (o != null ? sb.toString() : "0.00") + "元";
        }), NONE("", (o) ->
                o != null ? o.toString() : ""
        );
        String key;
        Function<Object, String> function;

        MagicKey(String key, Function<Object, String> function) {
            this.key = key;
            this.function = function;
        }

        public String getValue(String key) {
            if (StringUtils.isEmpty(this.key)) return key;
            return key.substring(this.key.length());
        }

        public String format(Object object) {
            return function.apply(object);
        }

        static MagicKey getInstance(String keyWord) {
            return Arrays.stream(values()).filter(magicKey -> magicKey != NONE && keyWord.startsWith(magicKey.key)).findFirst().orElse(NONE);
        }
    }

    void toLongKeyMap(Map<String, Object> map, String key, JSONObject jsonObject) {
        Object object, json;
        for (String s : jsonObject.keySet()) {
            object = jsonObject.get(s);
            if (object == null) continue;
            json = JSON.toJSON(object);
            if (json instanceof JSONObject) {
                toLongKeyMap(map, String.format("%s.%s", key, s), (JSONObject) json);
            } else if (json instanceof JSONArray) {
                toLongKeyMap(map, String.format("%s.%s", key, s), (JSONArray) json);
            } else if (json == object) {
                map.put(String.format("%s.%s", key, s), json);
            }
        }
    }

    void toLongKeyMap(Map<String, Object> map, String key, JSONArray jsonArray) {
        Object object, json;
        for (int i = 0, len = jsonArray.size(); i < len; i++) {
            object = jsonArray.get(i);
            if (object == null) continue;
            json = JSON.toJSON(object);
            if (json instanceof JSONObject) {
                toLongKeyMap(map, String.format("%[%d]", key, i), (JSONObject) json);
            } else if (json instanceof JSONArray) {
                toLongKeyMap(map, String.format("%s[%d]", key, i), (JSONArray) json);
            } else if (json == object) {
                map.put(String.format("%s[%d]", key, i), json);
            }
        }
    }

    Object toLongKeyMap(JSONObject map, String longKey) {
        int i = longKey.indexOf(".");
        Object object, json;
        String curKey = i < 0 ? longKey : longKey.substring(0, i);
        object = map.get(curKey);
        if (object == null) return null;
        json = JSON.toJSON(object);
        if (json instanceof JSONObject) {
            return toLongKeyMap((JSONObject) json, longKey.substring(i + 1));
        } else if (json instanceof JSONArray) {
            return toLongKeyMap((JSONArray) json, longKey.substring(i + 1));
        } else if (json == object) {
            return object;
        }
        return null;
    }

    Object toLongKeyMap(JSONArray arr, String longKey) {
        int i = longKey.indexOf(".");
        Object object, json;
        String curKey = i < 0 ? longKey : longKey.substring(0, i);
        int index = Integer.valueOf(curKey.substring(1, curKey.length() - 1));
        if (index >= arr.size()) return null;
        object = arr.get(index);
        if (object == null) return null;
        json = JSON.toJSON(object);
        if (json instanceof JSONObject) {
            return toLongKeyMap((JSONObject) json, longKey.substring(i + 1));
        } else if (json instanceof JSONArray) {
            return toLongKeyMap((JSONArray) json, longKey.substring(i + 1));
        } else if (json == object) {
            return object;
        }
        return null;
    }
}
