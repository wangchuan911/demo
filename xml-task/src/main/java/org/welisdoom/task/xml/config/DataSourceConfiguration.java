package org.welisdoom.task.xml.config;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Repository;
import org.welisdoom.task.xml.XmlTaskApplication;

/**
 * @Classname DataSourceConfiguration
 * @Description TODO
 * @Author Septem
 * @Date 16:32
 */
@Configuration
@MapperScan(basePackageClasses = XmlTaskApplication.class, annotationClass = Repository.class)
@ConditionalOnProperty(prefix = "spring.datasource", name = "enabled", havingValue = "true")
public class DataSourceConfiguration {
}
