package pers.welisdoon.webserver.config;

import org.mybatis.spring.annotation.MapperScan;
import org.mybatis.spring.annotation.MapperScannerRegistrar;
import org.mybatis.spring.annotation.MapperScans;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import pers.welisdoon.webserver.config.condition.MyBatisCondition;

@Configuration
@Conditional({MyBatisCondition.class})
@MapperScan({"pers.welisdoon.webserver.service","pers.welisdoon.webserver.core"})
public class MyBatisConfiguration {

}
