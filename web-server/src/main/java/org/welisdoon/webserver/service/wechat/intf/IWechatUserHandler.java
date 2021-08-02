package org.welisdoon.webserver.service.wechat.intf;

import org.welisdoon.webserver.entity.wechat.WeChatUser;

public interface IWechatUserHandler {
	Object login(WeChatUser weChatUser);
}
