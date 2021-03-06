package com.hubidaauto.carservice.wxapp.increment.entity;

import java.sql.Timestamp;

public class CouponVO {
	Integer templateId;
	String userId;
	Integer id;
	String content;
	Timestamp startDate;
	Timestamp endDate;
	Timestamp useDate;
	Integer type;
	Integer off;
	Integer lv;
	Integer orderId;
	String defineType;

	public String getDefineType() {
		return defineType;
	}

	public CouponVO setDefineType(String defineType) {
		this.defineType = defineType;
		return this;
	}

	public Integer getId() {
		return id;
	}

	public CouponVO setId(Integer id) {
		this.id = id;
		return this;
	}

	public String getContent() {
		return content;
	}

	public CouponVO setContent(String content) {
		this.content = content;
		return this;
	}

	public Timestamp getEndDate() {
		return endDate;
	}

	public CouponVO setEndDate(Timestamp endDate) {
		this.endDate = endDate;
		return this;
	}

	public Integer getType() {
		return type;
	}

	public CouponVO setType(Integer type) {
		this.type = type;
		return this;
	}

	public Integer getOff() {
		return off;
	}

	public CouponVO setOff(Integer off) {
		this.off = off;
		return this;
	}

	public Integer getLv() {
		return lv;
	}

	public CouponVO setLv(Integer lv) {
		this.lv = lv;
		return this;
	}

	public Timestamp getStartDate() {
		return startDate;
	}

	public CouponVO setStartDate(Timestamp startDate) {
		this.startDate = startDate;
		return this;
	}

	public String getUserId() {
		return userId;
	}

	public CouponVO setUserId(String userId) {
		this.userId = userId;
		return this;
	}

	public Timestamp getUseDate() {
		return useDate;
	}

	public CouponVO setUseDate(Timestamp useDate) {
		this.useDate = useDate;
		return this;
	}

	public Integer getOrderId() {
		return orderId;
	}

	public CouponVO setOrderId(Integer orderId) {
		this.orderId = orderId;
		return this;
	}

	public Integer getTemplateId() {
		return templateId;
	}

	public CouponVO setTemplateId(Integer templateId) {
		this.templateId = templateId;
		return this;
	}
}
