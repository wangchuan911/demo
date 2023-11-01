package org.welisdoon.individual;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.context.annotation.FilterType;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.welisdoon.web.MySpringApplication;
import org.welisdoon.web.WebserverApplication;
import org.welisdoon.web.vertx.proxy.factory.VertxServiceProxyScan;

/**
 * @Classname IndividualMain
 * @Description TODO
 * @Author wang.zhidong
 * @Date 2022/2/16 14:46
 */
@SpringBootApplication
@EnableTransactionManagement(proxyTargetClass = true)
@MapperScan(basePackageClasses = {IndividualApplication.class}, annotationClass = Repository.class)
@ComponentScan(
        basePackageClasses = {WebserverApplication.class, IndividualApplication.class},
        excludeFilters = {
                @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = {WebserverApplication.class})
        }
)
@EnableAspectJAutoProxy(proxyTargetClass = true)
@VertxServiceProxyScan(basePackageClasses = {IndividualApplication.class})
public class IndividualApplication extends MySpringApplication {
    public static void main(String[] args) {
        MySpringApplication.run(IndividualApplication.class, args);
    }
}
