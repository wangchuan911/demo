package org.welisdoon.webserver.vertx.verticle;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component("standaredVerticle")
@ConfigurationProperties(prefix = "server.web")
public class StandaredVerticle extends AbstractWebVerticle {

}


