package com.hubidaauto.carservice.wxapp.mall.common;

import com.hubidaauto.carservice.wxapp.mall.entity.AssignDto;
import com.hubidaauto.carservice.wxapp.mall.entity.MallOrderDto;

public abstract class AbstractAutoAssign<T extends AssignDto> {
	public abstract void buy(T t, MallOrderDto order);
}
