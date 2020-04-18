package com.hubidaauto.carservice.officalaccount.dao;

import com.hubidaauto.carservice.wxapp.entity.UserVO;
import org.springframework.stereotype.Repository;
import org.welisdoon.webserver.common.dao.IDao;

@Repository
public interface OfficalAccoutUserDao extends IDao<UserVO, String> {

}
