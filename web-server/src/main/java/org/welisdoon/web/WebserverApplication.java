package org.welisdoon.web;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.welisdoon.web.vertx.proxy.factory.VertxServiceProxyScan;

@SpringBootApplication
@EnableTransactionManagement
@VertxServiceProxyScan(basePackageClasses = WebserverApplication.class)
public class WebserverApplication extends MySpringApplication {

    public static void main(String[] args) {
        MySpringApplication.run(WebserverApplication.class, args);
    }
}
