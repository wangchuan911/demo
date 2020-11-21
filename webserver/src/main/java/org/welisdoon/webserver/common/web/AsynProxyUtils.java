package org.welisdoon.webserver.common.web;

import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.serviceproxy.ServiceBinder;
import io.vertx.serviceproxy.ServiceProxyBuilder;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;
import org.welisdoon.webserver.common.ApplicationContextProvider;

/**
 * @Classname AsynProxyUtils
 * @Description TODO
 * @Author wang.zhidong
 * @Date 2020/11/21 11:41
 */
public interface AsynProxyUtils {
    static <T> T create(Vertx vertx, String address, Class<T> type) {
        T bean = ApplicationContextProvider.getBean(type);
        new ServiceBinder(vertx).setAddress(address = name(type, address))
                .register(type, bean)
                .completionHandler(Promise.promise());
        LoggerFactory.getLogger(AsynProxyUtils.class).info(String.format("create Service:%s", address));
        return bean;
    }

    static <T> T createProxy(Vertx vertx, String address, Class<T> type) {
        T bean = new ServiceProxyBuilder(vertx)
                .setAddress(address = name(type, address))
                .build(type);
        LoggerFactory.getLogger(AsynProxyUtils.class).info(String.format("create Service Proxy:%s", address));
        return bean;
    }

    private static String name(Class<?> clz, String key) {
        return String.format("%s[%s]", clz.getName(), StringUtils.isEmpty(key) ? "default" : key);
    }
}
