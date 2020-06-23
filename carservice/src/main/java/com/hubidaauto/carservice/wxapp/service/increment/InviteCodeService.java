package com.hubidaauto.carservice.wxapp.service.increment;


import com.hubidaauto.carservice.officalaccount.dao.OfficalAccoutUserDao;
import com.hubidaauto.carservice.officalaccount.service.CustomWeChatOfficalAccountService;
import com.hubidaauto.carservice.wxapp.config.CustomConst;
import com.hubidaauto.carservice.wxapp.dao.InviteCodeDao;
import com.hubidaauto.carservice.wxapp.dao.UserDao;
import com.hubidaauto.carservice.wxapp.entity.increment.InviteCodeDto;
import com.hubidaauto.carservice.wxapp.entity.UserVO;
import org.apache.commons.collections4.MapUtils;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.welisdoon.webserver.common.ApplicationContextProvider;
import org.welisdoon.webserver.common.web.AbstractBaseService;
import org.welisdoon.webserver.vertx.annotation.VertxWebApi;

import java.util.List;
import java.util.Map;

@Service
public class InviteCodeService extends AbstractBaseService<InviteCodeDto> {
	private static final Logger logger = logger(InviteCodeService.class);


	@Autowired
	InviteCodeDao inviteCodeDao;
	@Autowired
	UserDao userDao;
	@Autowired
	OfficalAccoutUserDao accoutUserDao;

	CustomWeChatOfficalAccountService customWeChatOfficalAccountService;

	/*@PostConstruct
	void a() {
		;
		this.handle(CustomConst.DELETE, JsonObject.mapFrom(
				this.handle(CustomConst.ADD,
						Map.of("userId", "1", "type", "1")))
				.getMap());
	}*/

	@Override
	public void init() throws Throwable {
		super.init();
		customWeChatOfficalAccountService = ApplicationContextProvider.getBean(CustomWeChatOfficalAccountService.class);
	}

	@Override
	@VertxWebApi
	public Object handle(int exeCode, Map params) {
		Object resultObj = null;
		InviteCodeDto inviteCodeDto = null;
		switch (exeCode) {
			case CustomConst.ADD:
				inviteCodeDto = this.mapToObject(params, InviteCodeDto.class);
				List<InviteCodeDto> inviteCodeDtoList = inviteCodeDao.list(inviteCodeDto);
				if (CollectionUtils.isEmpty(inviteCodeDtoList)) {
					inviteCodeDao.add(inviteCodeDto);
					resultObj = inviteCodeDto;
				} else {
					resultObj = inviteCodeDtoList.get(0);
				}
				break;
			case CustomConst.DELETE:
				inviteCodeDto = this.mapToObject(params, InviteCodeDto.class);
				inviteCodeDao.del(inviteCodeDto);
				break;
			case CustomConst.GET:
				inviteCodeDto = this.mapToObject(params, InviteCodeDto.class);
				resultObj = inviteCodeDao.get(inviteCodeDto);
				break;
			case CustomConst.INVITE_CODE.USE_INVITE_CODE:
				inviteCodeDto = inviteCodeDao.get(new InviteCodeDto()
						.setCode(MapUtils.getInteger(params, "code")));
				if (inviteCodeDto == null) {
					throw new RuntimeException("邀请码无效，如有疑问请联系客服");
				}
				UserVO manager = userDao.get(new UserVO().setId(inviteCodeDto.getUserId())),
						worker = new UserVO().setId(MapUtils.getString(params, "userId"))
								.setUserAttr(new UserVO.UserAttr()
										.setRegionCode(manager.getUserAttr().getRegionCode())
										.setInviteCode(inviteCodeDto.getCode())
										.setVip(false))
								.setName(MapUtils.getString(params, "userName"));

				char type = inviteCodeDto.getType();
				switch (type) {
					case CustomConst.INVITE_CODE.WORKER:
						UserVO workerFormDb = userDao.get(worker);
						if (workerFormDb.getUserAttr() != null) {
							if (workerFormDb.getUserAttr().getInviteCode() != null) {
								throw new RuntimeException("用户已使用其他邀请码，如有疑问请联系客服");
							}
							worker.getUserAttr().setVip(workerFormDb.getUserAttr().isVip());
						}
						worker.setSessionKey(workerFormDb.openData(false).getSessionKey());
						userDao.setMaxRole(worker.setMaxRole(CustomConst.ROLE.WOCKER));
						String keyData = "phoneEncryptedData", iv = "phoneEncryptedIv";
						if (params.containsKey(keyData) && params.containsKey(iv)) {
							try {
								worker.phoneDecrypted(MapUtils.getString(params, keyData), MapUtils.getString(params, iv));
							} catch (Throwable e) {
								logger.error(e.getMessage(), e);
							}
						}
						keyData = "userEncryptedData";
						iv = "userEncryptedIv";
						if (StringUtils.isEmpty(worker.getUnionid()) && params.containsKey(keyData) && params.containsKey(iv)) {
							try {
								worker.userDecrypted(MapUtils.getString(params, keyData), MapUtils.getString(params, iv));
								com.hubidaauto.carservice.officalaccount.entity.UserVO userVO = customWeChatOfficalAccountService.getOrAddUser(MapUtils.getString(params, "pubAccUserId"));
								if (StringUtils.isEmpty(userVO.getUnionid())) {
									accoutUserDao.set(userVO.setUnionid(worker.getUnionid()));
								}
							} catch (Throwable e) {
								logger.error(e.getMessage(), e);
							}
						}
						if (!StringUtils.isEmpty(worker.getName())
								|| !StringUtils.isEmpty(worker.getPhone())
								|| !StringUtils.isEmpty(worker.getUnionid())) {
							userDao.set(worker);
						}
						break;
					case CustomConst.INVITE_CODE.VIP:
						worker.getUserAttr().setVip(true);
						break;
					default:
						return new RuntimeException("类型错误");
				}
				userDao.newUserAttr(worker);
				inviteCodeDao.del(inviteCodeDto);
				break;
			default:
				break;
		}
		return resultObj;
	}
}
