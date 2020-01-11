package org.welisdoon.webserver.service.custom.dao;

import java.util.Map;

import org.springframework.stereotype.Repository;

import org.welisdoon.webserver.common.dao.IDao;
import org.welisdoon.webserver.service.custom.entity.TacheVO;

@Repository
public interface TacheDao extends IDao<TacheVO, Integer> {
    Map getProccess(Map param);

}
