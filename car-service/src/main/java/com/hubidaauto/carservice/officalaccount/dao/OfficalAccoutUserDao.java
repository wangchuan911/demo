package com.hubidaauto.carservice.officalaccount.dao;

import com.hubidaauto.carservice.officalaccount.entity.UserVO;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;
import org.welisdoon.web.common.dao.IDao;

@Repository
public interface OfficalAccoutUserDao extends IDao<UserVO, String> {
    UserVO getByOtherplatformId(@Param("table") String table, @Param("key") String key, @Param("id") String id);

}
