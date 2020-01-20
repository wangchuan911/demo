package org.welisdoon.webserver.service.custom.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.welisdoon.webserver.service.custom.config.CustomConst;
import org.welisdoon.webserver.service.custom.dao.EvaluateDao;
import org.welisdoon.webserver.service.custom.entity.EvaluateVO;
import org.welisdoon.webserver.service.custom.entity.TacheVO;

import java.util.List;
import java.util.Map;

@Service
public class EvaluateSerivce extends AbstractBaseService {

    @Autowired
    EvaluateDao evaluateDao;

    @Override
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
                resultObj = evaluateDao.set(evaluateVO);
                break;
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
