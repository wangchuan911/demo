package com.hubidaauto.carservice.wxapp.dao;

import com.hubidaauto.carservice.wxapp.entity.TacheVO;
import org.springframework.stereotype.Repository;
import org.welisdoon.webserver.common.dao.IDao;

import java.util.Map;

@Repository
public interface TacheDao extends IDao<TacheVO, Integer> {
    Map getProccess(Map param);

}
