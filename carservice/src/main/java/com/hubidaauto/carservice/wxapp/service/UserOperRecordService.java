package com.hubidaauto.carservice.wxapp.service;

import com.hubidaauto.carservice.wxapp.config.CustomConst;
import com.hubidaauto.carservice.wxapp.dao.UserOperRecordDao;
import com.hubidaauto.carservice.wxapp.entity.OrderVO;
import com.hubidaauto.carservice.wxapp.entity.UserVO;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.welisdoon.webserver.common.web.AbstractBaseService;
import org.welisdoon.webserver.vertx.annotation.VertxWebApi;

import java.util.Map;

@Service
public class UserOperRecordService extends AbstractBaseService<Object> {
    Logger logger = logger(UserOperRecordService.class);
    @Autowired
    UserOperRecordDao userOperRecordDao;

    @VertxWebApi
    public Object handle(int exeCode, Map map) {
        Object resultObj = null;
        switch (exeCode) {
            case CustomConst.USER_RECORD.LOCAL:
                UserVO userVO = mapToObject(map, UserVO.class);
                userOperRecordDao.location(userVO);
                resultObj = userVO;
                break;

        }
        return resultObj;
    }

    public Object handle(int exeCode, Object obj) {
        Object resultObj = null;
        try {
            switch (exeCode) {
                case CustomConst.USER_RECORD.ORDER:
                    OrderVO orderVO = (OrderVO) obj;
                    userOperRecordDao.add(orderVO);
                    resultObj = orderVO;
                    break;

            }
        } catch (Throwable e) {
            logger.error(e.getMessage(), e);
        }
        return resultObj;
    }
}
