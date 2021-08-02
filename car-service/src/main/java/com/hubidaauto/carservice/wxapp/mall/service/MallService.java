package com.hubidaauto.carservice.wxapp.mall.service;


import com.hubidaauto.carservice.wxapp.core.config.CustomConst;
import com.hubidaauto.carservice.wxapp.mall.dao.MallDao;
import com.hubidaauto.carservice.wxapp.mall.entity.MallDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.welisdoon.webserver.common.web.AbstractBaseService;
import org.welisdoon.webserver.vertx.annotation.VertxWebApi;

import java.util.Map;

@Service
@Transactional(rollbackFor = Throwable.class)
public class MallService extends AbstractBaseService<MallDto> {

	@Autowired
	MallDao mallDao;

	@Override
	@VertxWebApi
	public Object handle(int exeCode, Map map) {
		Object resultObj = null;
		switch (exeCode) {
			case CustomConst.LIST: {
				MallDto mallDto = mapToObject(map, MallDto.class).setVisable(true);
				resultObj = mallDao.list(mallDto);
			}
			break;
			case CustomConst.GET: {
				MallDto mallDto = mapToObject(map, MallDto.class).setVisable(true);
				resultObj = mallDao.get(mallDto);
			}
			break;
		}
		return resultObj;
	}
}
