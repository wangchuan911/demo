package org.welisdoon.web.service.wechat.intf;

import org.welisdoon.web.entity.wechat.WeChatUser;

public interface IWechatUserHandler {
	Object login(WeChatUser weChatUser);
}
