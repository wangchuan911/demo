package com.hubidaauto.carservice.wxapp.increment.service;

import com.hubidaauto.carservice.wxapp.core.config.CustomConst;
import com.hubidaauto.carservice.wxapp.core.entity.OrderVO;
import com.hubidaauto.carservice.wxapp.core.entity.UserVO;
import com.hubidaauto.carservice.wxapp.increment.dao.UserOperRecordDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.welisdoon.web.common.web.AbstractBaseService;
import org.welisdoon.web.vertx.annotation.VertxWebApi;

import java.util.Map;

@Service
public class UserOperRecordService extends AbstractBaseService<Object> {
//    Logger logger = logger(UserOperRecordService.class);
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
