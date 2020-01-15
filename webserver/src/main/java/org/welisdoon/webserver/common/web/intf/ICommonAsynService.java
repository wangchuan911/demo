package org.welisdoon.webserver.common.web.intf;


import io.vertx.codegen.annotations.ProxyGen;
import io.vertx.codegen.annotations.VertxGen;
import io.vertx.core.*;
import io.vertx.serviceproxy.ServiceBinder;
import io.vertx.serviceproxy.ServiceProxyBuilder;
import org.welisdoon.webserver.common.ApplicationContextProvider;
import org.welisdoon.webserver.common.web.Requset;
import org.welisdoon.webserver.common.web.Response;

@ProxyGen
@VertxGen
public interface ICommonAsynService {

    static ICommonAsynService create(Vertx vertx) {
        ICommonAsynService ICommonAsynService = ApplicationContextProvider.getBean(ICommonAsynService.class);
        new ServiceBinder(vertx).setAddress(ICommonAsynService.class.getName())
                .register(ICommonAsynService.class, ICommonAsynService)
                .completionHandler(Promise.promise());
        return ICommonAsynService;
    }

    static ICommonAsynService createProxy(Vertx vertx) {
        return new ServiceProxyBuilder(vertx)
                .setAddress(ICommonAsynService.class.getName())
                .build(ICommonAsynService.class);
    }

    void wechatMsgReceive(String inputBody, Handler<AsyncResult<String>> outputBodyHandler);

    void requsetCall(Requset requset, Handler<AsyncResult<Response>> outputBodyHandler);

}
