package org.welisdoom.task.xml;

import org.mybatis.spring.annotation.MapperScan;
import org.reflections.ReflectionUtils;
import org.reflections.Reflections;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.welisdoom.task.xml.handler.SAXParserHandler;
import org.welisdoon.web.WebserverApplication;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import javax.annotation.PostConstruct;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.net.URL;
import java.util.*;

/**
 * @Classname XmlTaslApplication
 * @Description TODO
 * @Author Septem
 * @Date 17:06
 */

@SpringBootApplication
//@EnableTransactionManagement(proxyTargetClass = true)
//@MapperScan(basePackageClasses = XmlTaskApplication.class, annotationClass = Repository.class)
@ComponentScan(basePackageClasses = {WebserverApplication.class, XmlTaskApplication.class})
public class XmlTaskApplication {

    public static void main(String[] args) {
        SpringApplication.run(XmlTaskApplication.class, args);
    }

    @PostConstruct
    public void run() {
        try {
            SAXParserHandler.loadTask("src\\main\\resources\\xml\\demo.xml").run(new HashMap<>(Map.of("input1", 1)));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
