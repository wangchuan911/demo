package pers.welisdoon.webserver.service.custom.dao;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Repository;

import pers.welisdoon.webserver.common.dao.IDao;
import pers.welisdoon.webserver.service.custom.entity.TacheVO;

@Repository
public interface TacheDao extends IDao<TacheVO, Integer> {
    Map getProccess(Integer tacheId);

}
