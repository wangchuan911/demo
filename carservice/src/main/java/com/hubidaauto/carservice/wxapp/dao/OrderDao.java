package com.hubidaauto.carservice.wxapp.dao;

import com.hubidaauto.carservice.wxapp.entity.OrderVO;
import org.springframework.stereotype.Repository;
import org.welisdoon.webserver.common.dao.IDao;

import java.util.List;
import java.util.Map;

@Repository
public interface OrderDao extends IDao<OrderVO, Integer> {
	List<Map> getWorkIngOrderNum(OrderVO orderVO);
}
