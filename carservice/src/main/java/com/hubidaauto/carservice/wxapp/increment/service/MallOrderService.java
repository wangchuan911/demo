package com.hubidaauto.carservice.wxapp.increment.service;


import com.hubidaauto.carservice.wxapp.core.config.CustomConst;
import com.hubidaauto.carservice.wxapp.increment.dao.MallOrderDao;
import com.hubidaauto.carservice.wxapp.increment.entity.MallOrderDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.welisdoon.webserver.common.web.AbstractBaseService;
import org.welisdoon.webserver.vertx.annotation.VertxWebApi;

import java.util.Map;

@Service
@Transactional(rollbackFor = Throwable.class)
public class MallOrderService extends AbstractBaseService<MallOrderDto> {

	@Autowired
	MallOrderDao mallOrderDao;

	@Override
	public Object handle(int exeCode, Map map) {
		return null;
	}

	@Override
	public Object handle(int exeCode, MallOrderDto mallOrderDto) {
		Object resultObj = null;
		switch (exeCode) {
			case CustomConst.ADD: {
				mallOrderDao.add(mallOrderDto);
				resultObj = mallOrderDto;
			}
			break;
			case CustomConst.MODIFY: {
				resultObj = mallOrderDao.set(mallOrderDto);
			}
			break;
			case CustomConst.GET: {
				resultObj = mallOrderDao.get(mallOrderDto);
			}
			break;
		}
		return resultObj;
	}
}
