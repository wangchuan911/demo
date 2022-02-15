package org.welisdoon.web.service.wechat.intf;

import io.vertx.core.Future;
import org.welisdoon.web.entity.wechat.WeChatUser;

@FunctionalInterface
public interface WechatLoginHandler<T> {
    Future<T> login(WeChatUser weChatUser);
}
