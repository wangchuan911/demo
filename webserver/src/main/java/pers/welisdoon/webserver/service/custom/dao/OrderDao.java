package pers.welisdoon.webserver.service.custom.dao;

import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

import pers.welisdoon.webserver.common.dao.IDao;
import pers.welisdoon.webserver.service.custom.entity.OrderVO;

@Repository
public interface OrderDao extends IDao<OrderVO> {
}
