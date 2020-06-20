package com.hubidaauto.carservice.wxapp.service;


import com.hubidaauto.carservice.wxapp.config.CustomConst;
import com.hubidaauto.carservice.wxapp.dao.InviteCodeDao;
import com.hubidaauto.carservice.wxapp.entity.InviteCodeDto;
import io.vertx.core.json.JsonObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.welisdoon.webserver.common.web.AbstractBaseService;
import org.welisdoon.webserver.vertx.annotation.VertxWebApi;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.Map;

@Service
public class InviteCodeService extends AbstractBaseService<InviteCodeDto> {


	@Autowired
	InviteCodeDao inviteCodeDao;

	/*@PostConstruct
	void a() {
		;
		this.handle(CustomConst.DELETE, JsonObject.mapFrom(
				this.handle(CustomConst.ADD,
						Map.of("userId", "1", "type", "1")))
				.getMap());
	}*/

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
			default:
				break;
		}
		return resultObj;
	}
}
