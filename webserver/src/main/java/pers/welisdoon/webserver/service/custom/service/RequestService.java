package pers.welisdoon.webserver.service.custom.service;

import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.mybatis.spring.SqlSessionTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import io.vertx.core.shareddata.Lock;
import io.vertx.core.shareddata.SharedData;
import pers.welisdoon.webserver.service.custom.config.CustomConst;
import pers.welisdoon.webserver.service.custom.dao.*;
import pers.welisdoon.webserver.service.custom.entity.*;
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
public class RequestService {

    @Autowired
    OrderDao orderDao;
    @Autowired
    TacheDao tacheDao;
    @Autowired
    UserDao userDao;
    @Autowired
    CarDao carDao;
    @Autowired
    OperationDao operationDao;
    @Autowired
    SqlSessionTemplate sqlSessionTemplate;
    private static final Logger logger = LoggerFactory.getLogger(RequestService.class);

    private static List<TacheVO> TACHE_VO_LIST;
    private static Map<Integer, Integer> ROLE_MAP;

    @PostConstruct
    void init() {
        TACHE_VO_LIST = tacheDao.listAll(new TacheVO().setTampalateId(1));
        ROLE_MAP = userDao.listRoles()
                .stream()
                .collect(Collectors
                        .toMap(UserVO.RoleConfig::getRoleId
                                , UserVO.RoleConfig::getLevel));
    }

    /*定時任務*/
    @VertxRegister(StandaredVerticle.class)
    public Consumer<Vertx> createOrdderTimer() {
        final OrderVO nextOrderVO = new OrderVO().setOrderState(1);
        final String KEY = this.getClass().getName().toUpperCase();
        final String URL_TOCKEN_LOCK = KEY + ".LOCK";
        Consumer<Vertx> vertxConsumer = vertx1 -> {
            SharedData sharedData = vertx1.sharedData();
            Handler<Long> longHandler = aLong -> {
                /*集群锁，防止重复处理和锁表*/
                sharedData.getLock(URL_TOCKEN_LOCK, lockAsyncResult -> {
                    if (lockAsyncResult.succeeded()) {
                        try {
                            List<OrderVO> orderVOS = orderDao.list(nextOrderVO).stream().map(orderVO -> {
                                OrderVO newOrderVO = new OrderVO()
                                        .setOrderId(orderVO.getOrderId());
                                Iterator<TacheVO> iterator = TACHE_VO_LIST.iterator();
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
            /*5s*/
            vertx1.setPeriodic(5 * 1000, longHandler);
        };
        return vertxConsumer;
    }

    /*工单管理*/
    public Object orderManger(int mode, Map params) {
        Object resultObj = null;
        OrderVO orderVO;
        switch (mode) {
            case CustomConst.ADD:
                orderVO = mapToObject(params, OrderVO.class);
                orderVO.setTacheId(TACHE_VO_LIST.get(0).getTacheId());
                orderVO.setOrderState(1);
                orderVO.setOrderCode("");
                orderDao.add(orderVO);
                resultObj = orderVO;
                break;
            case CustomConst.DELETE:
                break;
            case CustomConst.MODIFY:
                orderVO = mapToObject(params, OrderVO.class);
                resultObj = orderDao.set(orderVO);
                break;
            case CustomConst.GET:
                orderVO = mapToObject(params, OrderVO.class);
                List list = orderDao.list(orderVO);
                resultObj = list;
                break;
            case CustomConst.ORDER.GET_WORK_NUMBER:
                orderVO = mapToObject(params, OrderVO.class);
                resultObj = orderDao.getWorkIngOrderNum(orderVO);
                break;
            default:
                break;

        }
        return resultObj;
    }

    /*车辆管理*/
    public Object carManger(int mode, Map params) {
        Object resultObj = null;
        CarVO carVO = null;
        switch (mode) {
            case CustomConst.LIST:
                carVO = mapToObject(params, CarVO.class);
                resultObj = carDao.list(carVO);
                break;
            case CustomConst.DELETE:
                carVO = mapToObject(params, CarVO.class);
                resultObj = carDao.del(carVO);
                break;
            case CustomConst.ADD:
                carVO = mapToObject(params, CarVO.class);
                carDao.add(carVO);
                resultObj = carVO;
                break;
            case CustomConst.MODIFY:
                carVO = mapToObject(params, CarVO.class);
                resultObj = carDao.set(carVO);
                break;
            default:
                break;
        }
        return resultObj;
    }

    /*用户管理*/
    public Object userManger(int mode, Map params) {
        Object resultObj = null;
        UserVO userVO = null;
        switch (mode) {
            case CustomConst.GET:
                userVO = mapToObject(params, UserVO.class);
                resultObj = userDao.get(userVO);
                break;
            default:
                break;
        }
        return resultObj;
    }

    /*环节管理*/
    public Object tacheManager(int mode, Map params) {
        Object resultObj = null;
        TacheVO tacheVO;
        switch (mode) {
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

    /*环节 操作 管理*/
    public Object operationManager(int mode, Map params) {
        Object resultObj = null;
        OperationVO operationVO;
        switch (mode) {
            case CustomConst.GET:
                operationVO = mapToObject(params, OperationVO.class);
                resultObj = operationDao.list(operationVO);
                break;
            case CustomConst.LIST:
                operationVO = mapToObject(params, OperationVO.class);
                List list = operationDao.listAll(operationVO);
                resultObj = list;
                break;
            default:
                break;
        }
        return resultObj;
    }

    /*环节 操作 内部 管理*/
    Object operationManager(int mode, OperationVO operationVO) {
        Object resultObj = null;
        switch (mode) {
            case CustomConst.ADD:
                operationDao.set(new OperationVO()
                        .setActive(true)
                        .setFinishTime(new Timestamp(System.currentTimeMillis()))
                        .setOrderId(operationVO.getOrderId())
                        .setOprMan(operationVO.getOprMan()));
                resultObj = operationDao.add(operationVO);
                break;
            case CustomConst.GET:
                List list = operationDao.list(operationVO);
                resultObj = list;
                break;
            default:
                break;
        }
        return resultObj;
    }

    /*登陆初始化*/
    public Object login(String userId) {
        JsonObject jsonObject = new JsonObject();
        if (!StringUtils.isEmpty(userId)) {
            Object o = userManger(CustomConst.GET, Map.of("id", userId));
            UserVO userVO;
            if (o == null) {
                userVO = new UserVO().setId(userId);
                userVO.setRole(0);
                userVO.setName("新用戶");
                jsonObject.put("user", JsonObject.mapFrom(userVO));
            }
            else {
                userVO = (UserVO) o;
                jsonObject.put("user", JsonObject.mapFrom(o));
                switch (userVO.getRole()) {
                    case CustomConst.ROLE.CUSTOMER:
                        o = tacheManager(CustomConst.TACHE.GET_WORK_NUMBER, Map.of("userId", userVO.getId()));
                        break;
                    case CustomConst.ROLE.WOCKER:
                        o = orderManger(CustomConst.ORDER.GET_WORK_NUMBER, Map.of("orderAppointPerson", userVO.getId()));
                        break;
                    case CustomConst.ROLE.DISTRIBUTOR:
                        o = orderManger(CustomConst.ORDER.GET_WORK_NUMBER, Map.of("orderControlPerson", userVO.getId()));
                        break;

                }
                o = o != null ? JsonObject.mapFrom(o) : Map.of("all_nums", 0, "nums", 0);
                jsonObject.put("work", o);
                jsonObject.put("cars", carDao.list(new CarVO().setUserId(userId).setDefaultSelected(1)));
            }
        }
        return jsonObject;
    }

    /*下一步*/
    public Object toBeContinue(Map map) {
        Object returnObj = 0;
        String orderId = MapUtils.getString(map, "orderId", null);
        Integer tacheId = MapUtils.getInteger(map, "tacheId", null);
        String userId = MapUtils.getString(map, "userId", null);
        if (StringUtils.isEmpty(orderId)
                || tacheId == null
                || StringUtils.isEmpty(userId)) {
            return returnObj;
        }

        UserVO userInfo = userDao.get(new UserVO().setId(userId));
        OrderVO orderVO = orderDao.get(new OrderVO().setOrderId(orderId));
        TacheVO queryTacheVo = new TacheVO().setTacheId(tacheId);

        for (TacheVO iteratorTempVo : TACHE_VO_LIST) {
            if (iteratorTempVo.getTacheId() == orderVO.getTacheId()) {
                if (tacheId == iteratorTempVo.getTacheId()) {
                    queryTacheVo = iteratorTempVo;
                    break;
                }
                else if (!CollectionUtils.isEmpty(iteratorTempVo.getTacheRelas())) {
                    queryTacheVo = findTache(queryTacheVo, iteratorTempVo, userInfo);
                    if (queryTacheVo != null) break;
                }
                else {
                    queryTacheVo = null;
                }
            }
        }
        if (queryTacheVo == null) return null;
        /*流程有权限限制*/
        TacheVO.TacheRela a = new TacheVO.TacheRela();
        {
            returnObj = orderDao.set(new OrderVO().setOrderId(orderVO.getOrderId()).setOrderState(1));
        }
        return returnObj;
    }

    private static TacheVO findTache(TacheVO queryTache, List<TacheVO> fullTaches, UserVO userinfo) {
        if (queryTache == null
                || queryTache.getTacheId() == null
                || queryTache.getTacheId() < 0) {
            return null;
        }

        Iterator<TacheVO> tacheVOIterator = fullTaches.iterator();
        TacheVO targetTacheVo = null;
        TacheVO iteratorTempVo = null;
        while (tacheVOIterator.hasNext()) {
            iteratorTempVo = tacheVOIterator.next();
            if (queryTache.getTacheId() == iteratorTempVo.getTacheId()) {
                targetTacheVo = iteratorTempVo;
                break;
            }
            else if (!CollectionUtils.isEmpty(iteratorTempVo.getTacheRelas())) {
                /*for (TacheVO.TacheRela tmepRela : tmepVo.getTacheRelas()) {
                    if (ROLE_MAP.get(tmepRela.getRole()) >= userinfo.getLevel()) {
                        tmepVo = findTache(queryTache, tmepRela.getChildTaches(), userinfo);
                        if (tmepVo != null) {
                            targetTacheVo = tmepVo;
                            break;
                        }
                    }
                }*/
                targetTacheVo = findTache(queryTache, iteratorTempVo, userinfo);
                if (targetTacheVo != null) break;
            }
        }
        return targetTacheVo;
    }

    private static TacheVO findTache(TacheVO queryTache, TacheVO iteratorTempVo, UserVO userinfo) {
        TacheVO targetTacheVo = null;
        for (TacheVO.TacheRela tmepRela : iteratorTempVo.getTacheRelas()) {
            if (ROLE_MAP.get(tmepRela.getRole()) >= userinfo.getLevel()) {
                iteratorTempVo = findTache(queryTache, tmepRela.getChildTaches(), userinfo);
                if (iteratorTempVo != null) {
                    targetTacheVo = iteratorTempVo;
                    break;
                }
            }
        }
        return targetTacheVo;
    }

    private static <T> T mapToObject(Map params, Class<T> type) {
        return JsonObject.mapFrom(params).mapTo(type);
    }
}

