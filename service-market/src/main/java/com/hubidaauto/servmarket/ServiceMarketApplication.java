package com.hubidaauto.servmarket;

import com.hubidaauto.servmarket.module.order.service.BaseOrderService;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.context.annotation.FilterType;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.welisdoon.common.object.wrapper.execute.SpringProxyProcessor;
import org.welisdoon.flow.OrderFlowApplication;
import org.welisdoon.web.MySpringApplication;
import org.welisdoon.web.WebserverApplication;
import org.welisdoon.web.config.condition.BeanRemoveProcessor;
import org.welisdoon.web.vertx.proxy.factory.VertxServiceProxyScan;

@SpringBootApplication
@EnableTransactionManagement(proxyTargetClass = true)
@MapperScan(basePackageClasses = {ServiceMarketApplication.class, OrderFlowApplication.class}, annotationClass = Repository.class)
@ComponentScan(
        basePackageClasses = {WebserverApplication.class, ServiceMarketApplication.class, OrderFlowApplication.class},
        excludeFilters = {
                @ComponentScan.Filter(type = FilterType.CUSTOM, classes = BeanRemoveProcessor.class)
        }
)
@EnableAspectJAutoProxy(proxyTargetClass = true)
@VertxServiceProxyScan(basePackageClasses = {ServiceMarketApplication.class})
public class ServiceMarketApplication extends MySpringApplication {

    public static void main(String[] args) {
        new SpringProxyProcessor(BaseOrderService.class);
        MySpringApplication.run(ServiceMarketApplication.class, args);
    }

}
