package com.hubidaauto.carservice.wxapp.dao.increment;

import com.hubidaauto.carservice.wxapp.entity.increment.InviteCodeDto;
import org.springframework.stereotype.Repository;
import org.welisdoon.webserver.common.dao.IDao;

@Repository
public interface InviteCodeDao extends IDao<InviteCodeDto, String> {
}
