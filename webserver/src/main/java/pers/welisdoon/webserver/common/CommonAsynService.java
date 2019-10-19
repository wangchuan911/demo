package pers.welisdoon.webserver.common;


import io.vertx.codegen.annotations.ProxyGen;
import io.vertx.codegen.annotations.VertxGen;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.serviceproxy.ServiceBinder;
import io.vertx.serviceproxy.ServiceProxyBuilder;

@ProxyGen
@VertxGen
public interface CommonAsynService {

    static CommonAsynService create(Vertx vertx) {
        CommonAsynService commonAsynService = ApplicationContextProvider.getBean(CommonAsynService.class);
        new ServiceBinder(vertx).setAddress(CommonAsynService.class.getName())
                .register(CommonAsynService.class, commonAsynService)
                .completionHandler(Future.future());
        return commonAsynService;
    }

    static CommonAsynService createProxy(Vertx vertx) {
        return new ServiceProxyBuilder(vertx)
                .setAddress(CommonAsynService.class.getName())
                .build(CommonAsynService.class);
    }

    void wechatMsgReceive(String inputBody, Handler<AsyncResult<String>> outputBodyHandler);

    void serviceCall(String serverName, String method, String inputBody, String option, Handler<AsyncResult<String>> outputBodyHandler);

}
