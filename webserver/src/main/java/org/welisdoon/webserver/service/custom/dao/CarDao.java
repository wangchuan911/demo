package org.welisdoon.webserver.service.custom.dao;

import org.springframework.stereotype.Repository;

import org.welisdoon.webserver.common.dao.IDao;
import org.welisdoon.webserver.service.custom.entity.CarVO;

import java.util.List;
import java.util.Map;

@Repository
public interface CarDao extends IDao<CarVO, String> {
    List<Map<String, Object>> getModel(Map<String, Object> param);
}
