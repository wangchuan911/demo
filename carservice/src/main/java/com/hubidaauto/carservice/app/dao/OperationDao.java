package com.hubidaauto.carservice.app.dao;

import com.hubidaauto.carservice.app.entity.OperationVO;
import org.springframework.stereotype.Repository;
import org.welisdoon.webserver.common.dao.IDao;

@Repository
public interface OperationDao extends IDao<OperationVO, Integer> {

}
