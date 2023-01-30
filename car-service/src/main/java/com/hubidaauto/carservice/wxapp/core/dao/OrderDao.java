package com.hubidaauto.carservice.wxapp.core.dao;

import com.hubidaauto.carservice.wxapp.core.entity.OrderVO;
import org.springframework.stereotype.Repository;
import org.welisdoon.web.common.dao.IDao;

import java.util.List;
import java.util.Map;

@Repository
public interface OrderDao extends IDao<OrderVO, Integer> {
	List<Map> getWorkIngOrderNum(OrderVO orderVO);
}
