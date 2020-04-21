package com.hubidaauto.carservice.wxapp.service;

import com.hubidaauto.carservice.wxapp.config.CustomConst;
import com.hubidaauto.carservice.wxapp.dao.CouponDao;
import com.hubidaauto.carservice.wxapp.entity.CouponVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.welisdoon.webserver.common.web.AbstractBaseService;
import org.welisdoon.webserver.vertx.annotation.VertxWebApi;

import java.util.Map;

@Service
@Transactional(rollbackFor = Throwable.class)
public class CouponService extends AbstractBaseService<CouponVO> {
    @Autowired
    CouponDao couponDao;

    @Override
    @VertxWebApi
    public Object handle(int exeCode, Map params) {
        Object resultObj = null;
        CouponVO couponVO = null;
        switch (exeCode) {
            case CustomConst.LIST:
                couponVO = mapToObject(params, CouponVO.class);
                resultObj = couponDao.list(couponVO);
                break;
            case CustomConst.ADD:
                couponVO = mapToObject(params, CouponVO.class);
                couponDao.add(couponVO);
                resultObj = couponVO;
                break;
            case CustomConst.MODIFY:
                couponVO = mapToObject(params, CouponVO.class);
                resultObj = couponDao.set(couponVO);
                break;
            case CustomConst.GET:
                couponVO = mapToObject(params, CouponVO.class);
                resultObj = couponDao.get(couponVO);
                break;
            default:
                break;
        }
        return resultObj;
    }
}
