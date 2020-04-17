package com.hubidaauto.carservice.wxapp.service;

import com.hubidaauto.carservice.wxapp.config.CustomConst;
import com.hubidaauto.carservice.wxapp.dao.CarDao;
import com.hubidaauto.carservice.wxapp.dao.OrderDao;
import com.hubidaauto.carservice.wxapp.dao.TacheDao;
import com.hubidaauto.carservice.wxapp.dao.UserDao;
import com.hubidaauto.carservice.wxapp.entity.CarVO;
import com.hubidaauto.carservice.wxapp.entity.UserVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.welisdoon.webserver.common.web.AbstractBaseService;
import org.welisdoon.webserver.vertx.annotation.VertxWebApi;

import java.util.Map;

@Service
@Transactional(rollbackFor = Throwable.class)
public class CarService extends AbstractBaseService<CarVO> {
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
