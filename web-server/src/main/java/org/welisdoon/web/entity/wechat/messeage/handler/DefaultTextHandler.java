package org.welisdoon.web.entity.wechat.messeage.handler;

import io.vertx.core.Future;
import org.springframework.stereotype.Component;
import org.welisdoon.web.common.config.AbstractWechatConfiguration;
import org.welisdoon.web.entity.wechat.messeage.request.TextMesseage;


/**
 * @Classname DefaultTextHandler
 * @Description TODO
 * @Author wang.zhidong
 * @Date 2022/2/15 15:13
 */
@Component
@MesseageHandler.Priority(config = AbstractWechatConfiguration.class, value = Integer.MAX_VALUE)
public class DefaultTextHandler implements MesseageHandler<TextMesseage, org.welisdoon.web.entity.wechat.messeage.response.TextMesseage> {
    @Override
    public Future<org.welisdoon.web.entity.wechat.messeage.response.TextMesseage> handle(TextMesseage textMesseage) {
        return null;
    }

    @Override
    public boolean matched(TextMesseage textMesseage) {
        return true;
    }
}
