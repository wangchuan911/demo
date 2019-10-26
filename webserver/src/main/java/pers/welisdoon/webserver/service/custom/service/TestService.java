package pers.welisdoon.webserver.service.custom.service;

import io.vertx.core.json.JsonObject;

import org.mybatis.spring.SqlSessionTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import pers.welisdoon.webserver.service.custom.dao.OrderDao;
import pers.welisdoon.webserver.service.custom.dao.TacheDao;
import pers.welisdoon.webserver.service.custom.entity.OrderVO;
import pers.welisdoon.webserver.service.custom.entity.TacheVO;
import pers.welisdoon.webserver.vertx.annotation.VertxConfiguration;

import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;

@Service
@VertxConfiguration
public class TestService {

    public final int ADD = 0;
    public final int DELETE = 1;
    public final int MODIFY = 2;
    public final int GET = 3;
    public final int LIST = 4;
    /*orderManger*/
    /*tacheManager*/
    public final int GET_WORK_NUMBER = 10;

    @Autowired
    OrderDao orderDao;
    @Autowired
    TacheDao tacheDao;
    @Autowired
    SqlSessionTemplate sqlSessionTemplate;
    private static final Logger logger = LoggerFactory.getLogger(TestService.class);

    private TacheVO firstWashTacheVO;

    @PostConstruct
    void init() {
        firstWashTacheVO = tacheDao.get(new TacheVO().setSeq(1.0f).setTampalateId(1));
    }


    public Object orderManger(int mode, Map params) {
        Object resultObj = null;
        OrderVO orderVO;
        switch (mode) {
            case ADD:
                orderVO = mapToObject(params, OrderVO.class);
                orderVO.setTacheId(firstWashTacheVO.getTacheId());
                orderDao.add(orderVO);
                resultObj = orderVO;
                break;
            case DELETE:
                break;
            case MODIFY:
                orderVO = mapToObject(params, OrderVO.class);
                resultObj = orderDao.set(orderVO);
                break;
            case GET:
                orderVO = mapToObject(params, OrderVO.class);
                List list = orderDao.list(orderVO);
                resultObj = list;
                break;
            case GET_WORK_NUMBER:
                orderVO = mapToObject(params, OrderVO.class);
                resultObj = orderDao.getWorkIngOrderNum(orderVO);
                break;
            default:
                break;

        }
        return resultObj;
    }

    public Object carManger(int mode, Map params) {
        OrderVO orderVO = mapToObject(params, OrderVO.class);
        List list = orderDao.list(orderVO);
        return Map.of("input", params, "output", list);
    }

    public Object userManger(int mode, Map params) {
        OrderVO orderVO = mapToObject(params, OrderVO.class);
        List list = orderDao.list(orderVO);
        return Map.of("input", params, "output", list);
    }

    public Object tacheManager(int mode, Map params) {
        Object resultObj = null;
        TacheVO tacheVO;
        switch (mode) {
            case GET:
                tacheVO = mapToObject(params, TacheVO.class);
                resultObj = tacheDao.list(tacheVO);
                break;
            case LIST:
                tacheVO = mapToObject(params, TacheVO.class);
                List list = tacheDao.listAll(tacheVO);
                resultObj = list;
                break;
            case GET_WORK_NUMBER:
                tacheVO = mapToObject(params, TacheVO.class);
                resultObj = tacheDao.getProccess(tacheVO.getTacheId());
                break;
            default:
                break;
        }
        return resultObj;
    }

    private static <T> T mapToObject(Map params, Class<T> type) {
        return JsonObject.mapFrom(params).mapTo(type);
    }
}
