package com.hubidaauto.carservice.wxapp.dao;

import com.hubidaauto.carservice.wxapp.entity.CouponVO;
import com.hubidaauto.carservice.wxapp.entity.UserVO;
import org.springframework.stereotype.Repository;
import org.welisdoon.webserver.common.dao.IDao;

import java.util.List;

@Repository
public interface CouponDao extends IDao<CouponVO, Integer> {
}
