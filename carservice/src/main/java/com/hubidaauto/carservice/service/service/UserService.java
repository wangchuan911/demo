package com.hubidaauto.carservice.service.service;

import com.hubidaauto.carservice.service.config.CustomConst;
import com.hubidaauto.carservice.service.dao.CarDao;
import com.hubidaauto.carservice.service.dao.UserDao;
import com.hubidaauto.carservice.service.entity.CarVO;
import com.hubidaauto.carservice.service.entity.UserVO;
import io.vertx.core.json.JsonObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.welisdoon.webserver.common.ApplicationContextProvider;
import org.welisdoon.webserver.common.web.AbstractBaseService;
import org.welisdoon.webserver.vertx.annotation.VertxWebApi;

import java.util.List;
import java.util.Map;

@Service
public class UserService extends AbstractBaseService<UserVO> {
    @Autowired
    UserDao userDao;
    @Autowired
    CarDao carDao;

    TacheService tacheService;
    OrderService orderService;

    @Override
    public void init() {
        tacheService = ApplicationContextProvider.getBean(TacheService.class);
        orderService = ApplicationContextProvider.getBean(OrderService.class);
    }

    @Override
    @VertxWebApi
    @Transactional(rollbackFor = Throwable.class)
    public Object handle(int exeCode, Map params) {
        Object resultObj = null;
        UserVO userVO = null;
        switch (exeCode) {
            case CustomConst.GET:
                userVO = mapToObject(params, UserVO.class);
                resultObj = userDao.get(userVO);
                break;
            case CustomConst.LIST:
                userVO = mapToObject(params, UserVO.class);
                resultObj = userDao.list(userVO);
                break;
            case CustomConst.USER.GET_WORKERS:
                userVO = mapToObject(params, UserVO.class);
                List<UserVO> userVOS = userDao.list(new UserVO().setMaxRole(CustomConst.ROLE.WOCKER));
                if (!StringUtils.isEmpty(userVO.getId())) {
                    userVOS.add(0, userDao.get(new UserVO().setId(userVO.getId())));
                }
                resultObj = userVOS;
                break;
            case CustomConst.ADD:
                userVO = mapToObject(params, UserVO.class);
                userVO.setRole(CustomConst.ROLE.CUSTOMER);
                resultObj = userDao.add(userVO);
                break;
            case CustomConst.MODIFY:
                userVO = mapToObject(params, UserVO.class);
                userDao.set(userVO);
                resultObj = userVO;
            case CustomConst.USER.AREA_RANGE:
                userVO = mapToObject(params, UserVO.class);
                resultObj = userDao.getWorkAreaRange(userVO);
            default:
                break;
        }
        return resultObj;
    }

    /*登陆初始化*/
    @VertxWebApi
    @Transactional(rollbackFor = Throwable.class)
    public Object login(String userId) {
        return login(new UserVO().setId(userId));
    }

    @Transactional(rollbackFor = Throwable.class)
    public Object login(UserVO userVO) {
        JsonObject jsonObject = new JsonObject();
        if (userVO == null || !StringUtils.isEmpty(userVO.getId())) {
            Object o = handle(CustomConst.GET, Map.of("id", userVO.getId()));
            if (o == null) {
                userVO.setRole(CustomConst.ROLE.GUEST);
                userVO.setName("新用戶");
                jsonObject.put("user", JsonObject.mapFrom(userVO));
                userDao.add(userVO.openData(false));
            } else {
                userVO = (UserVO) o;
                jsonObject.put("user", JsonObject.mapFrom(o));
                switch (userVO.getRole()) {
                    case CustomConst.ROLE.CUSTOMER:
                        o = tacheService.handle(CustomConst.TACHE.GET_WORK_NUMBER, Map.of("userId", userVO.getId()));
                        break;
                    case CustomConst.ROLE.DISTRIBUTOR:
                        o = orderService.handle(CustomConst.ORDER.GET_WORK_NUMBER, Map.of("orderControlPerson", userVO.getId()));
                        break;
                    case CustomConst.ROLE.WOCKER:
                        o = orderService.handle(CustomConst.ORDER.GET_WORK_NUMBER, Map.of("orderAppointPerson", userVO.getId()));
                        break;

                }
                o = o != null ? JsonObject.mapFrom(o) : Map.of("all_nums", 0, "nums", 0);
                jsonObject.put("work", o);
                jsonObject.put("cars", carDao.list(new CarVO().setUserId(userVO.getId()).setDefaultSelected(1)));
                if (!StringUtils.isEmpty(userVO.getSessionKey())) {
                    userDao.set(new UserVO().setId(userVO.getId()).setSessionKey(userVO.getSessionKey()).openData(false));
                }
            }
        }
        return jsonObject;
    }
}
