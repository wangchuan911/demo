package com.hubidaauto.carservice.wxapp.mall.config;

import com.hubidaauto.carservice.wxapp.core.config.CustomConst;
import com.hubidaauto.carservice.wxapp.core.config.CustomWeChatAppConfiguration;
import com.hubidaauto.carservice.wxapp.mall.common.AbstractAutoAssign;
import com.hubidaauto.carservice.wxapp.mall.entity.AssignDto;
import com.hubidaauto.carservice.wxapp.mall.entity.MallDto;
import com.hubidaauto.carservice.wxapp.mall.entity.MallOrderDto;
import com.hubidaauto.carservice.wxapp.mall.service.MallOrderService;
import com.hubidaauto.carservice.wxapp.mall.service.MallService;
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
import org.welisdoon.webserver.common.ApplicationContextProvider;
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
import java.util.UUID;
import java.util.function.Consumer;

@Configuration
@VertxConfiguration
@AutoConfigureAfter(WechatAppMallConfiguration.class)
@ConditionalOnProperty(prefix = "wechat-app-hubida", name = "appID")
public class WechatAppMallConfiguration {
	private static final Logger logger = LoggerFactory.getLogger(WechatAppMallConfiguration.class);

	AbstractWechatConfiguration configuration;
	WebClient webClient;
	MallOrderService mallOrderService;
	MallService mallService;

	@PostConstruct
	void init() {
		configuration = AbstractWechatConfiguration.getConfig(CustomWeChatAppConfiguration.class);
		mallService = ApplicationContextProvider.getBean(MallService.class);
		mallOrderService = ApplicationContextProvider.getBean(MallOrderService.class);
	}

	@VertxRegister(StandaredVerticle.class)
	public Consumer<Router> routeMapping(Vertx vertx) {
		final String URL_UNIFIEDORDERON = configuration.getUrls().get(CommonConst.WecharUrlKeys.UNIFIED_ORDER).toString();
		final String PAY_ADDRESS = configuration.getPath().getOther().get("mallPay"), PAY_CALLBACK = PAY_ADDRESS + "back";
		webClient = WebClient.create(vertx);
		Consumer<Router> routerConsumer = router -> {
			//get请求入口
			router.get(PAY_ADDRESS).handler(routingContext -> {
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
							Integer goodsId = Integer.parseInt(value.substring(offset, (offset = value.indexOf('.', offset))));
							Integer count = Integer.parseInt(value.substring(++offset, (offset = value.indexOf('.', offset))));
							String timeStamp = value.substring(++offset, (offset = value.indexOf('.', offset)));
							String custId = value.substring(++offset);

							MallDto mallDto = (MallDto) mallService.handle(CustomConst.GET, Map.of("id", goodsId));

							MallOrderDto orderVo, orderVo2 = new MallOrderDto()
									.setGoodsId(goodsId)
									.setUserId(custId)
									.setState(CustomConst.ORDER.STATE.RUNNING)
									.setCount(count);
							orderVo = (orderVo = (MallOrderDto) mallOrderService
									.handle(CustomConst.GET, orderVo2)) == null
									? (MallOrderDto) mallOrderService
									.handle(CustomConst.ADD, orderVo2
											.setCost(mallDto.getPrice() * count)
											.setCode(UUID.randomUUID().toString().replaceAll("-", ""))) : orderVo;

							Buffer buffer = Buffer.buffer(JAXBUtils.toXML(new PrePayRequsetMesseage()
									.setAppId(configuration.getAppID())
									.setMchId(configuration.getMchId())
									.setNonceStr(nonce)
									.setBody(String.format("%s-购买商品结算:\n定单编号：%s\n金额%s", configuration.getAppName(), orderVo.getCode(), orderVo.getCost()))
									.setOutTradeNo(orderVo.getCode())
									.setTotalFee(orderVo.getCost())
									.setSpbillCreateIp(configuration.getNetIp())
									.setNotifyUrl(configuration.getAddress() + PAY_CALLBACK)
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
				logger.error(routingContext.failure().getMessage(), routingContext.failure());
				routingContext.response().end(routingContext.failure().toString());
			});

			//支付信息微信回调
			router.post(PAY_CALLBACK).handler(routingContext -> {
				routingContext.response().setChunked(true);
				logger.info(String.format("%s,%s", "微信回调", routingContext.getBodyAsString()));
				try {
					PayBillRequsetMesseage payBillRequsetMesseage = JAXBUtils.fromXML(routingContext.getBodyAsString(), PayBillRequsetMesseage.class);
					MallOrderDto orderVO = new MallOrderDto()
							.setCode(payBillRequsetMesseage.getOutTradeNo())
							.setUserId(payBillRequsetMesseage.getOpenId());
					orderVO = (MallOrderDto) mallOrderService
							.handle(CustomConst.GET, orderVO);
					PayBillResponseMesseage payBillResponseMesseage = new PayBillResponseMesseage();
					String code;
					String msg;
					if (orderVO.getId() != null) {
						code = "SUCCESS";
						msg = "OK";
						if (orderVO.getState() == CustomConst.ORDER.STATE.RUNNING) {
							mallOrderService.handle(CustomConst.MODIFY, new MallOrderDto()
									.setId(orderVO.getId())
									.setState(CustomConst.ORDER.STATE.END)
									.setUserId(orderVO.getUserId()));
						}
						MallDto mallDto = (MallDto) mallService.handle(CustomConst.GET, Map.of("id", orderVO.getGoodsId()));
						AssignDto assignDto = mallDto.assignDto();
						(ApplicationContextProvider.getBean(AssignDto.getAutoAssignType(mallDto.getProtoType()))).buy(assignDto, orderVO);
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
