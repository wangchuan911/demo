package com.hubidaauto.carservice.wxapp.mall.subunit.entity;

import com.hubidaauto.carservice.wxapp.mall.entity.AssignDto;

public class CouponAssignDto extends AssignDto {
	Integer id, count;

	public Integer getId() {
		return id;
	}

	public CouponAssignDto setId(Integer id) {
		this.id = id;
		return this;
	}

	public Integer getCount() {
		return count;
	}

	public CouponAssignDto setCount(Integer count) {
		this.count = count;
		return this;
	}
}
