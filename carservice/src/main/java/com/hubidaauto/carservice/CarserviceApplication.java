package com.hubidaauto.carservice;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.welisdoon.webserver.WebserverApplication;

@SpringBootApplication
@EnableTransactionManagement(proxyTargetClass=true)
@MapperScan(basePackageClasses = CarserviceApplication.class, annotationClass = Repository.class)
@ComponentScan(basePackageClasses = {WebserverApplication.class, CarserviceApplication.class})
public class CarserviceApplication {

    public static void main(String[] args) {
        SpringApplication.run(CarserviceApplication.class, args);
    }

}
