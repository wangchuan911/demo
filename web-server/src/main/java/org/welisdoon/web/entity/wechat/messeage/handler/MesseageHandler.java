package org.welisdoon.web.entity.wechat.messeage.handler;

import io.vertx.core.Future;
import org.welisdoon.web.common.config.AbstractWechatConfiguration;
import org.welisdoon.web.entity.wechat.messeage.request.RequestMesseage;
import org.welisdoon.web.entity.wechat.messeage.request.RequestMesseageBody;
import org.welisdoon.web.entity.wechat.messeage.response.ResponseMesseage;

import java.lang.annotation.*;

/**
 * @Classname TextHandler
 * @Description TODO
 * @Author wang.zhidong
 * @Date 2022/2/15 10:14
 */
public interface MesseageHandler<I extends RequestMesseage, O extends ResponseMesseage> {
    Future<O> handle(I i);

    boolean matched(I i);


    @Target({ElementType.TYPE})
    @Retention(RetentionPolicy.RUNTIME)
    @Repeatable(Prioritys.class)
    @interface Priority {
        int value();

        Class<? extends AbstractWechatConfiguration> config();
    }

    @Target({ElementType.TYPE})
    @Retention(RetentionPolicy.RUNTIME)
    @interface Prioritys {
        Priority[] value();
    }
}
