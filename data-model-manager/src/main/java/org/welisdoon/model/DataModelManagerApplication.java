package org.welisdoon.model;

import org.mybatis.spring.annotation.MapperScan;
import org.reflections.Reflections;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.context.annotation.FilterType;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.welisdoon.web.WebserverApplication;
import org.welisdoon.web.vertx.proxy.factory.VertxServiceProxyScan;

import javax.annotation.PostConstruct;

@SpringBootApplication
@EnableTransactionManagement(proxyTargetClass = true)
@MapperScan(basePackageClasses = {DataModelManagerApplication.class}, annotationClass = Repository.class)
@ComponentScan(
        basePackageClasses = {WebserverApplication.class, DataModelManagerApplication.class},
        excludeFilters = {
                @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = {WebserverApplication.class})
        }
)
//@EnableAspectJAutoProxy(proxyTargetClass = true)
@VertxServiceProxyScan(basePackageClasses = {DataModelManagerApplication.class})
public class DataModelManagerApplication {
    public static void main(String[] args) {
        SpringApplication.run(DataModelManagerApplication.class, args);
    }

}
