package com.hubidaauto.carservice.wxapp.core.dao;

import com.hubidaauto.carservice.wxapp.core.entity.TacheVO;
import org.springframework.stereotype.Repository;
import org.welisdoon.web.common.dao.IDao;

import java.util.Map;

@Repository
public interface TacheDao extends IDao<TacheVO, Integer> {
    Map getProccess(Map param);

}
