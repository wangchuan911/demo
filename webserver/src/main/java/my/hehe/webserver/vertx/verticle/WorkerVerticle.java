package my.hehe.webserver.vertx.verticle;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;
import io.vertx.serviceproxy.ServiceBinder;
import my.hehe.webserver.service.wechat.WeChatAsynService;
import my.hehe.webserver.service.wechat.impl.WeChatAsynServiceImpl;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component("workerVerticle")
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class WorkerVerticle extends AbstractVerticle {

    @Override
    public void start(Future<Void> startFuture) throws Exception {
        try {
            WeChatAsynService.create(vertx);
            startFuture.complete();
        } catch (Exception e) {
            startFuture.fail(e);
        }
    }
}
