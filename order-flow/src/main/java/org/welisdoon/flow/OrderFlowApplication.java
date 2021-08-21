package org.welisdoon.flow;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.welisdoon.web.WebserverApplication;
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
@ComponentScan(basePackageClasses = {WebserverApplication.class, OrderFlowApplication.class},
        excludeFilters = {
                @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = {WebserverApplication.class})
        })
public class OrderFlowApplication {
    public static void main(String[] args) {
        SpringApplication.run(OrderFlowApplication.class, args);
    }
}
