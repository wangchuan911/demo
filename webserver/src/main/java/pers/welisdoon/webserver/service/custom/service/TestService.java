package pers.welisdoon.webserver.service.custom.service;

import io.vertx.core.Vertx;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.FileUpload;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.Session;

import org.mybatis.spring.SqlSessionTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import pers.welisdoon.webserver.common.web.CommonAsynService;
import pers.welisdoon.webserver.service.custom.config.TestConfiguration;
import pers.welisdoon.webserver.service.custom.dao.OrderDao;
import pers.welisdoon.webserver.service.custom.entity.OrderVO;
import pers.welisdoon.webserver.vertx.annotation.VertxConfiguration;
import pers.welisdoon.webserver.vertx.annotation.VertxRegister;
import pers.welisdoon.webserver.vertx.verticle.StandaredVerticle;

import java.io.File;
import java.io.FileInputStream;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

@Service
@VertxConfiguration
public class TestService {

    public final int ADD = 0;
    public final int DELETE = 1;
    public final int MODIFY = 2;
    public final int GET = 3;
    /*orderManger*/
    public final int GET_WORK_NUMBER = 4;

    @Autowired
    OrderDao customDao;
    @Autowired
    SqlSessionTemplate sqlSessionTemplate;
    private static final Logger logger = LoggerFactory.getLogger(TestService.class);


    public Object orderManger(int mode, Map params) {
        Object resultObj = null;
        OrderVO orderVO;
        switch (mode) {
            case ADD:
                orderVO = mapToOrderVO(params);
                customDao.add(orderVO);
                resultObj = orderVO;
                break;
            case DELETE:
                break;
            case MODIFY:
                orderVO = mapToOrderVO(params);
                resultObj = customDao.set(orderVO);
                break;
            case GET:
                orderVO = mapToOrderVO(params);
                List list = customDao.list(orderVO);
                resultObj = list;
                break;
            case GET_WORK_NUMBER:
                orderVO = mapToOrderVO(params);
                resultObj = customDao.getWorkIngOrderNum(orderVO);
                break;
            default:
                break;

        }
        return resultObj;
    }

    public Object carManger(int mode, Map params) {
        OrderVO orderVO = mapToOrderVO(params);
        List list = customDao.list(orderVO);
        return Map.of("input", params, "output", list);
    }

    public Object userManger(int mode, Map params) {
        OrderVO orderVO = mapToOrderVO(params);
        List list = customDao.list(orderVO);
        return Map.of("input", params, "output", list);
    }

    private static OrderVO mapToOrderVO(Map params) {
        return JsonObject.mapFrom(params).mapTo(OrderVO.class);
    }
}
