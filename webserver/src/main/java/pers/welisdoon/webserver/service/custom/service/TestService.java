package pers.welisdoon.webserver.service.custom.service;

import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.json.JsonObject;

import org.mybatis.spring.SqlSessionTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import io.vertx.core.shareddata.Lock;
import io.vertx.core.shareddata.SharedData;
import io.vertx.ext.web.client.HttpResponse;
import io.vertx.ext.web.client.WebClient;
import pers.welisdoon.webserver.common.web.CommonAsynService;
import pers.welisdoon.webserver.service.custom.dao.OrderDao;
import pers.welisdoon.webserver.service.custom.dao.TacheDao;
import pers.welisdoon.webserver.service.custom.dao.UserDao;
import pers.welisdoon.webserver.service.custom.entity.OrderVO;
import pers.welisdoon.webserver.service.custom.entity.TacheVO;
import pers.welisdoon.webserver.service.custom.entity.UserVO;
import pers.welisdoon.webserver.vertx.annotation.VertxConfiguration;
import pers.welisdoon.webserver.vertx.annotation.VertxRegister;
import pers.welisdoon.webserver.vertx.verticle.StandaredVerticle;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;

@Service
@VertxConfiguration
public class TestService {
    /*all*/
    public final static int ADD = 0;
    public final static int DELETE = 1;
    public final static int MODIFY = 2;
    public final static int GET = 3;
    public final static int LIST = 4;
    /*orderManger*/
    /*tacheManager*/
    public final static int GET_WORK_NUMBER = 10;

    final String KEY = "TEST";
    final String URL_TOCKEN_LOCK = KEY + ".LOCK";
    @Autowired
    OrderDao orderDao;
    @Autowired
    TacheDao tacheDao;
    @Autowired
    UserDao userDao;
    @Autowired
    SqlSessionTemplate sqlSessionTemplate;
    private static final Logger logger = LoggerFactory.getLogger(TestService.class);

    private List<TacheVO> tacheVOList;
    private OrderVO nextOrderVO;

    @PostConstruct
    void init() {
        tacheVOList = tacheDao.listAll(new TacheVO().setTampalateId(1));
        nextOrderVO = new OrderVO().setOrderState(1);
    }

    @VertxRegister(StandaredVerticle.class)
    public Consumer<Vertx> createOrdderTimer() {
        Consumer<Vertx> vertxConsumer = vertx1 -> {
            SharedData sharedData = vertx1.sharedData();
            Handler<Long> longHandler = aLong -> {
                sharedData.getLock(URL_TOCKEN_LOCK, lockAsyncResult -> {
                    if (lockAsyncResult.succeeded()) {
                        try {
                            List<OrderVO> orderVOS = orderDao.list(nextOrderVO).stream().map(orderVO -> {
                                OrderVO newOrderVO = new OrderVO()
                                        .setOrderId(orderVO.getOrderId());
                                Iterator<TacheVO> iterator = tacheVOList.iterator();
                                TacheVO tacheVO = null;
                                while (iterator.hasNext()) {
                                    if ((tacheVO = iterator.next()).getTacheId() == orderVO.getTacheId()) {
                                        tacheVO = iterator.hasNext() ? iterator.next() : null;
                                        break;
                                    }
                                    else {
                                        tacheVO = null;
                                        continue;
                                    }
                                }
                                if (tacheVO != null) {
                                    newOrderVO.setTacheId(tacheVO.getTacheId());
                                    newOrderVO.setOrderState(0);
                                }
                                else {
                                    newOrderVO.setFinishDate(Timestamp.valueOf(LocalDateTime.now()));
                                    newOrderVO.setOrderState(2);
                                }
                                return newOrderVO;
                            }).collect(Collectors.toList());
                            if (orderVOS.size() > 0) {
                                orderDao.setAll(orderVOS);
                            }
                        }
                        finally {
                            Lock lock = lockAsyncResult.result();
                            vertx1.setTimer(3 * 1000, aLong1 -> {
                                lock.release();
                            });
                        }
                    }
                    else {
                        logger.info(lockAsyncResult.cause().getMessage());
                    }
                });
            };
            vertx1.setPeriodic(5 * 1000, longHandler);
        };
        return vertxConsumer;
    }


    public Object orderManger(int mode, Map params) {
        Object resultObj = null;
        OrderVO orderVO;
        switch (mode) {
            case ADD:
                orderVO = mapToObject(params, OrderVO.class);
                orderVO.setTacheId(tacheVOList.get(0).getTacheId());
                orderVO.setOrderState(1);
                orderVO.setOrderCode("");
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
        Object resultObj = null;
        UserVO userVO = null;
        switch (mode) {
            case GET:
                userVO = mapToObject(params, UserVO.class);
                resultObj = userDao.get(userVO);
                break;
            default:
                break;
        }
        return resultObj;
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
                resultObj = tacheDao.getProccess(params);
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

