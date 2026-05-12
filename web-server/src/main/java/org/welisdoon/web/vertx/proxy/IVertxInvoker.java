package org.welisdoon.web.vertx.proxy;

import io.vertx.codegen.annotations.ProxyGen;
import io.vertx.codegen.annotations.VertxGen;
import io.vertx.core.Future;
import org.welisdoon.web.vertx.proxy.meta.ClassData;
import org.welisdoon.web.vertx.proxy.meta.MethodData;
import org.welisdoon.web.vertx.proxy.meta.ReturnData;
import org.welisdoon.web.vertx.proxy.meta.ThreadLocalData;

/**
 * @Classname IVertixMethodInvocker
 * @Description TODO
 * @Author wang.zhidong
 * @Date 2021/8/13 16:39
 */
@ProxyGen
@VertxGen
public interface IVertxInvoker {
    Future<String> invoke(String clzName, String method, String paramTypes, String params, String threadParams);

    Future<Void> check(String clzName, String method, String paramTypes, String returnType);

    Future<ReturnData> apply(ClassData classData, MethodData methodMetaData, ThreadLocalData threadParams);
}
