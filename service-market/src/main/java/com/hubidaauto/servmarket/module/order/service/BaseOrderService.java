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
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.*;

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
                    this.ORDER_CLASSES.put(ApplicationContextProvider
                            .getRealClass(stringObjectEntry.getValue().getClass())
                            .getAnnotation(OrderClass.class).id(), (IOrderService<?, ?>) stringObjectEntry.getValue());
                });
    }

    public OrderVO order(String jsonText) {
        JSONObject jsonObject = JSONObject.parseObject(jsonText);
        IOrderService iOrderService = ORDER_CLASSES.get(jsonObject.getLong(CLASS_ID));
        Type orderConditionClass = this.getIOrderServiceRawType(iOrderService)[0];
        OrderCondition condition = jsonObject.toJavaObject(orderConditionClass);
        return iOrderService.order(condition);
    }

    public void start(String jsonText) {
        JSONObject jsonObject = JSONObject.parseObject(jsonText);
        IOrderService iOrderService = ORDER_CLASSES.get(jsonObject.getLong(CLASS_ID));
        Type orderConditionClass = this.getIOrderServiceRawType(iOrderService)[0];
        OrderCondition condition = jsonObject.toJavaObject(orderConditionClass);
        iOrderService.start(condition);
    }

    public void workOrder(String jsonText) {
        JSONObject jsonObject = JSONObject.parseObject(jsonText);
        IOrderService iOrderService = ORDER_CLASSES.get(jsonObject.getLong(CLASS_ID));
        Type workOrderConditionClass = this.getIOrderServiceRawType(iOrderService)[1];
        WorkOrderCondition condition = jsonObject.toJavaObject(workOrderConditionClass);
        iOrderService.workOrder(condition);
    }

    public List<OrderVO> list(OrderCondition condition) {
        List<OrderVO> list = baseOrderDao.list(condition), list2 = new LinkedList<>();
        IOrderService iOrderService;
        for (OrderVO orderVO : list) {
            iOrderService = ORDER_CLASSES.get(orderVO.getClassId());
            list2.add(iOrderService.get(orderVO.getId()));
        }
        return list2;
    }

    Type[] getIOrderServiceRawType(IOrderService iOrderService) {
        return ((ParameterizedType) Arrays
                .stream(ApplicationContextProvider
                        .getRealClass(iOrderService.getClass())
                        .getGenericInterfaces())
                .filter(type -> {
                    if (type instanceof ParameterizedType) {
                        return ((ParameterizedType) type).getRawType() == IOrderService.class;
                    }
                    return false;
                })
                .findFirst()
                .get())
                .getActualTypeArguments();
    }
}