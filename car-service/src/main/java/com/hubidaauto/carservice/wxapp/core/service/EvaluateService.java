package com.hubidaauto.carservice.wxapp.core.service;

import com.hubidaauto.carservice.wxapp.core.config.CustomConst;
import com.hubidaauto.carservice.wxapp.core.dao.EvaluateDao;
import com.hubidaauto.carservice.wxapp.core.entity.EvaluateVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.welisdoon.web.common.web.AbstractBaseService;
import org.welisdoon.web.vertx.annotation.VertxWebApi;

import java.util.Map;

@Service
@Transactional(rollbackFor = Throwable.class)
public class EvaluateService extends AbstractBaseService<EvaluateVO> {

    @Autowired
    EvaluateDao evaluateDao;

    @Override
    @VertxWebApi
    public Object handle(int exeCode, Map params) {
        Object resultObj = null;
        EvaluateVO evaluateVO;
        switch (exeCode) {
            case CustomConst.GET:
                evaluateVO = mapToObject(params, EvaluateVO.class);
                resultObj = evaluateDao.get(evaluateVO);
                break;
            case CustomConst.MODIFY:
                evaluateVO = mapToObject(params, EvaluateVO.class);
                int updateInt = evaluateDao.set(evaluateVO);
                if (updateInt != 0) {
                    resultObj = updateInt;
                    break;
                }
            case CustomConst.ADD:
                evaluateVO = mapToObject(params, EvaluateVO.class);
                resultObj = evaluateDao.add(evaluateVO);
                break;
            default:
                break;
        }
        return resultObj;
    }
}
