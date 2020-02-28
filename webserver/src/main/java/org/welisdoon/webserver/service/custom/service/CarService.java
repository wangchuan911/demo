package org.welisdoon.webserver.service.custom.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.welisdoon.webserver.common.web.AbstractBaseService;
import org.welisdoon.webserver.service.custom.config.CustomConst;
import org.welisdoon.webserver.service.custom.dao.*;
import org.welisdoon.webserver.service.custom.entity.CarVO;
import org.welisdoon.webserver.service.custom.entity.UserVO;
import org.welisdoon.webserver.vertx.annotation.VertxWebApi;

import java.util.Map;

@Service
public class CarService extends AbstractBaseService {
    @Autowired
    OrderDao orderDao;
    @Autowired
    TacheDao tacheDao;
    @Autowired
    UserDao userDao;
    @Autowired
    CarDao carDao;

    @Override
    @VertxWebApi
    public Object handle(int exeCode, Map params) {
        Object resultObj = null;
        CarVO carVO = null;
        switch (exeCode) {
            case CustomConst.LIST:
                carVO = mapToObject(params, CarVO.class);
                resultObj = carDao.list(carVO);
                break;
            case CustomConst.DELETE:
                carVO = mapToObject(params, CarVO.class);
                resultObj = carDao.del(carVO);
                break;
            case CustomConst.ADD:
                Object phone = params.remove("phone");
                carVO = mapToObject(params, CarVO.class);
                carDao.add(carVO);
                if (!StringUtils.isEmpty(phone)) {
                    userDao.set(new UserVO().setId(carVO.getUserId()).setPhone(phone.toString()));
                }
                if (carVO.getCarInfo() == null) {
                    carVO.setCarInfo(carDao.getCarInfo(carVO.getCarModelId()));
                }
                resultObj = carVO;
                break;
            case CustomConst.MODIFY:
                carVO = mapToObject(params, CarVO.class);
                resultObj = carDao.set(carVO);
                break;
            case CustomConst.CAR.GET_MODEL:
                resultObj = carDao.getModel(params);
            default:
                break;
        }
        return resultObj;
    }
}
