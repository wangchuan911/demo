package com.hubidaauto.servmarket.module.message.entity;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.hubidaauto.servmarket.module.message.model.MessageEvent;
import com.hubidaauto.servmarket.module.order.entity.OrderVO;
import com.hubidaauto.servmarket.module.order.service.BaseOrderService;
import com.hubidaauto.servmarket.module.user.dao.AppUserDao;
import com.hubidaauto.servmarket.module.workorder.entity.WorkOrderVO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.welisdoon.flow.module.flow.entity.FlowValue;
import org.welisdoon.flow.module.flow.entity.Stream;
import org.welisdoon.web.common.ApplicationContextProvider;
import com.hubidaauto.servmarket.module.message.dao.MesseageTemplateDao;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

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
    List<Long> templateIds;

    public WorkOrderReadyEvent(OrderVO orderVO, WorkOrderVO workOrderVO) {
        this.orderVO = orderVO;
        if (workOrderVO.getCreateTime() == null)
            workOrderVO.setCreateTime(Timestamp.valueOf(LocalDateTime.now()));
        this.workOrderVO = workOrderVO;
        if (this.workOrderVO.getStream() != null
                && this.workOrderVO.getStream().getValue() != null && this.workOrderVO.getStream().getValue().getValue() != null) {
            FlowValue.FlowJSONValue valueJson = this.workOrderVO.getStream().getValue().jsonValue();
            JSONArray array = valueJson.getJSONArray("tpltIds");

            if (array != null && array.size() > 0) {
                templateIds = array.toJavaList(Long.class);
            }
        }
    }

    @Override
    public MessagePushVO[] toBodyString() {
        List<MessagePushVO> list = new LinkedList<>();
        Stream stream = workOrderVO.getStream();
        if (stream == null) return null;
        if (templateIds != null) {
            this.orderVO = ApplicationContextProvider.getBean(BaseOrderService.class).get(orderVO.getId());
            for (Long templateId : templateIds) {
                try {
                    MesseageTemplate template = ApplicationContextProvider.getBean(MesseageTemplateDao.class).get(templateId);
                    if (template == null) continue;
                    int index = template.mode.indexOf("@");
                    MessagePushVO.Mode platform = MessagePushVO.Mode.valueOf(template.mode.substring(0, index));
                    MessagePushVO.Reciver reciver = MessagePushVO.Reciver.valueOf(template.mode.substring(index + 1));
                    if (platform == null) continue;
                    MessagePushVO vo = new MessagePushVO()
                            .setTemplateId(template.code).setParams(template.toMap(this.params()))
                            .setMode(platform);
                    switch (reciver) {
                        case Staff:
                            vo.setCode(ApplicationContextProvider.getBean(AppUserDao.class).get(workOrderVO.getStaffId()).getUnionId());
                            break;
                        case Custom:
                            vo.setCode(ApplicationContextProvider.getBean(AppUserDao.class).get(orderVO.getCustId()).getAppId());
                            break;
                    }
                    list.add(vo);
                } catch (Throwable e) {
                    logger.error(e.getMessage(), e);
                }
            }


        }
        return list.toArray(MessagePushVO[]::new);
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

    public List<Long> getTemplateIds() {
        return templateIds;
    }

    public WorkOrderVO getWorkOrderVO() {
        return workOrderVO;
    }
}
