package pers.welisdoon.webserver.service.custom.dao;

import org.springframework.stereotype.Repository;

import pers.welisdoon.webserver.common.dao.IDao;
import pers.welisdoon.webserver.service.custom.entity.UserVO;

@Repository
public interface CarDao extends IDao<UserVO, String> {

}
