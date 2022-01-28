package com.hubidaauto.servmarket.module.message.entity;

import com.alibaba.fastjson.JSONObject;
import com.hubidaauto.servmarket.module.message.model.MessageEvent;
import com.hubidaauto.servmarket.module.order.entity.OrderVO;
import com.hubidaauto.servmarket.module.user.dao.AppUserDao;
import com.hubidaauto.servmarket.module.workorder.entity.WorkOrderVO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.welisdoon.flow.module.flow.entity.Stream;
import org.welisdoon.web.common.ApplicationContextProvider;
import com.hubidaauto.servmarket.module.message.dao.MesseageTemplateDao;

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
    JSONObject templateInfo;

    public WorkOrderReadyEvent(OrderVO orderVO, WorkOrderVO workOrderVO) {
        this.orderVO = orderVO;
        this.workOrderVO = workOrderVO;
        if (this.workOrderVO.getStream() != null
                && this.workOrderVO.getStream().getValue() != null && this.workOrderVO.getStream().getValue().getValue() != null) {
            JSONObject valueJson = this.workOrderVO.getStream().getValue().jsonValue();
            this.templateInfo = valueJson.getJSONObject("tplt");
        }
    }

    @Override
    public MessagePushVO[] toBodyString() {
        List<MessagePushVO> list = new LinkedList<>();
        Stream stream = workOrderVO.getStream();
        if (stream == null) return null;
        if (templateInfo != null && templateInfo.size() > 0) {
            Arrays.stream(MessagePushVO.Mode.values()).forEach(platform -> {
                try {
                    if (templateInfo.containsKey(platform.code)) {
                        MesseageTemplate template = ApplicationContextProvider.getBean(MesseageTemplateDao.class).get(templateInfo.getLong(platform.code));
                        if (template == null) return;
                        MessagePushVO vo = new MessagePushVO()
                                .setTemplateId(template.code).setParams(template.toMap(this.params()))
                                .setMode(platform);
                        switch (platform) {
                            case WechatOfficialAccounts:
                                vo.setCode(ApplicationContextProvider.getBean(AppUserDao.class).get(workOrderVO.getStaffId()).getUnionId());
                                break;
                            case WechatMiniApp:
                                vo.setCode(ApplicationContextProvider.getBean(AppUserDao.class).get(orderVO.getCustId()).getAppId());
                                break;
                        }
                        list.add(vo);
                    }
                } catch (Throwable e) {
                    logger.error(e.getMessage(), e);
                }
            });

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

    public WorkOrderVO getWorkOrderVO() {
        return workOrderVO;
    }

    public JSONObject getTemplateInfo() {
        return templateInfo;
    }
}
