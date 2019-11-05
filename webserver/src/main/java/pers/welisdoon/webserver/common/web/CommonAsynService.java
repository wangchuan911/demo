package pers.welisdoon.webserver.common.web;


import io.vertx.codegen.annotations.ProxyGen;
import io.vertx.codegen.annotations.VertxGen;
import io.vertx.core.*;
import io.vertx.serviceproxy.ServiceBinder;
import io.vertx.serviceproxy.ServiceProxyBuilder;
import pers.welisdoon.webserver.common.ApplicationContextProvider;
import pers.welisdoon.webserver.service.custom.entity.OrderVO;

@ProxyGen
@VertxGen
public interface CommonAsynService {

    static CommonAsynService create(Vertx vertx) {
        CommonAsynService commonAsynService = ApplicationContextProvider.getBean(CommonAsynService.class);
        new ServiceBinder(vertx).setAddress(CommonAsynService.class.getName())
                .register(CommonAsynService.class, commonAsynService)
                .completionHandler(Promise.promise());
        return commonAsynService;
    }

    static CommonAsynService createProxy(Vertx vertx) {
        return new ServiceProxyBuilder(vertx)
                .setAddress(CommonAsynService.class.getName())
                .build(CommonAsynService.class);
    }

    void wechatMsgReceive(String inputBody, Handler<AsyncResult<String>> outputBodyHandler);

    void serviceCall(String serverName, String method, String inputBody, String option, Handler<AsyncResult<String>> outputBodyHandler);

    void requsetCall(Requset requset, Handler<AsyncResult<Response>> outputBodyHandler);

}
