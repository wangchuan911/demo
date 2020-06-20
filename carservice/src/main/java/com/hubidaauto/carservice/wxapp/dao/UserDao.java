package com.hubidaauto.carservice.wxapp.dao;

import com.hubidaauto.carservice.wxapp.entity.OrderVO;
import com.hubidaauto.carservice.wxapp.entity.UserVO;
import org.springframework.stereotype.Repository;
import org.welisdoon.webserver.common.dao.IDao;

import java.util.List;

@Repository
public interface UserDao extends IDao<UserVO, String> {
	List<Integer> getWorkAreaRange(UserVO userVO);

	List<String> getRegionOrderController(Integer regionCode);

	List<UserVO> getWorkers(OrderVO orderVO);

	int newUserAttr(UserVO userVO);

	int setMaxRole(UserVO userVO);
}
