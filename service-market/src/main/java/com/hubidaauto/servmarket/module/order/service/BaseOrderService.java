package com.hubidaauto.servmarket.module.order.service;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.dynamic.datasource.annotation.DS;
import com.hubidaauto.servmarket.module.order.annotation.OrderClass;
import com.hubidaauto.servmarket.module.order.dao.BaseOrderDao;
import com.hubidaauto.servmarket.module.order.entity.OrderCondition;
import com.hubidaauto.servmarket.module.order.entity.OrderVO;
import com.hubidaauto.servmarket.module.workorder.entity.WorkOrderCondition;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.welisdoon.web.common.ApplicationContextProvider;

import javax.annotation.PostConstruct;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * @Classname BaseOrderService
 * @Description TODO
 * @Author wang.zhidong
 * @Date 2021/8/28 12:43
 */
@DS("shop")
@Service
@Transactional(rollbackFor = Throwable.class)
public class BaseOrderService {

    Map<Long, IOrderService<?, ?>> ORDER_CLASSES = new HashMap<>();
    static String CLASS_ID = "classId", ID = "id";

    BaseOrderDao baseOrderDao;

    @Autowired
    public void setBaseOrderDao(BaseOrderDao baseOrderDao) {
        this.baseOrderDao = baseOrderDao;
    }

    @PostConstruct
    public void initOrderClass() {
        ApplicationContextProvider
                .getApplicationContext()
                .getBeansWithAnnotation(OrderClass.class)
                .entrySet()
                .stream()
                .forEach(stringObjectEntry -> {
                    Arrays.stream(ApplicationContextProvider
                            .getRealClass(stringObjectEntry.getValue().getClass())
                            .getAnnotation(OrderClass.class).id()).forEach(value -> {
                        this.ORDER_CLASSES.put(value, (IOrderService<?, ?>) stringObjectEntry.getValue());
                    });
                });
    }

    public OrderVO order(String jsonText) {
        JSONObject jsonObject = JSONObject.parseObject(jsonText);
        IOrderService iOrderService = ORDER_CLASSES.get(jsonObject.getLong(CLASS_ID));
        OrderClass orderClass = IOrderService.getClassMeta(iOrderService.getClass());
        OrderCondition condition = jsonObject.toJavaObject(orderClass.orderConditionClass());
        return iOrderService.order(condition);
    }

    public void start(String jsonText) {
        JSONObject jsonObject = JSONObject.parseObject(jsonText);
        IOrderService iOrderService = ORDER_CLASSES.get(jsonObject.getLong(CLASS_ID));
        OrderClass orderClass = IOrderService.getClassMeta(iOrderService.getClass());
        OrderCondition condition = jsonObject.toJavaObject(orderClass.orderConditionClass());
        iOrderService.start(condition);
    }

    public void workOrder(String jsonText) {
        JSONObject jsonObject = JSONObject.parseObject(jsonText);
        IOrderService iOrderService = ORDER_CLASSES.get(jsonObject.getLong(CLASS_ID));
        OrderClass orderClass = IOrderService.getClassMeta(iOrderService.getClass());
        WorkOrderCondition condition = jsonObject.toJavaObject(orderClass.workOrderConditionClass());
        iOrderService.workOrder(condition);
    }

}
