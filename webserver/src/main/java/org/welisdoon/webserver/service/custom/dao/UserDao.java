package org.welisdoon.webserver.service.custom.dao;

import org.springframework.stereotype.Repository;

import org.welisdoon.webserver.common.dao.IDao;
import org.welisdoon.webserver.service.custom.entity.UserVO;

@Repository
public interface UserDao extends IDao<UserVO, String> {

}
