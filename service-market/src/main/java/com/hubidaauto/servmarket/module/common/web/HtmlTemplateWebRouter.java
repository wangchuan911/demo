package com.hubidaauto.servmarket.module.common.web;

import com.hubidaauto.servmarket.module.common.entity.ImageContentVO;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.file.FileSystem;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.common.template.TemplateEngine;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.handler.StaticHandler;
import io.vertx.ext.web.handler.TemplateHandler;
import io.vertx.ext.web.templ.thymeleaf.ThymeleafTemplateEngine;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;
import org.welisdoon.common.ObjectUtils;
import org.welisdoon.web.vertx.annotation.VertxConfiguration;
import org.welisdoon.web.vertx.annotation.VertxRegister;
import org.welisdoon.web.vertx.annotation.VertxRoutePath;
import org.welisdoon.web.vertx.annotation.VertxRouter;
import org.welisdoon.web.vertx.enums.VertxRouteType;
import org.welisdoon.web.vertx.utils.RoutingContextChain;
import org.welisdoon.web.vertx.verticle.StandaredVerticle;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * @Classname HtmlTemplateConfiguration
 * @Description TODO
 * @Author wang.zhidong
 * @Date 2021/9/27 21:53
 */
@VertxConfiguration
@Component
@VertxRoutePath("/template")
public class HtmlTemplateWebRouter {

    TemplateEngine engine;
    FileSystem fileSystem;

    @Value("${temp.filePath}")
    String tempFilePath;

    @VertxRegister(StandaredVerticle.class)
    public Consumer<Vertx> html() {
        Consumer<Vertx> consumer = vertx -> {
            setTemplateEngine(vertx);
            this.fileSystem = vertx.fileSystem();
        };
        return consumer;
    }

    @VertxRouter(path = "/*", order = 1)
    public void page(RoutingContextChain chain) {
        chain.handler(TemplateHandler.create(engine));
    }


    @VertxRouter(path = "(?<app>/[\\w\\-]+)\\/(?<path>[\\w\\.\\-\\/]+).html", mode = VertxRouteType.PathRegex)
    public void toPage(RoutingContextChain chain) {
        chain.handler(BodyHandler.create()).handler(routingContext -> {
            Map map = new HashMap<>();
            map.putAll(Map.ofEntries(routingContext.request().params().entries().stream().toArray(Map.Entry[]::new)));
            //map.putAll(routingContext.data());
            this.toPage(routingContext, JsonObject.mapFrom(map), routingContext.pathParam("path"));
        });
    }

    public void toPage(RoutingContext routingContext, JsonObject jsonObject, String page) {
        this.engine.render(jsonObject, String.format("templates/%s.html", page), res -> {
            if (res.succeeded()) {
                routingContext.response().putHeader("Content-Type", "text/html").end(res.result());
            } else {
                res.cause().printStackTrace();
                routingContext.fail(res.cause());
            }
        });
    }

    public void cacheImage(RoutingContext routingContext, String filePath, Supplier<ImageContentVO> getImage) {
        routingContext.response().setChunked(true);
        StringBuilder stringBuilder = new StringBuilder(tempFilePath);
        stringBuilder.append(filePath.charAt(0) == '/' ? "" : "/").append(filePath);
        String filePaht = stringBuilder.toString();
        fileSystem.exists(filePaht).onSuccess(aBoolean -> {
            if (aBoolean) {
                routingContext.next();
            } else {
                ImageContentVO image = getImage.get();
                if (image == null) {
                    failHandler(routingContext, new RuntimeException("没有数据"));
                    return;
                }
                fileSystem.writeFile(stringBuilder.toString(), Buffer.buffer(image.getContent()))
                        .onSuccess(unused -> {
                            routingContext.next();
                        })
                        .onFailure(throwable -> failHandler(routingContext, throwable));
            }
        }).onFailure(throwable -> failHandler(routingContext, throwable));
    }

    public void failHandler(RoutingContext routingContext, Throwable throwable) {
        routingContext.response().setStatusCode(404)
                .end(StringUtils.isEmpty(throwable.getMessage()) ? "not data" : throwable.getMessage());
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
