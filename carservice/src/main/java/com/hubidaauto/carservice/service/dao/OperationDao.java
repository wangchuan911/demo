package com.hubidaauto.carservice.service.dao;

import com.hubidaauto.carservice.service.entity.OperationVO;
import org.springframework.stereotype.Repository;
import org.welisdoon.webserver.common.dao.IDao;

@Repository
public interface OperationDao extends IDao<OperationVO, Integer> {

}
