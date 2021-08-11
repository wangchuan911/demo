package org.welisdoon.web.entity.wechat;

public class WeChatUser {
	private String openId;
	private String sessionKey;
	private String unionid;

	public String getOpenId() {
		return openId;
	}

	public WeChatUser setOpenId(String openId) {
		this.openId = openId;
		return this;
	}

	public String getSessionKey() {
		return this.sessionKey;
	}

	public WeChatUser setSessionKey(String sessionKey) {
		this.sessionKey = sessionKey;
		return this;
	}

	public String getUnionid() {
		return this.unionid;
	}

	public WeChatUser setUnionid(String unionid) {
		this.unionid = unionid;
		return this;
	}

}
