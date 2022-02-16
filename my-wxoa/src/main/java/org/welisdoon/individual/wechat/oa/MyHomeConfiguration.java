package org.welisdoon.individual.wechat.oa;

import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.client.WebClient;
import io.vertx.ext.web.handler.BodyHandler;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.welisdoon.common.JAXBUtils;
import org.welisdoon.web.common.ApplicationContextProvider;
import org.welisdoon.web.common.config.AbstractWechatConfiguration;
import org.welisdoon.web.common.web.AsyncProxyUtils;
import org.welisdoon.web.common.web.Requset;
import org.welisdoon.web.common.web.intf.ICommonAsynService;
import org.welisdoon.web.entity.wechat.messeage.MesseageTypeValue;
import org.welisdoon.web.entity.wechat.messeage.request.RequestMesseage;
import org.welisdoon.web.entity.wechat.messeage.request.RequestMesseageBody;
import org.welisdoon.web.entity.wechat.messeage.response.ResponseMesseage;
import org.welisdoon.web.service.wechat.service.AbstractWeChatService;
import org.welisdoon.web.vertx.annotation.VertxConfiguration;
import org.welisdoon.web.vertx.annotation.VertxRegister;
import org.welisdoon.web.vertx.annotation.VertxRoutePath;
import org.welisdoon.web.vertx.annotation.VertxRouter;
import org.welisdoon.web.vertx.enums.VertxRouteType;
import org.welisdoon.web.vertx.utils.RoutingContextChain;
import org.welisdoon.web.vertx.verticle.StandaredVerticle;

import java.util.Map;
import java.util.function.Consumer;

/**
 * @Classname MyHomeConfiguration
 * @Description TODO
 * @Author wang.zhidong
 * @Date 2022/2/16 14:51
 */
@VertxConfiguration
@ConfigurationProperties("wechat-oa-myhome")
@Configuration
@ConditionalOnProperty(prefix = "wechat-oa-myhome", name = "appID")
@VertxRoutePath("{wechat-oa-myhome.path.app}")
public class MyHomeConfiguration extends AbstractWechatConfiguration {

    @VertxRouter(path = "", order = Integer.MIN_VALUE)
    void bodyHandler(RoutingContextChain chain) {
        chain.handler(BodyHandler.create());
    }


    @VertxRouter(path = "", method = "GET")
    void wxGet(RoutingContextChain chain) {
        chain.handler(this::wechatMsgCheck);
    }

    @VertxRouter(path = "", method = "POST", mode = VertxRouteType.PathRegex)
    void wxPost(RoutingContextChain chain) {
        AbstractWeChatService weChatService = AbstractWeChatService.findService(this.getClass());
        chain.handler(this::wechatDecryptMsg)
                .handler(weChatService::receive)
                .handler(this::wechatEncryptMsg)
                .failureHandler(routingContext -> {
                    routingContext.response().end(MesseageTypeValue.MSG_REPLY);
                });
    }


    @VertxRegister(StandaredVerticle.class)
    public Consumer<Vertx> standaredverticle() {
        return vertx -> {
            WebClient webClient = WebClient.create(vertx);
            setWechatAsyncMeassger(webClient);
            initApiAsyncMeassger(vertx);

            this.initAccessTokenSyncTimer(vertx, objectMessage -> {
                this.setAccessToken(this.getTokenFromMessage(objectMessage));
            });
        };
    }
}
