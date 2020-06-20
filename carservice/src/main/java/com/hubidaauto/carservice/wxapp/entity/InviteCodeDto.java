package com.hubidaauto.carservice.wxapp.entity;

public class InviteCodeDto {
	Integer code;
	String userId;
	Integer valid;
	Character type;

	public Integer getCode() {
		return code;
	}

	public InviteCodeDto setCode(Integer code) {
		this.code = code;
		return this;
	}

	public String getUserId() {
		return userId;
	}

	public InviteCodeDto setUserId(String userId) {
		this.userId = userId;
		return this;
	}

	public Integer getValid() {
		return valid;
	}

	public InviteCodeDto setValid(Integer valid) {
		this.valid = valid;
		return this;
	}

	public Character getType() {
		return type;
	}

	public InviteCodeDto setType(Character type) {
		this.type = type;
		return this;
	}
}
