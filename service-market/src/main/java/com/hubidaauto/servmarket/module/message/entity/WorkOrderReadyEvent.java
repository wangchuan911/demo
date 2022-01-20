package com.hubidaauto.servmarket.module.message.entity;

import com.alibaba.fastjson.JSONObject;
import com.hubidaauto.servmarket.module.message.model.MessageEvent;
import com.hubidaauto.servmarket.module.order.entity.OrderVO;
import com.hubidaauto.servmarket.module.user.dao.AppUserDao;
import com.hubidaauto.servmarket.module.workorder.entity.WorkOrderVO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;
import org.welisdoon.flow.module.flow.entity.Stream;
import org.welisdoon.web.common.ApplicationContextProvider;
import com.hubidaauto.servmarket.module.message.dao.MesseageTemplateDao;

import java.util.Arrays;

/**
 * @Classname WorkOrderReadyEvent
 * @Description TODO
 * @Author wang.zhidong
 * @Date 2022/1/8 02:14
 */
public class WorkOrderReadyEvent implements MessageEvent<MessagePushVO> {

    private static final Logger logger = LoggerFactory.getLogger(WorkOrderReadyEvent.class);
    OrderVO orderVO;
    WorkOrderVO workOrderVO;
    JSONObject templateInfo;
    Long templateId;

    public WorkOrderReadyEvent(OrderVO orderVO, WorkOrderVO workOrderVO) {
        this.orderVO = orderVO;
        this.workOrderVO = workOrderVO;
        if (this.workOrderVO.getStream() != null
                && this.workOrderVO.getStream().getValue() != null && this.workOrderVO.getStream().getValue().getValue() != null) {
            JSONObject valueJson = this.workOrderVO.getStream().getValue().jsonValue();
            this.templateInfo = valueJson.getJSONObject("tplt");
            this.templateId = valueJson.getLong("tpltId");
        }
    }

    @Override
    public MessagePushVO toBodyString() {
        Stream stream = workOrderVO.getStream();
        if (stream == null) return null;
        if (templateInfo != null && templateInfo.containsKey("url")) {
            String url = templateInfo.getString("url");
            JSONObject params = new JSONObject(), map = this.params();
            Arrays.stream(url.split("&")).forEach(s -> {
                int i = s.indexOf("=");
                if (i < 0) return;
                String key = s.substring(0, i);
                String valueKey = (i == s.length() - 1) ? "" : s.substring(i + 1);
                MagicKey magicKey = MessageEvent.getMagic(valueKey);
            /*if (map.containsKey(valueKey = magicKey.getValue(valueKey)))
                params.put(key, magicKey.format(map.get(valueKey)));
            else
                params.put(key, valueKey);*/
                valueKey = magicKey.getValue(valueKey);
                params.put(key, magicKey.format(MessageEvent.toLongKeyMap(map, valueKey)));
            });
            return new MessagePushVO()
                    .setCode(ApplicationContextProvider.getBean(AppUserDao.class).get(workOrderVO.getStaffId()).getUnionId())
                    .setTemplateId(templateInfo.getString("tpid")).setParams(params);
        } else if (templateId != null) {
            MesseageTemplate template = ApplicationContextProvider.getBean(MesseageTemplateDao.class).get(templateId);
            if (template == null) return null;
            return new MessagePushVO()
                    .setCode(ApplicationContextProvider.getBean(AppUserDao.class).get(workOrderVO.getStaffId()).getUnionId())
                    .setTemplateId(template.code).setParams(template.toMap(this.params()));
        }
        return null;
    }

    protected JSONObject params() {
        JSONObject map = new JSONObject();
        map.put("order", orderVO);
        map.put("workorder", workOrderVO);
        return map;
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
