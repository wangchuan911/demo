package com.hubidaauto.carservice.service.dao;

import com.hubidaauto.carservice.service.entity.UserVO;
import org.springframework.stereotype.Repository;
import org.welisdoon.webserver.common.dao.IDao;

import java.util.List;

@Repository
public interface UserDao extends IDao<UserVO, String> {
    List<Integer> getWorkAreaRange(UserVO userVO);

    List<String> getRegionOrderController(Integer regionCode);
}
