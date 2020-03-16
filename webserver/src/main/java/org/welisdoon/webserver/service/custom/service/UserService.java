package org.welisdoon.webserver.service.custom.service;

import io.vertx.core.json.JsonObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.welisdoon.webserver.common.ApplicationContextProvider;
import org.welisdoon.webserver.common.web.AbstractBaseService;
import org.welisdoon.webserver.service.custom.config.CustomConst;
import org.welisdoon.webserver.service.custom.dao.CarDao;
import org.welisdoon.webserver.service.custom.dao.UserDao;
import org.welisdoon.webserver.service.custom.entity.CarVO;
import org.welisdoon.webserver.service.custom.entity.UserVO;
import org.welisdoon.webserver.vertx.annotation.VertxWebApi;

import java.util.List;
import java.util.Map;

@Service
public class UserService extends AbstractBaseService {
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
                List<UserVO> userVOS = userDao.list(new UserVO().setRole(CustomConst.ROLE.WOCKER));
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
    public Object login(String userId) {
        return login(userId, null);
    }
    public Object login(String userId,String sessionKey) {
        JsonObject jsonObject = new JsonObject();
        if (!StringUtils.isEmpty(userId)) {
            Object o = handle(CustomConst.GET, Map.of("id", userId));
            UserVO userVO;
            if (o == null) {
                userVO = new UserVO().setId(userId);
                userVO.setRole(CustomConst.ROLE.GUEST);
                userVO.setName("新用戶");
                jsonObject.put("user", JsonObject.mapFrom(userVO));
                userVO.setSessionKey(sessionKey);
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
                jsonObject.put("cars", carDao.list(new CarVO().setUserId(userId).setDefaultSelected(1)));
                if (!StringUtils.isEmpty(sessionKey)) {
                    userDao.set(new UserVO().setId(userVO.getId()).setSessionKey(sessionKey).openData(false));
                }
            }
        }
        return jsonObject;
    }
}
