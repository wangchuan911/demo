package pers.welisdoon.webserver.service.custom.dao;

import java.util.Map;

import org.springframework.stereotype.Repository;

import pers.welisdoon.webserver.common.dao.IDao;
import pers.welisdoon.webserver.service.custom.entity.OrderVO;

@Repository
public interface OrderDao extends IDao<OrderVO> {
    Map getWorkIngOrderNum(OrderVO orderVO);
}
