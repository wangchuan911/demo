package com.hubidaauto.carservice.wxapp.dao.increment;

import com.hubidaauto.carservice.wxapp.entity.OrderVO;
import com.hubidaauto.carservice.wxapp.entity.UserVO;
import org.springframework.stereotype.Repository;
import org.welisdoon.webserver.common.dao.IDao;

import java.util.Map;

@Repository
public interface UserOperRecordDao extends IDao<OrderVO, Integer> {
    void location(UserVO userVO);
}
