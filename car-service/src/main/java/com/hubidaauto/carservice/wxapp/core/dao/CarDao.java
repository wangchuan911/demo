package com.hubidaauto.carservice.wxapp.core.dao;

import com.hubidaauto.carservice.wxapp.core.entity.CarVO;
import org.springframework.stereotype.Repository;
import org.welisdoon.web.common.dao.IDao;

import java.util.List;
import java.util.Map;

@Repository
public interface CarDao extends IDao<CarVO, String> {
    List<Map<String, Object>> getModel(Map<String, Object> param);

    CarVO.CarInfo getCarInfo(Integer carModelId);
}
