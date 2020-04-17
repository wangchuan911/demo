package com.hubidaauto.carservice.app.dao;

import com.hubidaauto.carservice.app.entity.TacheVO;
import org.springframework.stereotype.Repository;
import org.welisdoon.webserver.common.dao.IDao;

import java.util.Map;

@Repository
public interface TacheDao extends IDao<TacheVO, Integer> {
    Map getProccess(Map param);

}
