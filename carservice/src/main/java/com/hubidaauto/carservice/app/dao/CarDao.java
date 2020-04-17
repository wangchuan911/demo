package com.hubidaauto.carservice.app.dao;

import com.hubidaauto.carservice.app.entity.CarVO;
import org.springframework.stereotype.Repository;
import org.welisdoon.webserver.common.dao.IDao;

import java.util.List;
import java.util.Map;

@Repository
public interface CarDao extends IDao<CarVO, String> {
    List<Map<String, Object>> getModel(Map<String, Object> param);

    CarVO.CarInfo getCarInfo(Integer carModelId);
}
