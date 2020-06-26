package com.hubidaauto.carservice.wxapp.mall.entity;

public class MallDto {
	Integer id;
	String name;
	String desc;
	Integer price;
	Boolean visable;

	public Integer getId() {
		return id;
	}

	public MallDto setId(Integer id) {
		this.id = id;
		return this;
	}

	public String getName() {
		return name;
	}

	public MallDto setName(String name) {
		this.name = name;
		return this;
	}

	public String getDesc() {
		return desc;
	}

	public MallDto setDesc(String desc) {
		this.desc = desc;
		return this;
	}

	public Integer getPrice() {
		return price;
	}

	public MallDto setPrice(Integer price) {
		this.price = price;
		return this;
	}

	public Boolean getVisable() {
		return visable;
	}

	public MallDto setVisable(Boolean visable) {
		this.visable = visable;
		return this;
	}
}
