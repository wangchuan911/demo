package com.hubidaauto.carservice.wxapp.core.service;

import com.hubidaauto.carservice.wxapp.core.config.CustomConst;
import com.hubidaauto.carservice.wxapp.core.dao.TacheDao;
import com.hubidaauto.carservice.wxapp.core.entity.TacheVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.welisdoon.web.common.web.AbstractBaseService;
import org.welisdoon.web.vertx.annotation.VertxWebApi;

import java.util.List;
import java.util.Map;

@Service
@Transactional(rollbackFor = Throwable.class)
public class TacheService extends AbstractBaseService<TacheVO> {

    @Autowired
    TacheDao tacheDao;

    @Override
    @VertxWebApi
    public Object handle(int exeCode, Map params) {
        Object resultObj = null;
        TacheVO tacheVO;
        switch (exeCode) {
            case CustomConst.GET:
                tacheVO = mapToObject(params, TacheVO.class);
                resultObj = tacheDao.list(tacheVO);
                break;
            case CustomConst.LIST:
                tacheVO = mapToObject(params, TacheVO.class);
                List list = tacheDao.listAll(tacheVO);
                resultObj = list;
                break;
            case CustomConst.TACHE.GET_WORK_NUMBER:
                resultObj = tacheDao.getProccess(params);
                break;
            default:
                break;
        }
        return resultObj;
    }
}
