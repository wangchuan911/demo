package org.welisdoon.webserver.entity.wechat;

public class WeChatPayOrder {
	String id;
	String userId;

	public String getId() {
		return id;
	}

	public WeChatPayOrder setId(String id) {
		this.id = id;
		return this;
	}

	public String getUserId() {
		return userId;
	}

	public WeChatPayOrder setUserId(String userId) {
		this.userId = userId;
		return this;
	}
}
