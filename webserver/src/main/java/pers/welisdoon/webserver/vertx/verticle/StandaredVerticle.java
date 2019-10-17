package pers.welisdoon.webserver.vertx.verticle;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

@Component("standaredVerticle")
@ConfigurationProperties(prefix = "server.web")
public class StandaredVerticle extends AbstractWebVerticle {

}


