package com.hubidaauto.carservice.wxapp.increment.service;

import com.hubidaauto.carservice.wxapp.config.CustomConst;
import com.hubidaauto.carservice.wxapp.dao.UserDao;
import com.hubidaauto.carservice.wxapp.entity.OrderVO;
import com.hubidaauto.carservice.wxapp.increment.dao.CouponDao;
import com.hubidaauto.carservice.wxapp.increment.dao.UserOperRecordDao;
import com.hubidaauto.carservice.wxapp.increment.entity.CouponVO;
import org.apache.commons.collections4.MapUtils;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.welisdoon.webserver.common.web.AbstractBaseService;
import org.welisdoon.webserver.vertx.annotation.VertxWebApi;

import java.util.List;
import java.util.Map;

@Service
@Transactional(rollbackFor = Throwable.class)
public class CouponService extends AbstractBaseService<CouponVO> {
	private static final Logger logger = logger(CouponService.class);
	@Autowired
	CouponDao couponDao;
	@Autowired
	UserDao userDao;
	@Autowired
	UserOperRecordDao userOperRecordDao;

    /*@Override
    public void init() throws Throwable {
        CouponVO couponVO = (CouponVO)
                handle(CustomConst.GET, new CouponVO()
                        .setOrderId(216)
                        .setUserId("oX1Dn5YMM2MipWO_owPjMqwmmr18"));
        if (couponVO != null) {
            handle(CustomConst.MODIFY, couponVO.setOrderId(null).setUseDate(null));
        }
    }*/

	@Override
	@VertxWebApi
	public Object handle(int exeCode, Map params) {
		Object resultObj;
		CouponVO couponVO;
		switch (exeCode) {
			case CustomConst.LIST:
				couponVO = mapToObject(params, CouponVO.class);
				resultObj = this.handle(exeCode, couponVO);
				break;
			default:
				resultObj = null;
				break;
		}
		return resultObj;
	}

	@Override
	public Object handle(int exeCode, CouponVO couponVO) {
		Object resultObj = null;
		switch (exeCode) {
			case CustomConst.LIST:
				resultObj = couponDao.list(couponVO);
				break;
			case CustomConst.ADD:
				couponDao.add(couponVO);
				resultObj = couponVO;
				break;
			case CustomConst.MODIFY:
				resultObj = couponDao.set(couponVO);
				break;
			case CustomConst.GET:
				resultObj = couponDao.get(couponVO);
				break;
			default:
				break;
		}
		return resultObj;
	}

	public List<CouponVO> newCoupon(int exeCode, Map map) {
		List<CouponVO> couponVOS = null;
		CouponVO couponVO;
		switch (exeCode) {
			//新用户
			case CustomConst.COUPON.NEW_USER:
				try {
					String custId = MapUtils.getString(map, "id");
					if (userOperRecordDao.num(new OrderVO().setCustId(custId)) > 0)
						break;
					couponVO = new CouponVO()
							.setTemplateId(CustomConst.COUPON.TEMPLATE.NEW_USER)
							.setUserId(custId);
					if (couponDao.num(couponVO) > 0)
						break;
					this.handle(CustomConst.ADD
							, couponVO);
					couponVOS = couponDao.list(couponVO);
				} catch (Throwable e) {
					logger.error(e.getMessage(), e);
				}
				break;
			case CustomConst.COUPON.BUY_COUPONS: {
				String custId = MapUtils.getString(map, "id");
				couponVO = new CouponVO()
						.setTemplateId(MapUtils.getInteger(map, "couponId"))
						.setUserId(custId);
				for (int i = 0; i < 5; i++) {
					this.handle(CustomConst.ADD, couponVO);
				}
			}
			break;
			default:
				break;

		}

		return couponVOS;
	}
}
