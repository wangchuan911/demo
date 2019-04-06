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


    @Override
    void registedBefore(Future future) {
        future.complete();
    }

    @Override
    void registedAfter(Future future) {
        future.complete();
    }
}
