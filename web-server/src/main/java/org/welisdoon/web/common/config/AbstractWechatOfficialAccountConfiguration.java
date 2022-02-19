package org.welisdoon.web.common.config;

import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.ext.web.RoutingContext;
import org.springframework.util.StringUtils;
import org.welisdoon.web.common.encrypt.AesException;
import org.welisdoon.web.entity.wechat.messeage.MesseageTypeValue;
import org.welisdoon.web.vertx.annotation.VertxRegister;
import org.welisdoon.web.vertx.verticle.StandaredVerticle;

/**
 * @Classname AbstractWechatOfficialAccountConfiguration
 * @Description TODO
 * @Author wang.zhidong
 * @Date 2022/2/19 13:39
 */
public abstract class AbstractWechatOfficialAccountConfiguration extends AbstractWechatConfiguration {


    public void wechatDecryptMsg(RoutingContext routingContext) {
        routingContext.response().setChunked(true);
        //解密
        try {
            HttpServerRequest httpServerRequest = routingContext.request();
            String signature = httpServerRequest.getParam(MesseageTypeValue.MSG_PARM_MSG_SIGNATURE);
            String timeStamp = httpServerRequest.getParam(MesseageTypeValue.MSG_PARM_TIMESTAMP);
            String nonce = httpServerRequest.getParam(MesseageTypeValue.MSG_PARM_NONCE);

            if (StringUtils.isEmpty(timeStamp) && StringUtils.isEmpty(nonce)) {
                return;
            }

            Buffer requestbuffer = routingContext.getBody();
            requestbuffer = Buffer.buffer(wxBizMsgCrypt.decryptMsg(signature, timeStamp, nonce, requestbuffer.toString()));
            routingContext.setBody(requestbuffer);

        } catch (AesException e) {
            e.printStackTrace();
        } finally {
            routingContext.next();
        }
    }

    public void wechatEncryptMsg(RoutingContext routingContext) {
        Buffer requestbuffer = routingContext.getBody();
        try {

            HttpServerRequest httpServerRequest = routingContext.request();
            String timeStamp = httpServerRequest.getParam(MesseageTypeValue.MSG_PARM_TIMESTAMP);
            String nonce = httpServerRequest.getParam(MesseageTypeValue.MSG_PARM_NONCE);


            if (StringUtils.isEmpty(timeStamp) || StringUtils.isEmpty(nonce)) {
                return;
            }

            requestbuffer = Buffer.buffer(wxBizMsgCrypt.encryptMsg(requestbuffer.toString(), timeStamp, nonce));
        } catch (AesException e) {
            e.printStackTrace();
        } finally {
            routingContext.response().end(requestbuffer);
        }

    }

    @Override
    @VertxRegister(StandaredVerticle.class)
    public synchronized void vertxConfiguration(Vertx vertx) {
        super.vertxConfiguration(vertx);
    }
}
