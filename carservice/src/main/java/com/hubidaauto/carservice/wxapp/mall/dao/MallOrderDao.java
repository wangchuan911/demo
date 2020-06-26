package com.hubidaauto.carservice.wxapp.mall.dao;

import com.hubidaauto.carservice.wxapp.mall.entity.MallOrderDto;
import org.springframework.stereotype.Repository;
import org.welisdoon.webserver.common.dao.IDao;

@Repository
public interface MallOrderDao extends IDao<MallOrderDto, Integer> {

}
