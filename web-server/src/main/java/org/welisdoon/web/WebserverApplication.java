package org.welisdoon.web;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.welisdoon.web.vertx.proxy.factory.VertxServiceProxyScan;

@SpringBootApplication
@EnableTransactionManagement
@VertxServiceProxyScan(basePackageClasses = WebserverApplication.class)
public class WebserverApplication {

    public static void main(String[] args) {
        SpringApplication.run(WebserverApplication.class, args);
    }

}
