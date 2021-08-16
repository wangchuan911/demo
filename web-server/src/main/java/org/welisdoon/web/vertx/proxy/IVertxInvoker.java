package org.welisdoon.web.vertx.proxy;

import io.vertx.codegen.annotations.ProxyGen;
import io.vertx.codegen.annotations.VertxGen;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonArray;

/**
 * @Classname IVertixMethodInvocker
 * @Description TODO
 * @Author wang.zhidong
 * @Date 2021/8/13 16:39
 */
@ProxyGen
@VertxGen
public interface IVertxInvoker {
    void invoke(String clzName, String method, String paramTypes, String params, Handler<AsyncResult<String>> handler);
}
