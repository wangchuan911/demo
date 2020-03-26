package com.hubidaauto.carservice.service.dao;

import com.hubidaauto.carservice.service.entity.OrderVO;
import org.springframework.stereotype.Repository;
import org.welisdoon.webserver.common.dao.IDao;

import java.util.Map;

@Repository
public interface OrderDao extends IDao<OrderVO, Integer> {
    Map getWorkIngOrderNum(OrderVO orderVO);
}
