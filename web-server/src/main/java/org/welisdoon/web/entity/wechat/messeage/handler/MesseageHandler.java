package org.welisdoon.web.entity.wechat.messeage.handler;

import org.welisdoon.web.entity.wechat.messeage.request.RequestMesseage;
import org.welisdoon.web.entity.wechat.messeage.request.RequestMesseageBody;
import org.welisdoon.web.entity.wechat.messeage.response.ResponseMesseage;

import java.util.concurrent.Future;

/**
 * @Classname TextHandler
 * @Description TODO
 * @Author wang.zhidong
 * @Date 2022/2/15 10:14
 */
@FunctionalInterface
public interface MesseageHandler<I extends RequestMesseage, O extends ResponseMesseage> {
    Future<O> handle(I i);
}
