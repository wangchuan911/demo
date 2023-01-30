package org.welisdoon.individual.wechat.oa.handler;

import io.vertx.core.Future;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.welisdoon.individual.wechat.oa.MyHomeConfiguration;
import org.welisdoon.web.entity.wechat.messeage.handler.MesseageHandler;
import org.welisdoon.web.entity.wechat.messeage.request.LocationMesseage;
import org.welisdoon.web.entity.wechat.messeage.response.TextMesseage;

/**
 * @Classname GetMyOpenIdHandler
 * @Description TODO
 * @Author wang.zhidong
 * @Date 2022/2/16 22:37
 */
@Component
@MesseageHandler.Priority(value = 0, config = MyHomeConfiguration.class)
public class GetMyOpenIdHandler implements MesseageHandler<org.welisdoon.web.entity.wechat.messeage.request.TextMesseage, TextMesseage> {
    @Override
    public Future<TextMesseage> handle(org.welisdoon.web.entity.wechat.messeage.request.TextMesseage textMesseage) {
        TextMesseage messeage = new TextMesseage(textMesseage);
        messeage.setContent(textMesseage.getToUserName());
        return Future.succeededFuture(messeage);
    }

    @Override
    public Future<Boolean> matched(org.welisdoon.web.entity.wechat.messeage.request.TextMesseage textMesseage) {
        return Future.succeededFuture(StringUtils.hasText(textMesseage.getContent()) && textMesseage.getContent().equals("我是谁"));
    }
}
