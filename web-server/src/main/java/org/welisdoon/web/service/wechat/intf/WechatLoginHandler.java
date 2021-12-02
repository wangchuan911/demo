package org.welisdoon.web.service.wechat.intf;

import org.welisdoon.web.entity.wechat.WeChatUser;

@FunctionalInterface
public interface WechatLoginHandler<T> {
    T login(WeChatUser weChatUser);
}
