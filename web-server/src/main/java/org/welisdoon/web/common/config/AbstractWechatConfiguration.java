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
    protected WechatAsyncMeassger mchApiAsyncMeassger;
    protected boolean readOnly = false;
    protected String classPath;
    protected Merchant merchant;
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


    public <T> void initAccessTokenSyncTimer(Vertx vertx1, Handler<Message<T>> var1) {
        final String key = "WX.TOKEN";
        final String URL_TOCKEN_LOCK = String.format("%s.%s.LOCK", key, this.getAppID());
        final String URL_TOCKEN_UPDATE = String.format("%s.%s.UPDATE", key, this.getAppID());
        final String URL_REQUSET = this.getUrls().get("getAccessToken").toString();
        EventBus eventBus = vertx1.eventBus();
        SharedData sharedData = vertx1.sharedData();
        WebClient webClient = WebClient.create(vertx1);
        Handler<Long> longHandler = avoid -> {
            sharedData.getLock(URL_TOCKEN_LOCK, lockAsyncResult -> {
                if (lockAsyncResult.succeeded()) {
//                    logger.info(URL_REQUSET);
                    webClient.getAbs(URL_REQUSET)
                            /*.addQueryParam("grant_type", "client_credential")
                            .addQueryParam("appid", this.getAppID())
                            .addQueryParam("secret", this.getAppsecret())*/
                            .timeout(20000)
                            .send(httpResponseAsyncResult -> {
                                try {
                                    if (httpResponseAsyncResult.succeeded()) {
                                        HttpResponse<Buffer> httpResponse = httpResponseAsyncResult.result();
                                        eventBus.publish(URL_TOCKEN_UPDATE, httpResponse.body().toJsonObject());
                                    } else {
                                        httpResponseAsyncResult.cause().printStackTrace();
                                    }
                                } finally {
                                    Lock lock = lockAsyncResult.result();
                                    vertx1.setTimer(30 * 1000, aLong1 -> {
                                        lock.release();
                                    });
                                }
                            });
                } else {
                    logger.info(lockAsyncResult.cause().getMessage());
                }
            });
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

    public <T> String getTokenFromMessage(Message<T> var1) {
        JsonObject tokenJson = (JsonObject) var1.body();
        if (tokenJson.getInteger("errcode") != null) {
            String error;
            logger.info(String.format("[%s]errcode:%s", this.getAppID(), tokenJson.getInteger("errcode")));
            logger.info(String.format("[%s]errmsg:%s", this.getAppID(), error = tokenJson.getString("errmsg")));
            throw new RuntimeException(error);
        } else {
            String accessToken = tokenJson.getString("access_token");
            logger.info(String.format("[%s]Token:%s[%s]", this.getAppID(), accessToken, tokenJson.getLong("expires_in")));
            return accessToken;
        }
    }

    public synchronized void setWechatAsyncMeassger(WebClient webClient) {
        if (this.wechatAsyncMeassger != null)
            return;
        this.wechatAsyncMeassger = new WechatAsyncMeassger(urls, webClient,this::getAccessToken);
    }

    public synchronized void initApiAsyncMeassger(Vertx vertx) {
        if (this.mchApiAsyncMeassger != null || this.merchant == null)
            return;
        this.mchApiAsyncMeassger = new WechatAsyncMeassger(urls, WebClient.create(vertx, new WebClientOptions().setPemKeyCertOptions(new PemKeyCertOptions().setKeyPath(this.merchant.keyPath).setCertPath(this.merchant.certPath))),this::getAccessToken);
    }

    public WechatAsyncMeassger getWechatAsyncMeassger() {
        return wechatAsyncMeassger;
    }

    public WechatAsyncMeassger getMchApiAsyncMeassger() {
        return mchApiAsyncMeassger;
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

    private void isReadOnly() {
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

    public <T> Future<T> getWeChatCode2session(String jsCode, WechatLoginHandler<T> wechatUserHandler) {
        return Future.future(jsonObjectPromise -> {
            this.wechatAsyncMeassger.getWebClient().getAbs(this.getUrls().get(CommonConst.WecharUrlKeys.CODE_2_SESSION).toString() + jsCode)
                    .send(httpResponseAsyncResult -> {
                        try {
                            if (httpResponseAsyncResult.succeeded()) {
                                HttpResponse<Buffer> httpResponse = httpResponseAsyncResult.result();
                                JsonObject jsonObject = httpResponse.body().toJsonObject();
                                logger.info(jsonObject.toString());
                                jsonObjectPromise.complete(wechatUserHandler.login(new WeChatUser()
                                        .setOpenId(jsonObject.getString("openid"))
                                        .setSessionKey(jsonObject.getString("session_key"))
                                        .setUnionid(jsonObject.getString("unionid"))));
                            } else {
                                jsonObjectPromise.fail(httpResponseAsyncResult.cause());
                            }
                        } catch (Throwable e) {
                            logger.error(e.getMessage(), e);
                            jsonObjectPromise.fail(e);
                        }
                    });
        });
    }

    public Future<JsonObject> wechatPayRequst(WeChatPayOrder weChatPayOrder) {
        return Future.future(jsonObjectPromise -> {
            try {
                Class<IWechatPayHandler> iWechatPayHandlerClass = (Class<IWechatPayHandler>) Class.forName(weChatPayOrder.getPayClass());
                IWechatPayHandler iWechatPayHandler = ApplicationContextProvider.getBean(iWechatPayHandlerClass);
                PrePayRequsetMesseage prePayRequsetMesseage = iWechatPayHandler.payRequset(weChatPayOrder);
                Buffer buffer = Buffer.buffer(JAXBUtils.toXML(prePayRequsetMesseage
                        .setAppId(this.getAppID())
                        .setMchId(this.getMerchant().getMchId())
                        .setNonceStr(weChatPayOrder.getNonce())
                        .setSpbillCreateIp(this.getNetIp())
                        .setNotifyUrl(this.getAddress() + this.getPath().getPayment().get(weChatPayOrder.getPayClass()).getPaid())
                        .setTradeType("JSAPI")
                        .setSign(this.getMerchant().getMchKey())
                ));
                this.wechatAsyncMeassger.getWebClient().postAbs(this.getUrls().get(CommonConst.WecharUrlKeys.UNIFIED_ORDER).toString())
                        .sendBuffer(buffer, httpResponseAsyncResult -> {
                            if (!httpResponseAsyncResult.succeeded()) {
                                jsonObjectPromise.fail(httpResponseAsyncResult.cause());
                                return;
                            }
                            try {
                                PrePayResponseMesseage prePayResponseMesseage = JAXBUtils.fromXML(httpResponseAsyncResult.result().bodyAsString(), PrePayResponseMesseage.class);
                                System.out.println(prePayResponseMesseage);
                                if (!CommonConst.WeChatPubValues.SUCCESS.equals(prePayResponseMesseage.getResultCode())) {
                                    throw new RuntimeException(String.format("支付失败:%s[%s]",
                                            prePayResponseMesseage.getReturnCode(),
                                            prePayResponseMesseage.getReturnMsg()));
                                } else {
                                    String sign = /*String.format("appId=%s&nonceStr=%s&package=prepay_id=%s&signType=MD5&timeStamp=%s&key=%s"
                                                ,*/ PrePayResponseMesseage.generateSign(
                                            this.getAppID()
                                            , weChatPayOrder.getNonce()
                                            , prePayResponseMesseage.getPrepayId()
                                            , weChatPayOrder.getTimeStamp()
                                            , this.getMerchant().getMchKey());
                                    JsonObject resultBodyJson = new JsonObject()
                                            .put("sign", /*DigestUtils.md5Hex(sign)*/sign)
                                            .put("prepayId", prePayResponseMesseage.getPrepayId())
                                            .put("nonce", weChatPayOrder.getNonce())
                                            .put("timeStamp", weChatPayOrder.getTimeStamp());
                                    jsonObjectPromise.complete(resultBodyJson);
                                }
                            } catch (Throwable t) {
                                logger.error(t.getMessage(), t);
                                jsonObjectPromise.fail(t);
                            }
                        });
            } catch (Throwable e) {
                logger.error(e.getMessage(), e);
                jsonObjectPromise.fail(e);
            }
        });

    }

    public void wechatPayResult(RoutingContext routingContext) {
        try {
            String path = routingContext.request().path();
            Optional<String> optional = getPath().getPayment().entrySet().stream().filter(paymentEntry ->
                    path.indexOf(paymentEntry.getValue().getPaid()) >= 0
            ).map(Map.Entry::getKey).findFirst();
            if (optional.isEmpty()) {
                routingContext.fail(404, new RuntimeException("无效的地址"));
                return;
            }
            Class<IWechatPayHandler> iWechatPayHandlerClass = (Class<IWechatPayHandler>) Class.forName(optional.get());
            IWechatPayHandler iWechatPayHandler = ApplicationContextProvider.getBean(iWechatPayHandlerClass);
            routingContext.response().setChunked(true);
            logger.info(String.format("%s,%s", "微信回调", routingContext.getBodyAsString()));
            PayBillRequsetMesseage payBillRequsetMesseage = JAXBUtils.fromXML(routingContext.getBodyAsString(), PayBillRequsetMesseage.class);
            PayBillResponseMesseage payBillResponseMesseage = iWechatPayHandler.payCallBack(payBillRequsetMesseage);
            routingContext.response()
                    .end(Buffer.buffer(JAXBUtils.toXML(payBillResponseMesseage)));
        } catch (JAXBException | ClassNotFoundException e) {
            logger.error(e.getMessage(), e);
            routingContext.fail(e);
        }

    }

    public Future<JsonObject> wechatRefundRequest(WeChatRefundOrder weChatRefundOrder) {
        return Future.future(jsonObjectPromise -> {
            try {
                Class<IWechatPayHandler> iWechatPayHandlerClass = (Class<IWechatPayHandler>) Class.forName(weChatRefundOrder.getPayClass());
                IWechatPayHandler iWechatPayHandler = ApplicationContextProvider.getBean(iWechatPayHandlerClass);
                RefundRequestMesseage requset = iWechatPayHandler.refundRequset(weChatRefundOrder);
                Buffer buffer = Buffer.buffer(JAXBUtils.toXML(requset
                        .setAppId(this.getAppID())
                        .setMchId(this.getMerchant().getMchId())
                        .setNonceStr(weChatRefundOrder.getNonce())
                        .setNotifyUrl(String.format("%s%s", this.getAddress(), this.getPath().getPayment().get(weChatRefundOrder.getPayClass()).getRefunded()))
                        .setSign(this.getMerchant().getMchKey())
                ));
/*
                Buffer buffer = Buffer.buffer(xml.startsWith("<?") ? (xml.substring(xml.indexOf("?>") + 2)) : xml);
*/
                logger.info(buffer.toString());
                this.mchApiAsyncMeassger.getWebClient().postAbs(this.getUrls().get(CommonConst.WecharUrlKeys.REFUND).toString())
                        .sendBuffer(buffer, httpResponseAsyncResult -> {
                            if (!httpResponseAsyncResult.succeeded()) {
                                jsonObjectPromise.fail(httpResponseAsyncResult.cause());
                                return;
                            }
                            try {
                                RefundResponseMesseage messeage = JAXBUtils.fromXML(httpResponseAsyncResult.result().bodyAsString(), RefundResponseMesseage.class);
                                System.out.println(messeage);
                                if (!CommonConst.WeChatPubValues.SUCCESS.equals(messeage.getReturnCode())) {
                                    throw new RuntimeException(String.format("退款失败:%s[%s,%s]",
                                            messeage.getReturnCode(),
                                            messeage.getReturnMsg(),
                                            messeage.getErrCodeDes()));
                                } else {
                                    JsonObject resultBodyJson = new JsonObject().put("success", messeage.getReturnMsg());
                                    jsonObjectPromise.complete(resultBodyJson);
                                }
                            } catch (Throwable t) {
                                logger.error(t.getMessage(), t);
                                jsonObjectPromise.fail(t);
                            }
                        });
            } catch (Throwable e) {
                logger.error(e.getMessage(), e);
                jsonObjectPromise.fail(e);
            }
        });
    }

    public void weChatRefundCallBack(RoutingContext routingContext) {
        try {
            String path = routingContext.request().path();
            Optional<String> optional = getPath().getPayment().entrySet().stream().filter(paymentEntry ->
                    paymentEntry.getValue().getRefunded() != null
                            && path.indexOf(paymentEntry.getValue().getRefunded()) >= 0
            ).map(Map.Entry::getKey).findFirst();
            if (optional.isEmpty()) {
                routingContext.fail(404, new RuntimeException("无效的地址"));
                return;
            }
            Class<IWechatPayHandler> iWechatPayHandlerClass = (Class<IWechatPayHandler>) Class.forName(optional.get());
            IWechatPayHandler iWechatPayHandler = ApplicationContextProvider.getBean(iWechatPayHandlerClass);
            routingContext.response().setChunked(true);
            logger.info(String.format("%s,%s", "微信回调", routingContext.getBodyAsString()));
            RefundResultMesseage resultMesseage = JAXBUtils.fromXML(routingContext.getBodyAsString(), RefundResultMesseage.class).decrypt(this.getMerchant().getMchKey());
            RefundReplyMesseage responseMesseage = iWechatPayHandler.refundCallBack(resultMesseage);
            routingContext.response()
                    .end(Buffer.buffer(JAXBUtils.toXML(responseMesseage)));
        } catch (JAXBException | ClassNotFoundException e) {
            logger.error(e.getMessage(), e);
            routingContext.fail(e);
        }

    }

    public Future<JsonObject> wechatMarketTransfersRequest(WeChatMarketTransferOrder transferOrder) {
        return Future.future(jsonObjectPromise -> {
            try {
                Class<IWechatPayHandler> iWechatPayHandlerClass = (Class<IWechatPayHandler>) Class.forName(transferOrder.getPayClass());
                IWechatPayHandler iWechatPayHandler = ApplicationContextProvider.getBean(iWechatPayHandlerClass);
                MarketTransferRequsetMesseage requset = iWechatPayHandler.marketTransferRequset(transferOrder);
                Buffer buffer = Buffer.buffer(JAXBUtils.toXML(requset
                        .setMchAppid(this.getAppID())
                        .setMchId(this.getMerchant().getMchId())
                        .setNonceStr(transferOrder.getNonce())
                        .setSign(this.getMerchant().getMchKey())
                ));
/*
                Buffer buffer = Buffer.buffer(xml.startsWith("<?") ? (xml.substring(xml.indexOf("?>") + 2)) : xml);
*/
                logger.info(buffer.toString());
                this.mchApiAsyncMeassger.getWebClient().postAbs(this.getUrls().get(CommonConst.WecharUrlKeys.MARKET_TRANSFER).toString())
                        .sendBuffer(buffer, httpResponseAsyncResult -> {
                            if (!httpResponseAsyncResult.succeeded()) {
                                jsonObjectPromise.fail(httpResponseAsyncResult.cause());
                                return;
                            }
                            try {
                                MarketTransferResponseMesseage messeage = JAXBUtils.fromXML(httpResponseAsyncResult.result().bodyAsString(), MarketTransferResponseMesseage.class);
                                System.out.println(messeage);
                                if (CommonConst.WeChatPubValues.SUCCESS.equals(messeage.getReturnCode()) &&
                                        CommonConst.WeChatPubValues.SUCCESS.equals(messeage.getResultCode())) {
                                    JsonObject resultBodyJson = new JsonObject().put("success", messeage.getReturnMsg());
                                    jsonObjectPromise.complete(resultBodyJson);
                                    return;
                                }
                                throw new RuntimeException(String.format("结算失败:%s[%s,%s]",
                                        messeage.getReturnCode(),
                                        messeage.getReturnMsg(),
                                        messeage.getErrCodeDes()));
                            } catch (Throwable t) {
                                logger.error(t.getMessage(), t);
                                jsonObjectPromise.fail(t);
                            }
                        });
            } catch (Throwable e) {
                logger.error(e.getMessage(), e);
                jsonObjectPromise.fail(e);
            }
        });
    }

    public Merchant getMerchant() {
        return merchant;
    }

    public void setMerchant(Merchant merchant) {
        this.isReadOnly();
        this.merchant = merchant;
    }

    public static class Merchant {
        String certPath, keyPath;
        private String mchId;
        private String mchKey;

        public String getCertPath() {
            return certPath;
        }

        public void setCertPath(String certPath) {
            this.certPath = certPath;
        }

        public String getKeyPath() {
            return keyPath;
        }

        public void setKeyPath(String keyPath) {
            this.keyPath = keyPath;
        }

        public String getMchKey() {
            return mchKey;
        }

        public void setMchKey(String mchKey) {
            this.mchKey = mchKey;
        }

        public String getMchId() {
            return mchId;
        }

        public void setMchId(String mchId) {
            this.mchId = mchId;
        }
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public String getAccessToken() {
        return this.accessToken;
    }
}
