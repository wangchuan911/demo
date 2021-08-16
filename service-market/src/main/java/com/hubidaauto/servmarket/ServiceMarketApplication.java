package com.hubidaauto.servmarket;

import com.hubidaauto.carservice.CarserviceApplication;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.welisdoon.web.WebserverApplication;
import org.welisdoon.web.vertx.proxy.factory.VertxServiceProxyScan;

@SpringBootApplication
@EnableTransactionManagement(proxyTargetClass = true)
@MapperScan(basePackageClasses = {ServiceMarketApplication.class, CarserviceApplication.class}, annotationClass = Repository.class)
@ComponentScan(
        basePackageClasses = {WebserverApplication.class, ServiceMarketApplication.class, CarserviceApplication.class},
        excludeFilters = {
                @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = {WebserverApplication.class, CarserviceApplication.class})
        }
)

@VertxServiceProxyScan(basePackageClasses = {ServiceMarketApplication.class, CarserviceApplication.class})
public class ServiceMarketApplication {

    public static void main(String[] args) {
        SpringApplication.run(ServiceMarketApplication.class, args);
    }

}
