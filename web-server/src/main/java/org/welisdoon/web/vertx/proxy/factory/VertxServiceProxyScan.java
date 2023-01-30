package org.welisdoon.web.vertx.proxy.factory;

import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

/**
 * @Classname VertxProxyScan
 * @Description TODO
 * @Author wang.zhidong
 * @Date 2021/8/16 16:15
 */
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
//spring中的注解,加载对应的类
@Import(VertxServiceProxyRegister.class)//这个是我们的关键，实际上也是由这个类来扫描的
@Documented
public @interface VertxServiceProxyScan {

    String[] basePackages() default {};

    Class<?>[] basePackageClasses() default {};
}
