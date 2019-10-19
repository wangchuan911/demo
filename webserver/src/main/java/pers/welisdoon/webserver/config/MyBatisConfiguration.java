package pers.welisdoon.webserver.config;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Repository;
import pers.welisdoon.webserver.WebserverApplication;
import pers.welisdoon.webserver.config.condition.MyBatisCondition;

@Configuration
@Conditional({MyBatisCondition.class})
@MapperScan(basePackageClasses = WebserverApplication.class, annotationClass = Repository.class)
public class MyBatisConfiguration {

}
