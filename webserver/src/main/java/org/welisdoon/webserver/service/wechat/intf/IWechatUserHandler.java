package org.welisdoon.webserver.service.wechat.intf;

import org.welisdoon.webserver.entity.wechat.user.WeChatUser;

public interface IWechatUserHandler {
	Object login(WeChatUser weChatUser);
}
