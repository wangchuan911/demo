package com.hubidaauto.servmarket.module.message.entity;

import com.alibaba.fastjson.JSONObject;
import com.hubidaauto.servmarket.module.message.config.MessagePushConfiguration;
import com.hubidaauto.servmarket.module.message.model.EventMessageBody;
import com.hubidaauto.servmarket.module.order.entity.OrderVO;
import com.hubidaauto.servmarket.module.user.dao.AppUserDao;
import com.hubidaauto.servmarket.module.workorder.entity.ServiceClassWorkOrderVO;
import com.hubidaauto.servmarket.module.workorder.entity.WorkOrderVO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;
import org.welisdoon.flow.module.flow.entity.Stream;
import org.welisdoon.web.common.ApplicationContextProvider;
import org.welisdoon.web.vertx.verticle.AbstractWebVerticle;

import java.util.Arrays;

/**
 * @Classname WorkOrderReadyEvent
 * @Description TODO
 * @Author wang.zhidong
 * @Date 2022/1/8 02:14
 */
public class WorkOrderReadyEvent implements EventMessageBody {

    private static final Logger logger = LoggerFactory.getLogger(WorkOrderReadyEvent.class);
    OrderVO orderVO;
    WorkOrderVO workOrderVO;
    JSONObject templateInfo;

    public WorkOrderReadyEvent(OrderVO orderVO, WorkOrderVO workOrderVO, JSONObject templateInfo) {
        this.orderVO = orderVO;
        this.workOrderVO = workOrderVO;
        this.templateInfo = templateInfo;
    }

    @Override
    public String toBodyString() {
        Stream stream = workOrderVO.getStream();
        if (stream == null || stream.getValueId() == null || stream.getValue() == null) return null;
        if (templateInfo == null || templateInfo.size() == 0) return null;
        String url = templateInfo.getString("url");
        if (StringUtils.isEmpty(url)) return null;
        JSONObject params = new JSONObject(), map = new JSONObject();
        map.put("order", orderVO);
        map.put("workorder", workOrderVO);

        Arrays.stream(url.split("&")).forEach(s -> {
            int i = s.indexOf("=");
            if (i < 0) return;
            String key = s.substring(0, i);
            String valueKey = (i == s.length() - 1) ? "" : s.substring(i + 1);
            MagicKey magicKey = EventMessageBody.getMagic(valueKey);
            /*if (map.containsKey(valueKey = magicKey.getValue(valueKey)))
                params.put(key, magicKey.format(map.get(valueKey)));
            else
                params.put(key, valueKey);*/
            valueKey = magicKey.getValue(valueKey);
            params.put(key, magicKey.format(EventMessageBody.toLongKeyMap(map, valueKey)));
        });
        String jsonString = JSONObject.toJSONString(new MessagePushVO()
                .setCode(ApplicationContextProvider.getBean(AppUserDao.class).get(workOrderVO.getStaffId()).getUnionId())
                .setTemplateId(templateInfo.getString("tpid")).setParams(params));
        logger.warn(jsonString);
        return jsonString;
    }

    public OrderVO getOrderVO() {
        return orderVO;
    }

    public WorkOrderVO getWorkOrderVO() {
        return workOrderVO;
    }

    public JSONObject getTemplateInfo() {
        return templateInfo;
    }
}
