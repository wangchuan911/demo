package org.welisdoon.web.common.config;

import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.JsonObject;
import io.vertx.core.net.PemKeyCertOptions;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.client.HttpResponse;
import io.vertx.ext.web.client.WebClient;
import io.vertx.ext.web.client.WebClientOptions;
import org.welisdoon.common.JAXBUtils;
import org.welisdoon.web.common.ApplicationContextProvider;
import org.welisdoon.web.common.CommonConst;
import org.welisdoon.web.common.WechatAsyncMeassger;
import org.welisdoon.web.entity.wechat.WeChatMarketTransferOrder;
import org.welisdoon.web.entity.wechat.WeChatPayOrder;
import org.welisdoon.web.entity.wechat.WeChatRefundOrder;
import org.welisdoon.web.entity.wechat.WeChatUser;
import org.welisdoon.web.entity.wechat.payment.requset.PayBillRequsetMesseage;
import org.welisdoon.web.entity.wechat.payment.requset.RefundResultMesseage;
import org.welisdoon.web.entity.wechat.payment.response.MarketTransferResponseMesseage;
import org.welisdoon.web.entity.wechat.payment.response.PrePayResponseMesseage;
import org.welisdoon.web.entity.wechat.payment.response.RefundResponseMesseage;
import org.welisdoon.web.service.wechat.intf.IWechatPayHandler;
import org.welisdoon.web.service.wechat.intf.WechatLoginHandler;
import org.welisdoon.web.vertx.annotation.VertxRegister;
import org.welisdoon.web.vertx.verticle.StandaredVerticle;

import javax.xml.bind.JAXBException;
import java.util.Map;
import java.util.Optional;

/**
 * @Classname AbstractWechatMiniProgramsConfiguration
 * @Description TODO
 * @Author wang.zhidong
 * @Date 2022/2/19 13:44
 */
public class AbstractWechatMiniProgramsConfiguration extends AbstractWechatConfiguration {

    protected WechatAsyncMeassger mchApiAsyncMeassger;
    protected Merchant merchant;

    public <T> Future<T> getWeChatCode2session(String jsCode, WechatLoginHandler<T> wechatUserHandler) {
        return Future.future(jsonObjectPromise -> {
            this.wechatAsyncMeassger.getWebClient().getAbs(this.getUrls().get(CommonConst.WecharUrlKeys.CODE_2_SESSION).toString() + jsCode)
                    .send(httpResponseAsyncResult -> {
                        try {
                            if (httpResponseAsyncResult.succeeded()) {
                                HttpResponse<Buffer> httpResponse = httpResponseAsyncResult.result();
                                JsonObject jsonObject = httpResponse.body().toJsonObject();
                                logger.info(jsonObject.toString());
                                wechatUserHandler.login(new WeChatUser()
                                        .setOpenId(jsonObject.getString("openid"))
                                        .setSessionKey(jsonObject.getString("session_key"))
                                        .setUnionid(jsonObject.getString("unionid")))
                                        .onSuccess(jsonObjectPromise::complete)
                                        .onFailure(jsonObjectPromise::fail);
                                return;
                            }
                            throw httpResponseAsyncResult.cause();
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
                /*PrePayRequsetMesseage prePayRequsetMesseage = iWechatPayHandler.payRequset(weChatPayOrder);*/
                iWechatPayHandler.payRequset(weChatPayOrder).onSuccess(prePayRequsetMesseage -> {
                    try {
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
                }).onFailure(jsonObjectPromise::fail);
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
            /*PayBillResponseMesseage payBillResponseMesseage = iWechatPayHandler.payCallBack(payBillRequsetMesseage);
            routingContext.response()
                    .end(Buffer.buffer(JAXBUtils.toXML(payBillResponseMesseage)));*/
            iWechatPayHandler.payCallBack(payBillRequsetMesseage)
                    .onSuccess(payBillResponseMesseage -> {
                        try {
                            routingContext.response()
                                    .end(Buffer.buffer(JAXBUtils.toXML(payBillResponseMesseage)));
                        } catch (JAXBException e) {
                            logger.error(e.getMessage(), e);
                            routingContext.fail(e);
                        }
                    })
                    .onFailure(routingContext::fail)
            ;
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
                /*RefundRequestMesseage requset = iWechatPayHandler.refundRequset(weChatRefundOrder);*/
                iWechatPayHandler.refundRequset(weChatRefundOrder).onSuccess(requset -> {
                    try {
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
                        jsonObjectPromise.fail(e);
                    }
                }).onFailure(jsonObjectPromise::fail);
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
            /*RefundReplyMesseage responseMesseage = iWechatPayHandler.refundCallBack(resultMesseage);*/
            iWechatPayHandler.refundCallBack(resultMesseage).onSuccess(responseMesseage -> {
                try {
                    routingContext.response()
                            .end(Buffer.buffer(JAXBUtils.toXML(responseMesseage)));
                } catch (JAXBException e) {
                    logger.error(e.getMessage(), e);
                    routingContext.fail(e);
                }
            }).onFailure(routingContext::fail);

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
                /* MarketTransferRequsetMesseage requset = iWechatPayHandler.marketTransferRequset(transferOrder);*/
                iWechatPayHandler.marketTransferRequset(transferOrder).onSuccess(requset -> {
                    try {

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

    @Override
    @VertxRegister(StandaredVerticle.class)
    public synchronized void vertxConfiguration(Vertx vertx) {
        super.vertxConfiguration(vertx);
        initApiAsyncMeassger(vertx);
    }

    protected synchronized void initApiAsyncMeassger(Vertx vertx) {
        if (this.mchApiAsyncMeassger != null || this.merchant == null)
            return;
        this.mchApiAsyncMeassger = new WechatAsyncMeassger(urls, WebClient.create(vertx, new WebClientOptions().setPemKeyCertOptions(new PemKeyCertOptions().setKeyPath(this.merchant.keyPath).setCertPath(this.merchant.certPath))), this::getAccessToken);
    }

    public WechatAsyncMeassger getMchApiAsyncMeassger() {
        return mchApiAsyncMeassger;
    }

}
