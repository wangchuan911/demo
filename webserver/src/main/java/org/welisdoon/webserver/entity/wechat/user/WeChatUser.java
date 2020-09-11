package org.welisdoon.webserver.entity.wechat.user;

public class WeChatUser {
	private String id;
	private String sessionKey;
	private String unionid;

	public String getId() {
		return id;
	}

	public WeChatUser setId(String id) {
		this.id = id;
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
