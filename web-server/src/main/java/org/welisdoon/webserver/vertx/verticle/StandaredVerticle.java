package org.welisdoon.webserver.vertx.verticle;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.welisdoon.webserver.vertx.annotation.Verticle;

@Component("standaredVerticle")
@ConfigurationProperties(prefix = "server.web")
@Verticle
public class StandaredVerticle extends AbstractWebVerticle {

}


