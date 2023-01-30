package org.welisdoon.web.common.config;

import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.eventbus.Message;
import io.vertx.core.eventbus.MessageConsumer;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.json.JsonObject;
import io.vertx.core.net.PemKeyCertOptions;
import io.vertx.core.shareddata.Lock;
import io.vertx.core.shareddata.SharedData;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.client.HttpResponse;
import io.vertx.ext.web.client.WebClient;
import io.vertx.ext.web.client.WebClientOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;
import org.welisdoon.web.common.ApplicationContextProvider;
import org.welisdoon.web.common.CommonConst;
import org.welisdoon.common.JAXBUtils;
import org.welisdoon.web.common.WechatAsyncMeassger;
import org.welisdoon.web.common.encrypt.AesException;
import org.welisdoon.web.common.encrypt.WXBizMsgCrypt;
import org.welisdoon.web.common.web.AsyncProxyUtils;
import org.welisdoon.web.entity.wechat.WeChatMarketTransferOrder;
import org.welisdoon.web.entity.wechat.WeChatPayOrder;
import org.welisdoon.web.entity.wechat.WeChatRefundOrder;
import org.welisdoon.web.entity.wechat.WeChatUser;
import org.welisdoon.web.entity.wechat.messeage.MesseageTypeValue;
import org.welisdoon.web.entity.wechat.payment.requset.*;
import org.welisdoon.web.entity.wechat.payment.response.*;
import org.welisdoon.web.service.wechat.intf.IWechatPayHandler;
import org.welisdoon.web.service.wechat.intf.WechatLoginHandler;
import org.welisdoon.web.vertx.annotation.VertxRegister;
import org.welisdoon.web.vertx.proxy.IVertxInvoker;
import org.welisdoon.web.vertx.verticle.WorkerVerticle;

import javax.annotation.PostConstruct;
import javax.xml.bind.JAXBException;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;

public abstract class AbstractWechatConfiguration {
    public final Logger logger = LoggerFactory.getLogger(ApplicationContextProvider.getRealClass(this.getClass()));

    protected String appID;
    protected String appsecret;
    protected String token;
    protected String key;
    protected Integer afterUpdateTokenTime;
    protected Map urls;
    protected Map schedul;
    protected String appName;
    protected String address;
    protected Path path;
    protected String netIp;
    protected WXBizMsgCrypt wxBizMsgCrypt;
    protected WechatAsyncMeassger wechatAsyncMeassger;
    protected boolean readOnly = false;
    protected String classPath;
    protected String accessToken;

    public String getAppID() {
        return appID;
    }

    public void setAppID(String appID) {
        this.isReadOnly();
        this.appID = appID;
    }

    public String getAppsecret() {
        return appsecret;
    }

    public void setAppsecret(String appsecret) {
        this.isReadOnly();
        this.appsecret = appsecret;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.isReadOnly();
        this.token = token;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.isReadOnly();
        this.key = key;
    }

    public Integer getAfterUpdateTokenTime() {
        return afterUpdateTokenTime;
    }

    public void setAfterUpdateTokenTime(Integer afterUpdateTokenTime) {
        this.afterUpdateTokenTime = afterUpdateTokenTime;
    }

    public Map getUrls() {
        return urls;
    }

    public void setUrls(Map urls) {
        this.isReadOnly();
        this.urls = urls;
    }

    public Map getSchedul() {
        return schedul;
    }

    public void setSchedul(Map schedul) {
        this.isReadOnly();
        this.schedul = schedul;
    }


    public String getAppName() {
        return appName;
    }

    public void setAppName(String appName) {
        this.isReadOnly();
        this.appName = appName;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.isReadOnly();
        this.address = address;
    }

    public Path getPath() {
        return path;
    }

    public void setPath(Path path) {
        this.isReadOnly();
        this.path = path;
    }


    public String getNetIp() {
        if (StringUtils.isEmpty(this.netIp)) {
            try {
                URL whatismyip = new URL("http://checkip.amazonaws.com");
                BufferedReader in = new BufferedReader(new InputStreamReader(
                        whatismyip.openStream()));
                this.netIp = in.readLine();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return netIp;
    }

    public void setNetIp(String netIp) {
        this.isReadOnly();
        this.netIp = netIp;
    }

    public static class Path {
        Map<String, Payment> payment;
        String app;
        String push;
        String appIndex;
        Map<String, String> other;

        public Map<String, Payment> getPayment() {
            return payment;
        }

        public void setPayment(Map<String, Payment> payment) {
            this.payment = payment;
        }

        public String getApp() {
            return app;
        }

        public void setApp(String app) {
            this.app = app;
        }

        public String getPush() {
            return push;
        }

        public void setPush(String push) {
            this.push = push;
        }

        public String getAppIndex() {
            return appIndex;
        }

        public void setAppIndex(String appIndex) {
            this.appIndex = appIndex;
        }

        public Map<String, String> getOther() {
            return other;
        }

        public void setOther(Map<String, String> other) {
            this.other = other;
        }

        public static class Payment {
            String paying, paid, refunded, refunding;

            public String getPaying() {
                return paying;
            }

            public void setPaying(String paying) {
                this.paying = paying;
            }

            public String getPaid() {
                return paid;
            }

            public void setPaid(String paid) {
                this.paid = paid;
            }

            public String getRefunded() {
                return refunded;
            }

            public void setRefunded(String refunded) {
                this.refunded = refunded;
            }

            public String getRefunding() {
                return refunding;
            }

            public void setRefunding(String refunding) {
                this.refunding = refunding;
            }
        }
    }

    public WXBizMsgCrypt getWXBizMsgCrypt() throws AesException {
        if (wxBizMsgCrypt == null) {
            synchronized (this) {
                if (wxBizMsgCrypt == null)
                    wxBizMsgCrypt = new WXBizMsgCrypt(this.getToken(), this.getKey(), this.getAppID());
            }
        }
        return wxBizMsgCrypt;
    }

    public void wechatMsgCheck(RoutingContext routingContext) {
        HttpServerRequest httpServerRequest = routingContext.request();
        String signature = httpServerRequest.getParam("signature");
        String timestamp = httpServerRequest.getParam("timestamp");
        String nonce = httpServerRequest.getParam("nonce");
        String echostr = httpServerRequest.getParam("echostr");
        if (StringUtils.isEmpty(signature)
                || StringUtils.isEmpty(timestamp)
                || StringUtils.isEmpty(nonce)
                || StringUtils.isEmpty(echostr)) {
            routingContext.response().end(String.format("[%s]interl server error", this.getAppID()));
            return;
        }
        try {
            routingContext.response().end(wxBizMsgCrypt.verifyUrl2(signature, timestamp, nonce, echostr));
            logger.info(String.format("[%s]wechat token check success!", this.getAppID()));
        } catch (Exception e) {
            logger.error(String.format("[%s]wechat token check fail!", this.getAppID()), e);
            routingContext.response().end(MesseageTypeValue.MSG_REPLY);
        }
    }


    protected <T> void initAccessTokenSyncTimer(Vertx vertx1, Handler<Message<T>> var1) {
        final String key = "WX.TOKEN";
        final String URL_TOCKEN_LOCK = String.format("%s.%s.LOCK", key, this.getAppID());
        final String URL_TOCKEN_UPDATE = String.format("%s.%s.UPDATE", key, this.getAppID());
        final String URL_REQUSET = this.getUrls().get("getAccessToken").toString();
        EventBus eventBus = vertx1.eventBus();
        SharedData sharedData = vertx1.sharedData();
        WebClient webClient = WebClient.create(vertx1);
        Handler<Long> longHandler = avoid -> {
            sharedData.getLock(URL_TOCKEN_LOCK)
                    .compose(lock -> webClient.getAbs(URL_REQUSET).timeout(20000)
                            .send()
                            .onSuccess(result -> {
                                HttpResponse<Buffer> httpResponse = result;
                                eventBus.publish(URL_TOCKEN_UPDATE, httpResponse.body().toJsonObject());
                            })
                            .onComplete(event -> vertx1.timerStream(3000).handler(event1 -> lock.release())))
                    .onFailure(throwable -> logger.error(throwable.getMessage(), throwable));
        };

        MessageConsumer<T> messageConsumer = eventBus.consumer(URL_TOCKEN_UPDATE);
        messageConsumer.handler(var1);
        //启动就运行运行
        if (this.getAfterUpdateTokenTime() > 0) {
            longHandler.handle(null);
            //开始循环
            vertx1.setPeriodic(this.getAfterUpdateTokenTime() * 1000, longHandler);
        }
    }


    public <T> String getTokenFromMessage(Message<T> var1) {
        JsonObject tokenJson = (JsonObject) var1.body();
        if (tokenJson.getInteger("errcode") != null) {
            logger.error("[{}] ERR_CODE: {}", this.getAppID(), tokenJson.getInteger("errcode"));
            logger.error("[{}] ERR_MSG:  {}", this.getAppID(), tokenJson.getString("errmsg"));
            return null;
        } else {
            String accessToken = tokenJson.getString("access_token");
            logger.info("[{}] Token: {} [{}]", this.getAppID(), accessToken, LocalDateTime.now().plusSeconds(tokenJson.getLong("expires_in")).format(DateTimeFormatter.ofPattern("yyyy:MM:dd HH-mm-ss")));
            return accessToken;
        }
    }

    public void vertxConfiguration(Vertx vertx) {
        WebClient webClient = WebClient.create(vertx);
        setWechatAsyncMeassger(webClient);

        this.initAccessTokenSyncTimer(vertx, objectMessage -> {
            this.setAccessToken(this.getTokenFromMessage(objectMessage));
        });
    }

    public synchronized void setWechatAsyncMeassger(WebClient webClient) {
        if (this.wechatAsyncMeassger != null)
            return;
        this.wechatAsyncMeassger = new WechatAsyncMeassger(urls, webClient, this::getAccessToken);
    }


    public WechatAsyncMeassger getWechatAsyncMeassger() {
        return wechatAsyncMeassger;
    }


    final static Map<Class<?>, AbstractWechatConfiguration> WECHAT_CONFIGS = new HashMap(4);

    @PostConstruct
    public void initValue() throws Throwable {
        this.getWXBizMsgCrypt();

        Class<?> cls = this.getClass();
        if (WECHAT_CONFIGS.containsKey(cls))
            throw new RuntimeException(String.format("%s is exists", cls.getName()));
		/*if (cls.getName().indexOf("EnhancerBySpringCGLIB") > 0) {
			cls = cls.getSuperclass();
		}*/
        cls = ApplicationContextProvider.getRealClass(cls);
        WECHAT_CONFIGS.put(cls, this);

        readOnly = true;
    }

    public static <T extends AbstractWechatConfiguration> T getConfig(Class<T> clz) {
        return (T) WECHAT_CONFIGS.get(clz);
    }

    protected void isReadOnly() {
        if (readOnly) {
            throw new RuntimeException("configuration is init finish! please don't change it");
        }
    }

    public String getClassPath() {
        return classPath;
    }

    public void setClassPath(String classPath) {
        this.classPath = classPath;
    }


    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public String getAccessToken() {
        return this.accessToken;
    }
}
