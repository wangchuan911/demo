package org.welisdoon.individual.wechat.oa.handler;

import io.vertx.core.Future;
import org.springframework.stereotype.Component;
import org.welisdoon.individual.wechat.oa.MyHomeConfiguration;
import org.welisdoon.web.entity.wechat.messeage.handler.MesseageHandler;
import org.welisdoon.web.entity.wechat.messeage.request.LocationMesseage;
import org.welisdoon.web.entity.wechat.messeage.response.TextMesseage;

/**
 * @Classname GetLocationHandler
 * @Description TODO
 * @Author wang.zhidong
 * @Date 2022/2/16 15:01
 */
@Component
@MesseageHandler.Priority(value = 0, config = MyHomeConfiguration.class)
public class GetLocationHandler implements MesseageHandler<LocationMesseage, TextMesseage> {
    @Override
    public Future<TextMesseage> handle(LocationMesseage locationMesseage) {
        TextMesseage textMesseage = new TextMesseage(locationMesseage);
        textMesseage.setContent(String.format("X:%s,Y:%s", locationMesseage.getLocationX(), locationMesseage.getLocationY()));
        return Future.succeededFuture(textMesseage);
    }

}
