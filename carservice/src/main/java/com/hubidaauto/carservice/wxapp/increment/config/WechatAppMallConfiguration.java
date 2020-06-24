package com.hubidaauto.carservice.wxapp.increment.config;

import com.hubidaauto.carservice.wxapp.core.config.CustomConst;
import com.hubidaauto.carservice.wxapp.core.config.CustomWeChatAppConfiguration;
import com.hubidaauto.carservice.wxapp.increment.entity.MallOrderDto;
import com.hubidaauto.carservice.wxapp.increment.service.MallOrderService;
import io.vertx.core.MultiMap;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.client.WebClient;
import org.apache.commons.codec.digest.DigestUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.welisdoon.webserver.common.CommonConst;
import org.welisdoon.webserver.common.JAXBUtils;
import org.welisdoon.webserver.common.config.AbstractWechatConfiguration;
import org.welisdoon.webserver.entity.wechat.payment.requset.PayBillRequsetMesseage;
import org.welisdoon.webserver.entity.wechat.payment.requset.PrePayRequsetMesseage;
import org.welisdoon.webserver.entity.wechat.payment.response.PayBillResponseMesseage;
import org.welisdoon.webserver.entity.wechat.payment.response.PrePayResponseMesseage;
import org.welisdoon.webserver.vertx.annotation.VertxConfiguration;
import org.welisdoon.webserver.vertx.annotation.VertxRegister;
import org.welisdoon.webserver.vertx.verticle.StandaredVerticle;

import javax.annotation.PostConstruct;
import javax.xml.bind.JAXBException;
import java.util.Map;
import java.util.function.Consumer;

@Configuration
@VertxConfiguration
@AutoConfigureAfter(WechatAppMallConfiguration.class)
@ConditionalOnProperty(prefix = "wechat-app-hubida", name = "appID")
public class WechatAppMallConfiguration {
	private static final Logger logger = LoggerFactory.getLogger(WechatAppMallConfiguration.class);

	AbstractWechatConfiguration configuration;
	WebClient webClient;
	MallOrderService orderService;

	@PostConstruct
	void init() {
		configuration = AbstractWechatConfiguration.getConfig(CustomWeChatAppConfiguration.class);
	}

	@VertxRegister(StandaredVerticle.class)
	public Consumer<Router> routeMapping(Vertx vertx) {
		final String URL_UNIFIEDORDERON = configuration.getUrls().get(CommonConst.WecharUrlKeys.UNIFIED_ORDER).toString();
		webClient = WebClient.create(vertx);
		Consumer<Router> routerConsumer = router -> {
			//get请求入口
			router.get(configuration.getPath().getOther().get("mallPay")).handler(routingContext -> {
				routingContext.response().setChunked(true);
				MultiMap multiMap = routingContext.request().params();
				int code = Integer.parseInt(multiMap.get(CommonConst.WebParamsKeys.GET_CODE));
				String value = multiMap.get(CommonConst.WebParamsKeys.GET_VALUE_1);
				switch (code) {
					//获取支付信息
					case CustomConst.OTHER.MALL_PRE_PAY:
						try {
							int offset = 32;
							String nonce = value.substring(0, offset);
							String orderId = value.substring(offset, (offset = value.indexOf('.', offset)));
							String timeStamp = value.substring(++offset, (offset = value.indexOf('.', offset)));
							String custId = value.substring(++offset);

							MallOrderDto orderVo = (MallOrderDto) orderService
									.handle(CustomConst.GET, Map.of("id", orderId, "userId", custId));
							Buffer buffer = Buffer.buffer(JAXBUtils.toXML(new PrePayRequsetMesseage()
									.setAppId(configuration.getAppID())
									.setMchId(configuration.getMchId())
									.setNonceStr(nonce)
									.setBody(String.format("%s-购买商品结算:\n定单编号：%s\n金额%s", configuration.getAppName(), orderVo.getCode(), orderVo.getCost()))
									.setOutTradeNo(orderVo.getCode())
									.setTotalFee(orderVo.getCost())
									.setSpbillCreateIp(configuration.getNetIp())
									.setNotifyUrl(configuration.getAddress() + configuration.getPath().getOther().get("mallPay"))
									.setTradeType("JSAPI")
									.setOpenid(orderVo.getUserId())
									.setSign(configuration.getMchKey())
							));
							webClient.postAbs(URL_UNIFIEDORDERON)
									.sendBuffer(buffer, httpResponseAsyncResult -> {
										if (httpResponseAsyncResult.succeeded()) {
											JsonObject resultBodyJson = new JsonObject();
											try {
												PrePayResponseMesseage prePayResponseMesseage = JAXBUtils.fromXML(httpResponseAsyncResult.result().bodyAsString(), PrePayResponseMesseage.class);
												System.out.println(prePayResponseMesseage);
												if (!CommonConst.WeChatPubValues.SUCCESS.equals(prePayResponseMesseage.getResultCode())) {
													resultBodyJson
															.put("error", String.format("支付失败:%s[%s]",
																	prePayResponseMesseage.getErrCodeDes(),
																	prePayResponseMesseage.getErrCode()));
												} else if (!CommonConst.WeChatPubValues.SUCCESS.equals(prePayResponseMesseage.getReturnCode())) {
													resultBodyJson
															.put("error", String.format("支付失败:%s[%s]",
																	prePayResponseMesseage.getReturnMsg(),
																	prePayResponseMesseage.getErrCode()));
												} else {
													String sign = String.format("appId=%s&nonceStr=%s&package=prepay_id=%s&signType=MD5&timeStamp=%s&key=%s"
															, configuration.getAppID()
															, nonce
															, prePayResponseMesseage.getPrepayId()
															, timeStamp
															, configuration.getMchKey());
													String prePayId = prePayResponseMesseage.getPrepayId();
													resultBodyJson
															.put("sign", DigestUtils.md5Hex(sign))
															.put("prePayId", prePayId);
												}
												routingContext.response()
														.end(resultBodyJson.toBuffer());
											} catch (Throwable t) {
												routingContext.fail(httpResponseAsyncResult.cause());
											}
										} else {
											routingContext.fail(httpResponseAsyncResult.cause());
										}
									});
						} catch (Throwable t) {
							routingContext.fail(t);
						}
						break;
					default:

						break;
				}

			}).failureHandler(routingContext -> {
				routingContext.response().end(routingContext.failure().toString());
			});

			//支付信息微信回调
			router.post(configuration.getPath().getOther().get("mallPay")).handler(routingContext -> {
				routingContext.response().setChunked(true);
				logger.info(String.format("%s,%s", "微信回调", routingContext.getBodyAsString()));
				try {
					PayBillRequsetMesseage payBillRequsetMesseage = JAXBUtils.fromXML(routingContext.getBodyAsString(), PayBillRequsetMesseage.class);
					MallOrderDto orderVO = new MallOrderDto()
							.setCode(payBillRequsetMesseage.getOutTradeNo())
							.setUserId(payBillRequsetMesseage.getOpenId());
					orderVO = (MallOrderDto) orderService
							.handle(CustomConst.GET,
									JsonObject.mapFrom(orderVO).getMap());
					PayBillResponseMesseage payBillResponseMesseage = new PayBillResponseMesseage();
					String code;
					String msg;
					if (orderVO.getId() != null) {
						code = "SUCCESS";
						msg = "OK";
						if (orderVO.getState() == CustomConst.ORDER.STATE.RUNNING) {
							orderService.handle(CustomConst.MODIFY, new MallOrderDto()
									.setId(orderVO.getId())
									.setState(CustomConst.ORDER.STATE.END)
									.setUserId(orderVO.getUserId()));
						}
					} else {
						code = "FAIL";
						msg = "定单不存在";
					}
					payBillResponseMesseage.setReturnCode(code);
					payBillResponseMesseage.setReturnMsg(msg);
					routingContext.response()
							.end(Buffer.buffer(JAXBUtils.toXML(payBillResponseMesseage)));
				} catch (JAXBException e) {
					logger.error(e.getMessage(), e);
					routingContext.fail(e);
				}

			});
		};
		return routerConsumer;
	}
}
