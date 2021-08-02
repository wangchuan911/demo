package com.hubidaauto.servmarket;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.welisdoon.web.WebserverApplication;

@SpringBootApplication
@EnableTransactionManagement(proxyTargetClass=true)
@MapperScan(basePackageClasses = ServiceMarketApplication.class, annotationClass = Repository.class)
@ComponentScan(basePackageClasses = {WebserverApplication.class, ServiceMarketApplication.class})
public class ServiceMarketApplication {

    public static void main(String[] args) {
        SpringApplication.run(ServiceMarketApplication.class, args);
    }

}
