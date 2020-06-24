package com.hubidaauto.carservice.wxapp.increment.dao;

import com.hubidaauto.carservice.wxapp.core.entity.OrderVO;
import com.hubidaauto.carservice.wxapp.core.entity.UserVO;
import org.springframework.stereotype.Repository;
import org.welisdoon.webserver.common.dao.IDao;

@Repository
public interface UserOperRecordDao extends IDao<OrderVO, Integer> {
    void location(UserVO userVO);
}
