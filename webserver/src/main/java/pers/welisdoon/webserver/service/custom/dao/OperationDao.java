package pers.welisdoon.webserver.service.custom.dao;

import java.util.List;

import org.springframework.stereotype.Repository;

import pers.welisdoon.webserver.common.dao.IDao;
import pers.welisdoon.webserver.service.custom.entity.OperationVO;
import pers.welisdoon.webserver.service.custom.entity.UserVO;

@Repository
public interface OperationDao extends IDao<OperationVO, Integer> {

}
