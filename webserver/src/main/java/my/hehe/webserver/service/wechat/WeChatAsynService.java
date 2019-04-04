package my.hehe.webserver.service.wechat;


import io.vertx.codegen.annotations.ProxyGen;
import io.vertx.codegen.annotations.VertxGen;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.serviceproxy.ServiceBinder;
import io.vertx.serviceproxy.ServiceProxyBuilder;
import my.hehe.webserver.common.ApplicationContextProvider;

@ProxyGen
@VertxGen
public interface WeChatAsynService {

    static WeChatAsynService create(Vertx vertx) {
        WeChatAsynService weChatAsynService = ApplicationContextProvider.getBean(WeChatAsynService.class);
        new ServiceBinder(vertx).setAddress(WeChatAsynService.class.getName())
                .register(WeChatAsynService.class, weChatAsynService)
                .completionHandler(Future.future());
        return weChatAsynService;
    }

    static WeChatAsynService createProxy(Vertx vertx) {
        return new ServiceProxyBuilder(vertx)
                .setAddress(WeChatAsynService.class.getName())
                .build(WeChatAsynService.class);
    }

    void receive(String inputBody, Handler<AsyncResult<String>> outputBodyHandler);
}
