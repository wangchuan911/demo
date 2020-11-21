package com.hubidaauto.carservice.wxapp.mall.subunit.unit;

import com.hubidaauto.carservice.wxapp.core.config.CustomConst;
import com.hubidaauto.carservice.wxapp.increment.service.CouponService;
import com.hubidaauto.carservice.wxapp.mall.common.AbstractAutoAssign;
import com.hubidaauto.carservice.wxapp.mall.common.annotation.AutoAssign;
import com.hubidaauto.carservice.wxapp.mall.entity.MallOrderDto;
import com.hubidaauto.carservice.wxapp.mall.subunit.entity.CouponAssignDto;
import io.vertx.core.json.JsonObject;
import org.springframework.stereotype.Component;
import org.welisdoon.webserver.common.ApplicationContextProvider;

import javax.annotation.PostConstruct;
import java.util.Map;

@AutoAssign(name = "coupon")
@Component
public class CouponAssignComponent extends AbstractAutoAssign<CouponAssignDto> {

	CouponService couponService;

	@PostConstruct
	void init() {
		couponService = ApplicationContextProvider.getBean(CouponService.class);
	}

	@Override
	public void buy(CouponAssignDto couponAssignDto, MallOrderDto order) {
		couponService.newCoupon(CustomConst.COUPON.BUY_COUPONS,
				Map.of("id", order.getUserId(), "couponId", couponAssignDto.getId(), "count", couponAssignDto.getCount()));
	}
}
