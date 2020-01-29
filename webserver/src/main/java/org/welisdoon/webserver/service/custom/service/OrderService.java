package org.welisdoon.webserver.service.custom.service;

import io.vertx.core.json.JsonObject;
import org.apache.commons.collections4.MapUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.welisdoon.webserver.common.ApplicationContextProvider;
import org.welisdoon.webserver.common.web.AbstractBaseService;
import org.welisdoon.webserver.service.custom.config.CustomConst;
import org.welisdoon.webserver.service.custom.dao.*;
import org.welisdoon.webserver.service.custom.entity.OrderVO;
import org.welisdoon.webserver.service.custom.entity.PictureVO;
import org.welisdoon.webserver.service.custom.entity.UserVO;
import org.welisdoon.webserver.vertx.annotation.VertxWebApi;

import java.util.List;
import java.util.Map;

@Service
public class OrderService extends AbstractBaseService {

    @Autowired
    OrderDao orderDao;
    @Autowired
    TacheDao tacheDao;
    @Autowired
    UserDao userDao;
    @Autowired
    OperationDao operationDao;
    @Autowired
    PictureDao pictureDao;

    TacheService tacheService;

    @Override
    public void init() {
        tacheService = ApplicationContextProvider.getBean(TacheService.class);
    }

    @Override
    @VertxWebApi
    public Object handle(int exeCode, Map params) {
        Object resultObj = null;
        OrderVO orderVO;
        switch (exeCode) {
            case CustomConst.ADD:
                JsonObject orderVoJson = JsonObject.mapFrom(params);
                List<Integer> jsonArray = (List<Integer>) orderVoJson.remove("pictureIds");

                orderVO = orderVoJson.mapTo(OrderVO.class);
                orderVO.setTacheId(CustomConst.TACHE.FIRST_TACHE.getTacheId())
                        .setOrderState(CustomConst.ORDER.STATE.WAIT_NEXT);
                UserVO userVO = userDao.get(new UserVO().setId(orderVO.getCustId()));
                orderVO.setCustName(userVO.getName())
                        .setCustPhone(StringUtils.isEmpty(orderVO.getCustPhone())
                                ? userVO.getPhone()
                                : orderVO.getCustPhone());
                orderDao.add(orderVO);

                if (jsonArray != null && jsonArray.size() > 0) {
                    for (int i = 0; i < jsonArray.size(); i++) {
                        pictureDao.set(new PictureVO()
                                .setPictrueId(jsonArray.get(i))
                                .setOrderId(orderVO.getOrderId())
                                .setTacheId(orderVO.getTacheId()));
                    }
                }
                resultObj = tacheService.handle(CustomConst.TACHE.GET_WORK_NUMBER, Map.of("userId", orderVO.getCustId()));
                break;
            case CustomConst.DELETE:
                break;
            /*case CustomConst.MODIFY:
                orderVO = mapToObject(params, OrderVO.class);
                resultObj = orderDao.set(orderVO);
                break;*/
            case CustomConst.LIST:
                orderVO = mapToObject(params, OrderVO.class);
                List list = orderDao.listAll(orderVO);
                resultObj = list;
                break;
            case CustomConst.GET:
                orderVO = mapToObject(params, OrderVO.class);
                resultObj = orderDao.get(orderVO);
                break;
            case CustomConst.ORDER.GET_WORK_NUMBER:
                orderVO = mapToObject(params, OrderVO.class);
                resultObj = orderDao.getWorkIngOrderNum(orderVO);
                break;
            case CustomConst.ORDER.APPIONT_WORKER:
                orderVO = new OrderVO()
//                        .setOrderAppointPerson(MapUtils.getString(params, "orderControlPerson"))
                        .setOrderId(MapUtils.getInteger(params, "orderId"));
                orderVO = orderDao.get(orderVO);
                if (orderVO != null) {
                    resultObj = orderDao.set(new OrderVO()
                            .setOrderId(orderVO.getOrderId())
                            .setOrderAppointPerson(MapUtils.getString(params, "orderAppointPerson")));
                }
            default:
                break;

        }
        return resultObj;
    }
}
