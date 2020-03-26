package org.welisdoon.webserver.service.custom.service;

import io.vertx.core.json.JsonObject;
import org.apache.commons.collections4.MapUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.welisdoon.webserver.common.ApplicationContextProvider;
import org.welisdoon.webserver.common.encrypt.WXBizMsgCrypt;
import org.welisdoon.webserver.common.web.AbstractBaseService;
import org.welisdoon.webserver.service.custom.config.CustomConst;
import org.welisdoon.webserver.service.custom.dao.*;
import org.welisdoon.webserver.service.custom.entity.OrderVO;
import org.welisdoon.webserver.service.custom.entity.PictureVO;
import org.welisdoon.webserver.service.custom.entity.UserVO;
import org.welisdoon.webserver.vertx.annotation.VertxWebApi;
import org.welisdoon.webserver.vertx.verticle.AbstractCustomVerticle;

import java.util.List;
import java.util.Map;

@Service
@Transactional(rollbackFor = Throwable.class)
public class OrderService extends AbstractBaseService<OrderVO> {

    private static final Logger logger = logger(OrderService.class);

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
    @Autowired
    WXBizMsgCrypt wxBizMsgCrypt;

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

                Map encryptedMap = mapSpliter(params, Map.class, "phoneEncryptedData", "phoneEncryptedIv");

                orderVO = orderVoJson.mapTo(OrderVO.class);
                orderVO.setTacheId(CustomConst.TACHE.FIRST_TACHE.getTacheId())
                        .setOrderState(CustomConst.ORDER.STATE.WAIT_NEXT);
                UserVO userVO = userDao.get(new UserVO().setId(orderVO.getCustId()));

                try {
                    String phoneEncryptedData = MapUtils.getString(encryptedMap, "phoneEncryptedData", null),
                            phoneEncryptedIv = MapUtils.getString(encryptedMap, "phoneEncryptedIv", null);
                    if (!(StringUtils.isEmpty(phoneEncryptedData) || StringUtils.isEmpty(phoneEncryptedIv))) {
                        userVO.openData(false);
                        JsonObject jsonObject = new JsonObject(wxBizMsgCrypt.wxDecrypt(phoneEncryptedData, userVO.getSessionKey(), phoneEncryptedIv));
                        orderVO.setCustPhone(jsonObject.getString("phoneNumber", jsonObject.getString("purePhoneNumber", null)));
                    }
                } catch (Throwable e) {
                    logger.error(e.getMessage(), e);
                }
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

    @Override
    public Object handle(int exeCode, OrderVO orderVO) {
        Object resultObj = null;
        switch (exeCode) {
            case CustomConst.MODIFY:
                orderDao.set(orderVO);
                resultObj = orderVO;
                break;
        }
        return resultObj;
    }

}
