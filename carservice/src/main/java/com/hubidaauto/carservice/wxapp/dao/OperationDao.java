package com.hubidaauto.carservice.wxapp.dao;

import com.hubidaauto.carservice.wxapp.entity.OperationVO;
import org.springframework.stereotype.Repository;
import org.welisdoon.webserver.common.dao.IDao;

@Repository
public interface OperationDao extends IDao<OperationVO, Integer> {

}
