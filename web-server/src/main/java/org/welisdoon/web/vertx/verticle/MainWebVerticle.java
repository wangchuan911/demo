package org.welisdoon.web.vertx.verticle;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.welisdoon.web.vertx.annotation.Verticle;

@Component("standaredVerticle")
@ConfigurationProperties(prefix = "vertx.webs.main")
@Verticle
public class MainWebVerticle extends AbstractWebVerticle {

}


