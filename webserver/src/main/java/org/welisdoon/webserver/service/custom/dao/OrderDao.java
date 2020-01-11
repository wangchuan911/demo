package org.welisdoon.webserver.service.custom.dao;

import java.util.Map;

import org.springframework.stereotype.Repository;

import org.welisdoon.webserver.common.dao.IDao;
import org.welisdoon.webserver.service.custom.entity.OrderVO;

@Repository
public interface OrderDao extends IDao<OrderVO, Integer> {
    Map getWorkIngOrderNum(OrderVO orderVO);
}
