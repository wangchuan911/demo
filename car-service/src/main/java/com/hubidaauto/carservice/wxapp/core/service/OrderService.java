package com.hubidaauto.carservice.wxapp.core.service;

import com.hubidaauto.carservice.wxapp.core.config.CustomConst;
import com.hubidaauto.carservice.wxapp.core.config.CustomWeChatAppConfiguration;
import com.hubidaauto.carservice.wxapp.core.dao.*;
import com.hubidaauto.carservice.wxapp.increment.entity.CouponVO;
import com.hubidaauto.carservice.wxapp.core.entity.OrderVO;
import com.hubidaauto.carservice.wxapp.core.entity.PictureVO;
import com.hubidaauto.carservice.wxapp.core.entity.UserVO;
import com.hubidaauto.carservice.wxapp.increment.service.CouponService;
import com.hubidaauto.carservice.wxapp.increment.service.UserOperRecordService;
import io.vertx.core.json.JsonObject;
import org.apache.commons.collections4.MapUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.welisdoon.web.common.ApplicationContextProvider;
import org.welisdoon.web.common.config.AbstractWechatConfiguration;
import org.welisdoon.web.common.web.AbstractBaseService;
import org.welisdoon.web.entity.wechat.WeChatPayOrder;
import org.welisdoon.web.entity.wechat.WeChatRefundOrder;
import org.welisdoon.web.entity.wechat.payment.requset.PayBillRequsetMesseage;
import org.welisdoon.web.entity.wechat.payment.requset.PrePayRequsetMesseage;
import org.welisdoon.web.entity.wechat.payment.requset.RefundRequestMesseage;
import org.welisdoon.web.entity.wechat.payment.requset.RefundResultMesseage;
import org.welisdoon.web.entity.wechat.payment.response.PayBillResponseMesseage;
import org.welisdoon.web.entity.wechat.payment.response.RefundReplyMesseage;
import org.welisdoon.web.entity.wechat.payment.response.RefundResponseMesseage;
import org.welisdoon.web.service.wechat.intf.IWechatPayHandler;
import org.welisdoon.web.vertx.annotation.VertxWebApi;

import java.sql.Timestamp;
import java.util.List;
import java.util.Map;

@Service
@Transactional(rollbackFor = Throwable.class)
public class OrderService extends AbstractBaseService<OrderVO> implements IWechatPayHandler {

//	private static final Logger logger = logger(OrderService.class);

	@Autowired
	OrderDao orderDao;
	@Autowired
	TacheDao tacheDao;
	@Autowired
	UserDao userDao;
	@Autowired
	OperationDao operationDao;
	@Autowired
	PictureDao pictureDao;
    /*@Autowired
    UserOperRecordDao userOperRecordDao;*/

	UserOperRecordService userOperRecordService;


	TacheService tacheService;
	OperationService operationService;
	CouponService couponService;

	@Override
	public void init() throws Throwable {
		tacheService = ApplicationContextProvider.getBean(TacheService.class);
		operationService = ApplicationContextProvider.getBean(OperationService.class);
		couponService = ApplicationContextProvider.getBean(CouponService.class);
		userOperRecordService = ApplicationContextProvider.getBean(UserOperRecordService.class);
	}

	@Override
	@VertxWebApi
	public Object handle(int exeCode, Map params) {
		Object resultObj = null;
		OrderVO orderVO;
		switch (exeCode) {
			case CustomConst.ADD:
				Map encryptedMap = mapSpliter(params, Map.class, "phoneEncryptedData", "phoneEncryptedIv");
				Map coupon = mapSpliter(params, Map.class, "couponId");

				JsonObject orderVoJson = JsonObject.mapFrom(params);
				List<Integer> pictureIds = (List<Integer>) orderVoJson.remove("pictureIds");
				orderVO = orderVoJson.mapTo(OrderVO.class);
				orderVO.setTacheId(CustomConst.TACHE.FIRST_TACHE.getTacheId())
						.setOrderState(CustomConst.ORDER.STATE.WAIT_NEXT);
				UserVO userVO = userDao.get(new UserVO().setId(orderVO.getCustId()));
				//GET WX API ENCRYT TEXT TO PHONE
				try {
					String phoneEncryptedData = MapUtils.getString(encryptedMap, "phoneEncryptedData", null),
							phoneEncryptedIv = MapUtils.getString(encryptedMap, "phoneEncryptedIv", null);
					if (!(StringUtils.isEmpty(phoneEncryptedData) || StringUtils.isEmpty(phoneEncryptedIv))) {
						/*userVO.openData(false);
						JsonObject jsonObject = new JsonObject(WXBizMsgCrypt.wxDecrypt(phoneEncryptedData, userVO.getSessionKey(), phoneEncryptedIv));
						orderVO.setCustPhone(jsonObject.getString("phoneNumber", jsonObject.getString("purePhoneNumber", null)));*/
						orderVO.setCustPhone(userVO.phoneDecrypted(phoneEncryptedData, phoneEncryptedIv).getPhone());
					}
				} catch (Throwable e) {
					logger.error(e.getMessage(), e);
				}
				//UPDATE PHONE_NUM
				orderVO.setCustName(userVO.getName())
						.setCustPhone(StringUtils.isEmpty(orderVO.getCustPhone())
								? userVO.getPhone()
								: orderVO.getCustPhone());
				orderDao.add(orderVO);
				//save PIC
				if (!CollectionUtils.isEmpty(pictureIds)) {
					Integer orderId = orderVO.getOrderId();
					Integer tacheId = orderVO.getTacheId();
					pictureIds.stream().forEach(pictureId -> {
						pictureDao.set(new PictureVO()
								.setPictrueId(pictureId)
								.setOrderId(orderId)
								.setTacheId(tacheId));
					});
				}
				//SET COUPON
				Integer couponId = MapUtils.getInteger(coupon, "couponId", 0);
				if (couponId > 0) {
					couponService.handle(CustomConst.MODIFY, new CouponVO()
							.setUserId(orderVO.getCustId())
							.setOrderId(orderVO.getOrderId())
							.setId(couponId)
							.setUseDate(new Timestamp(System.currentTimeMillis())));
				}
				resultObj = tacheService.handle(CustomConst.TACHE.GET_WORK_NUMBER, Map.of("userId", orderVO.getCustId()));
				operationService.wechatMessagePush(orderVO, orderVO.getTacheId());
				break;
			case CustomConst.DELETE:
				orderVO = mapToObject(params, OrderVO.class);
				resultObj = this.handle(CustomConst.MODIFY, new OrderVO()
						.setOrderId(orderVO.getOrderId())
						.setCustId(orderVO.getCustId())
						.setFinishDate(new Timestamp(System.currentTimeMillis()))
						.setOrderState(CustomConst.ORDER.STATE.CANCEL));

				CouponVO couponVO = (CouponVO) couponService
						.handle(CustomConst.GET, new CouponVO()
								.setOrderId(orderVO.getOrderId())
								.setUserId(orderVO.getCustId()));
				if (couponVO != null) {
					couponService.handle(CustomConst.MODIFY, couponVO.setOrderId(null).setUseDate(null));
				}
				break;
            /*case CustomConst.MODIFY:
                orderVO = mapToObject(params, OrderVO.class);
                resultObj = orderDao.set(orderVO);
                break;*/
			case CustomConst.LIST:
				orderVO = mapToObject(params, OrderVO.class);
				List list = orderDao.listAll(orderVO);
				resultObj = list;
				break;
			case CustomConst.GET:
				orderVO = mapToObject(params, OrderVO.class);
				resultObj = orderDao.get(orderVO);
				break;
			case CustomConst.ORDER.GET_WORK_NUMBER:
				orderVO = mapToObject(params, OrderVO.class);
				List<Map> all = orderDao.getWorkIngOrderNum(orderVO);
				resultObj = Map.of("all_nums", all.size(),
						"nums", all.size() == 0 ? 0 : all.stream().map(map -> MapUtils.getInteger(map, "working", 0)).reduce((integer, integer2) -> integer + integer2).get(),
						"pos", all
				);
				break;
			case CustomConst.ORDER.APPIONT_WORKER:
				orderVO = new OrderVO()
//                        .setOrderAppointPerson(MapUtils.getString(params, "orderControlPerson"))
						.setOrderId(MapUtils.getInteger(params, "orderId"));
				orderVO = orderDao.get(orderVO);
				if (orderVO != null) {
					resultObj = orderDao.set(new OrderVO()
							.setOrderId(orderVO.getOrderId())
							.setOrderAppointPerson(MapUtils.getString(params, "orderAppointPerson")));
				}
			default:
				break;

		}
		return resultObj;
	}

	@Override
	public Object handle(int exeCode, OrderVO orderVO) {
		Object resultObj = null;
		switch (exeCode) {
			case CustomConst.MODIFY:
				orderDao.set(orderVO);
				//update user operation
//                userOperRecordDao.add(orderVO);
				userOperRecordService.handle(CustomConst.USER_RECORD.ORDER, orderVO);
				resultObj = orderVO;
				break;
		}
		return resultObj;
	}

	@Override
	public PayBillResponseMesseage payCallBack(PayBillRequsetMesseage payBillRequsetMesseage) {
		OrderVO orderVO = new OrderVO()
				.setOrderCode(payBillRequsetMesseage.getOutTradeNo())
				.setCustId(payBillRequsetMesseage.getOpenId());
		orderVO = (OrderVO) this
				.handle(CustomConst.GET,
						JsonObject.mapFrom(orderVO).getMap());
		PayBillResponseMesseage payBillResponseMesseage = new PayBillResponseMesseage();
		String code;
		String msg;
		if (orderVO.getOrderId() != null) {
			code = "SUCCESS";
			msg = "OK";
			if (orderVO.getOrderState() == CustomConst.ORDER.STATE.RUNNING) {
				this.handle(CustomConst.MODIFY, new OrderVO()
						.setOrderId(orderVO.getOrderId())
						.setOrderState(CustomConst.ORDER.STATE.WAIT_NEXT)
						.setCustId(orderVO.getCustId()));
			}
		} else {
			code = "FAIL";
			msg = "定单不存在";
		}
		payBillResponseMesseage.setReturnCode(code);
		payBillResponseMesseage.setReturnMsg(msg);
		return payBillResponseMesseage;
	}

	@Override
	public PrePayRequsetMesseage payRequset(WeChatPayOrder payOrder) {

		OrderVO orderVo = orderDao.get(new OrderVO(payOrder));
		CustomWeChatAppConfiguration customWeChatAppConfiguration = AbstractWechatConfiguration.getConfig(CustomWeChatAppConfiguration.class);
		PrePayRequsetMesseage messeage = new PrePayRequsetMesseage()
				.setBody(String.format("%s-服务费用结算:\n定单编号：%s\n金额%s", customWeChatAppConfiguration.getAppName(), orderVo.getOrderCode(), orderVo.getCost() / 100))
				.setOutTradeNo(orderVo.getOrderCode())
				.setTotalFee(orderVo.getCost())
				.setOpenid(orderVo.getCustId());
		return messeage;
	}

	@Override
	public RefundReplyMesseage refundCallBack(RefundResultMesseage refundResultMesseage) {
		return null;
	}

	@Override
	public RefundRequestMesseage refundRequset(WeChatRefundOrder weChatPayOrder) {
		return null;
	}
}
