package com.hubidaauto.carservice.wxapp.increment.entity;

public class MallOrderDto {
	Integer id;
	String code;
	Integer goodsId;
	Integer count;
	String userId;
	Integer cost;
	Integer state;

	public Integer getId() {
		return id;
	}

	public MallOrderDto setId(Integer id) {
		this.id = id;
		return this;
	}

	public String getCode() {
		return code;
	}

	public MallOrderDto setCode(String code) {
		this.code = code;
		return this;
	}

	public Integer getGoodsId() {
		return goodsId;
	}

	public MallOrderDto setGoodsId(Integer goodsId) {
		this.goodsId = goodsId;
		return this;
	}

	public Integer getCount() {
		return count;
	}

	public MallOrderDto setCount(Integer count) {
		this.count = count;
		return this;
	}

	public String getUserId() {
		return userId;
	}

	public MallOrderDto setUserId(String userId) {
		this.userId = userId;
		return this;
	}

	public Integer getCost() {
		return cost;
	}

	public MallOrderDto setCost(Integer cost) {
		this.cost = cost;
		return this;
	}

	public Integer getState() {
		return state;
	}

	public MallOrderDto setState(Integer state) {
		this.state = state;
		return this;
	}
}
