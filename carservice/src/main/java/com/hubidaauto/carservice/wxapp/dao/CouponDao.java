package com.hubidaauto.carservice.wxapp.dao;

import com.hubidaauto.carservice.wxapp.entity.increment.CouponVO;
import org.springframework.stereotype.Repository;
import org.welisdoon.webserver.common.dao.IDao;

@Repository
public interface CouponDao extends IDao<CouponVO, Integer> {
}
