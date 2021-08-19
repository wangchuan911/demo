package org.welisdoon.flow;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.welisdoon.web.vertx.proxy.factory.VertxServiceProxyScan;

/**
 * @Classname OrderFlowApplication
 * @Description TODO
 * @Author wang.zhidong
 * @Date 2021/8/16 23:23
 */
@SpringBootApplication
@EnableTransactionManagement
@MapperScan(basePackageClasses = OrderFlowApplication.class, annotationClass = Repository.class)
@VertxServiceProxyScan(basePackageClasses = OrderFlowApplication.class)
public class OrderFlowApplication {
    public static void main(String[] args) {
        SpringApplication.run(OrderFlowApplication.class, args);
    }
}
