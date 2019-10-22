package pers.welisdoon.webserver.service.custom.config;

import java.util.Map;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import pers.welisdoon.webserver.common.config.AbstractWechatConfiguration;

@Configuration
@ConfigurationProperties("wechat-app")
public class TestConfiguration extends AbstractWechatConfiguration {

    @PostConstruct
    public void aVoid(){
        System.out.println(this.getUrls());
    }
}
