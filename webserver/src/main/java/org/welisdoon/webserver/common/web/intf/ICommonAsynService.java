package org.welisdoon.webserver.common.web.intf;


import io.vertx.codegen.annotations.ProxyGen;
import io.vertx.codegen.annotations.VertxGen;
import io.vertx.core.*;
import io.vertx.serviceproxy.ServiceBinder;
import io.vertx.serviceproxy.ServiceProxyBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;
import org.welisdoon.webserver.common.ApplicationContextProvider;
import org.welisdoon.webserver.common.web.Requset;
import org.welisdoon.webserver.common.web.Response;
import org.welisdoon.webserver.config.WeChatServiceConfiguration;

@ProxyGen
@VertxGen
public interface ICommonAsynService {

    static ICommonAsynService create(Vertx vertx, String address) {
        ICommonAsynService ICommonAsynService = ApplicationContextProvider.getBean(ICommonAsynService.class);
        new ServiceBinder(vertx).setAddress(address = name(ICommonAsynService.class, address))
                .register(ICommonAsynService.class, ICommonAsynService)
                .completionHandler(Promise.promise());
        LoggerFactory.getLogger(ICommonAsynService.class).info(String.format("create Service:%s", address));
        return ICommonAsynService;
    }

    static ICommonAsynService createProxy(Vertx vertx, String address) {
        ICommonAsynService service = new ServiceProxyBuilder(vertx)
                .setAddress(address = name(ICommonAsynService.class, address))
                .build(ICommonAsynService.class);
        LoggerFactory.getLogger(ICommonAsynService.class).info(String.format("create Service Proxy:%s", address));
        return service;
    }

    private static String name(Class<?> clz, String key) {
        return String.format("%s[%s]", clz.getName(), StringUtils.isEmpty(key) ? "default" : key);
    }

    /*void wechatMsgReceive(String serverName, String inputBody, Handler<AsyncResult<String>> outputBodyHandler);*/

    void requsetCall(Requset requset, Handler<AsyncResult<Response>> outputBodyHandler);

}
