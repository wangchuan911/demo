package my.hehe.webserver.vertx.verticle;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.serviceproxy.ServiceBinder;
import my.hehe.webserver.service.wechat.WeChatAsynService;
import my.hehe.webserver.service.wechat.impl.WeChatAsynServiceImpl;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.Set;

@Component("workerVerticle")
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class WorkerVerticle extends AbstractCustomVerticle {

    static Set<Handler<Vertx>> VERTX_HANDLERS = null;

    @Override
    public void start(Future<Void> startFuture) throws Exception {
        try {

            for (Handler<Vertx> vertxHandler : VERTX_HANDLERS) {
                vertxHandler.handle(vertx);
            }

            startFuture.complete();
        } catch (Exception e) {
            startFuture.fail(e);
        }
    }

}
