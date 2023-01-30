package org.welisdoon.web.entity.wechat.messeage.handler;

import io.vertx.core.Future;
import org.springframework.stereotype.Component;
import org.welisdoon.web.common.config.AbstractWechatConfiguration;
import org.welisdoon.web.entity.wechat.messeage.request.TextMesseage;
import org.welisdoon.web.entity.wechat.messeage.response.NoReplyMesseage;


/**
 * @Classname DefaultTextHandler
 * @Description TODO
 * @Author wang.zhidong
 * @Date 2022/2/15 15:13
 */
@Component
@MesseageHandler.Priority(config = AbstractWechatConfiguration.class, value = Integer.MAX_VALUE)
public class DefaultTextHandler implements MesseageHandler<TextMesseage, NoReplyMesseage> {
    @Override
    public Future<NoReplyMesseage> handle(TextMesseage textMesseage) {
        return Future.succeededFuture(new NoReplyMesseage(textMesseage));
    }

    @Override
    public Future<Boolean> matched(TextMesseage textMesseage) {
        return Future.succeededFuture(true);
    }
}
