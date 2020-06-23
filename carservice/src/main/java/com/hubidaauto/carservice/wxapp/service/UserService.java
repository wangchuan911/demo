package com.hubidaauto.carservice.wxapp.service;

import com.hubidaauto.carservice.wxapp.config.CustomConst;
import com.hubidaauto.carservice.wxapp.dao.CarDao;
import com.hubidaauto.carservice.wxapp.dao.UserDao;
import com.hubidaauto.carservice.wxapp.entity.CarVO;
import com.hubidaauto.carservice.wxapp.entity.OrderVO;
import com.hubidaauto.carservice.wxapp.entity.UserVO;
import com.hubidaauto.carservice.wxapp.service.increment.CouponService;
import io.vertx.core.json.JsonObject;
import org.apache.commons.collections4.MapUtils;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.welisdoon.webserver.common.ApplicationContextProvider;
import org.welisdoon.webserver.common.web.AbstractBaseService;
import org.welisdoon.webserver.vertx.annotation.VertxWebApi;

import java.util.List;
import java.util.Map;

@Service
public class UserService extends AbstractBaseService<UserVO> {
	private static final Logger logger = logger(UserService.class);
	@Autowired
	UserDao userDao;
	@Autowired
	CarDao carDao;

	TacheService tacheService;
	OrderService orderService;
	CouponService couponService;

	@Override
	public void init() {
		tacheService = ApplicationContextProvider.getBean(TacheService.class);
		orderService = ApplicationContextProvider.getBean(OrderService.class);
		couponService = ApplicationContextProvider.getBean(CouponService.class);
	}

	@Override
	@VertxWebApi
	@Transactional(rollbackFor = Throwable.class)
	public Object handle(int exeCode, Map params) {
		Object resultObj = null;
		UserVO userVO = null;
		switch (exeCode) {
			case CustomConst.GET:
				userVO = mapToObject(params, UserVO.class);
				resultObj = userDao.get(userVO);
				break;
			case CustomConst.LIST:
				userVO = mapToObject(params, UserVO.class);
				resultObj = userDao.list(userVO);
				break;
			case CustomConst.USER.GET_WORKERS:
                /*userVO = mapToObject(params, UserVO.class);
                List<UserVO> userVOS = userDao.list(new UserVO().setMaxRole(CustomConst.ROLE.WOCKER));
                if (!StringUtils.isEmpty(userVO.getId())) {
                    userVOS.add(0, userDao.get(new UserVO().setId(userVO.getId())));
                }*/
				OrderVO orderVO = mapToObject(params, OrderVO.class);
				List<UserVO> userVOS = userDao.getWorkers(orderVO);
				if (!StringUtils.isEmpty(orderVO.getOrderControlPerson())) {
					userVOS.add(0, userDao.get(new UserVO().setId(orderVO.getOrderControlPerson())));
				}
				resultObj = userVOS;
				break;
			case CustomConst.ADD:
				userVO = mapToObject(params, UserVO.class);
				userVO.setRole(CustomConst.ROLE.CUSTOMER);
				resultObj = userDao.add(userVO);
				break;
			case CustomConst.MODIFY:
				Map encryptedMap = mapSpliter(params, Map.class, "userEncryptedData", "useriv");
				userVO = mapToObject(params, UserVO.class);
				try {
					String userEncryptedData = MapUtils.getString(encryptedMap, "userEncryptedData", null),
							useriv = MapUtils.getString(encryptedMap, "useriv", null);
					if (!(StringUtils.isEmpty(userEncryptedData) || StringUtils.isEmpty(useriv))) {
						UserVO cacheInfo = userDao.get(userVO);
						if (cacheInfo != null) {
                            /*String json;
                            JsonObject jsonObject = new JsonObject(json = WXBizMsgCrypt.wxDecrypt(userEncryptedData, cacheInfo.openData(false).getSessionKey(), useriv));
                            logger.info(json);
                            userVO.setUnionid(jsonObject.getString("unionId", null)).openData(false);*/
							cacheInfo.userDecrypted(userEncryptedData, useriv).openData(false);
						}
					}
				} catch (Throwable e) {
					logger.error(e.getMessage(), e);
				}
				userDao.set(userVO);
				if (userVO.getRole() == CustomConst.ROLE.CUSTOMER) {
					userVO.setCoupons(couponService.newCoupon(CustomConst.COUPON.NEW_USER, Map.of("id", userVO.getId())));
				}
				resultObj = userVO.openData(true);
				break;
			case CustomConst.USER.AREA_RANGE:
				userVO = mapToObject(params, UserVO.class);
				resultObj = userDao.getWorkAreaRange(userVO);
				break;
			default:
				break;
		}
		return resultObj;
	}

	/*登陆初始化*/
	@VertxWebApi
	@Transactional(rollbackFor = Throwable.class)
	public Object login(String userId) {
		return login(new UserVO().setId(userId));
	}

	@Transactional(rollbackFor = Throwable.class)
	public Object login(UserVO wxUserInfo) {
		JsonObject jsonObject = new JsonObject();
		if (wxUserInfo != null && !StringUtils.isEmpty(wxUserInfo.getId())) {
			Object o = handle(CustomConst.GET, Map.of("id", wxUserInfo.getId()));
			if (o == null) {
				wxUserInfo.setRole(CustomConst.ROLE.GUEST);
				wxUserInfo.setName("新用戶");
				jsonObject.put("user", JsonObject.mapFrom(wxUserInfo));
				userDao.add(wxUserInfo.openData(false));
			} else {
				UserVO userVO = (UserVO) o;
				jsonObject.put("user", JsonObject.mapFrom(o));
				switch (userVO.getRole()) {
					case CustomConst.ROLE.CUSTOMER:
						o = tacheService.handle(CustomConst.TACHE.GET_WORK_NUMBER, Map.of("userId", userVO.getId()));
						break;
					case CustomConst.ROLE.DISTRIBUTOR:
						o = orderService.handle(CustomConst.ORDER.GET_WORK_NUMBER, Map.of("orderControlPerson", userVO.getId()));
						break;
					case CustomConst.ROLE.WOCKER:
						o = orderService.handle(CustomConst.ORDER.GET_WORK_NUMBER, Map.of("orderAppointPerson", userVO.getId()));
						break;

				}
				o = o != null ? JsonObject.mapFrom(o) : Map.of("all_nums", 0, "nums", 0);
				jsonObject.put("work", o);
				jsonObject.put("cars", carDao.list(new CarVO().setUserId(userVO.getId()).setDefaultSelected(1)));
				if (!StringUtils.isEmpty(wxUserInfo.openData(false).getSessionKey())) {
					userDao.set(wxUserInfo);
				}
			}
		}
		return jsonObject;
	}
}
