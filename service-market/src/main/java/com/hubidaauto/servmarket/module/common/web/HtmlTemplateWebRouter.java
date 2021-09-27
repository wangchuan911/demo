package com.hubidaauto.servmarket.module.common.web;

import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.common.template.TemplateEngine;
import io.vertx.ext.web.handler.TemplateHandler;
import io.vertx.ext.web.templ.thymeleaf.ThymeleafTemplateEngine;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;
import org.welisdoon.web.vertx.annotation.VertxConfiguration;
import org.welisdoon.web.vertx.annotation.VertxRegister;
import org.welisdoon.web.vertx.annotation.VertxRoutePath;
import org.welisdoon.web.vertx.annotation.VertxRouter;
import org.welisdoon.web.vertx.utils.RoutingContextChain;
import org.welisdoon.web.vertx.verticle.StandaredVerticle;

import javax.annotation.PostConstruct;
import java.util.function.Consumer;

/**
 * @Classname HtmlTemplateConfiguration
 * @Description TODO
 * @Author wang.zhidong
 * @Date 2021/9/27 21:53
 */
@VertxConfiguration
@Component
@ConditionalOnProperty(prefix = "wechat-app-hubida", name = "appID")
@VertxRoutePath("{wechat-app-hubida.path.app}/template")
public class HtmlTemplateWebRouter {

    TemplateEngine engine;

    @VertxRegister(StandaredVerticle.class)
    public Consumer<Vertx> html() {
        Consumer<Vertx> consumer = vertx -> {
            setTemplateEngine(vertx);
        };
        return consumer;
    }

    @VertxRouter(path = "/*", order = -1)
    public void page(RoutingContextChain chain) {
        chain.handler(TemplateHandler.create(engine));
    }


    public void goHtml(RoutingContext routingContext, JsonObject jsonObject, String page) {
        this.engine.render(jsonObject, String.format("templates/%s.html", page), res -> {
            if (res.succeeded()) {
                routingContext.response().putHeader("Content-Type", "text/html").end(res.result());
            } else {
                res.cause().printStackTrace();
                routingContext.fail(res.cause());
            }
        });
    }

    public void setTemplateEngine(Vertx vertx) {
        this.engine = ThymeleafTemplateEngine.create(vertx);
        {
            // 定时模板解析器,表示从类加载路径下找模板
            ClassLoaderTemplateResolver templateResolver = new ClassLoaderTemplateResolver();
      /*// 设置模板的前缀，我们设置的是templates目录
      templateResolver.setPrefix("templates");
            // 设置后缀为.html文件
      templateResolver.setSuffix(".html");*/
            templateResolver.setTemplateMode("HTML");
            templateResolver.setCharacterEncoding("utf-8");
            org.thymeleaf.TemplateEngine templateEngine = this.engine.unwrap();
            templateEngine.setTemplateResolver(templateResolver);
        }
    }
}
