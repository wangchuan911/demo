package pers.welisdoon.webserver.service.custom.service;

import io.vertx.core.json.JsonObject;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.mybatis.spring.SqlSessionTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import pers.welisdoon.webserver.service.custom.config.CustomConst;
import pers.welisdoon.webserver.service.custom.dao.*;
import pers.welisdoon.webserver.service.custom.entity.*;
import pers.welisdoon.webserver.vertx.annotation.VertxConfiguration;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

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

    /*private static List<TacheVO> TACHE_VO_LIST;
    private static Map<Integer, Integer> ROLE_MAP;*/

    @PostConstruct
    void init() {
        /*TACHE_VO_LIST = tacheDao.listAll(new TacheVO().setTampalateId(1));
        ROLE_MAP = userDao.listRoles()
                .stream()
                .collect(Collectors
                        .toMap(UserVO.RoleConfig::getRoleId
                                , UserVO.RoleConfig::getLevel));*/

    }

    /*扫单调用*/
    public void toBeContinue() {
        final OrderVO nextOrderVO = new OrderVO().setOrderState(CustomConst.ORDER.STATE.WAIT_NEXT);
        orderDao.list(nextOrderVO).stream().forEach(orderVO -> {
            /*OrderVO newOrderVO = new OrderVO()
                    .setOrderId(orderVO.getOrderId());
            TacheVO tacheVO = null;
            *//*Iterator<TacheVO> iterator = TACHE_VO_LIST.iterator();
            while (iterator.hasNext()) {
                if ((tacheVO = iterator.next()).getTacheId() == orderVO.getTacheId()) {
                    if (tacheVO.getNextTache() == null) {
                        tacheVO.setNextTache(iterator.hasNext() ?
                                iterator.next() : new TacheVO().setTacheId(CustomConst.TACHE.STATE.END));
                    }
                    tacheVO = tacheVO.getNextTache();
                    break;
                }
                else {
                    tacheVO = null;
                    continue;
                }
            }*//*
            tacheVO = CustomConst.TACHE.TACHE_MAP.get(orderVO.getTacheId()).getNextTache();
            if (tacheVO != null && tacheVO.getTacheId() >= 0) {
                newOrderVO.setTacheId(tacheVO.getTacheId());
                newOrderVO.setOrderState(CustomConst.ORDER.STATE.RUNNING);
                orderDao.set(newOrderVO);
                if (!CollectionUtils.isEmpty(tacheVO.getTacheRelas())) {
                    tacheVO.getTacheRelas().stream().forEach(tacheRela -> {
                        OperationVO operationVO = new OperationVO()
                                .setOrderId(orderVO.getOrderId())
                                .setTacheId(tacheRela.getChildTaches().get(0).getTacheId());
                        switch (tacheRela.getRole()) {
                            case CustomConst.ROLE.CUSTOMER:
                                operationVO.setOprMan(orderVO.getCustId());
                                break;
                            case CustomConst.ROLE.DISTRIBUTOR:
                                operationVO.setOprMan(orderVO.getOrderAppointPerson());
                                break;
                            case CustomConst.ROLE.WOCKER:
                                operationVO.setOprMan(orderVO.getOrderControlPerson());
                                break;
                            default:
                                return;
                        }
                        operationManager(CustomConst.ADD, operationVO);
                    });
                }
            } else {
                newOrderVO.setFinishDate(Timestamp.valueOf(LocalDateTime.now()));
                newOrderVO.setOrderState(CustomConst.ORDER.STATE.END);
                orderDao.set(newOrderVO);
            }*/
            this.toBeContinue(orderVO, null);
        });
    }

    void toBeContinue(OrderVO orderVO, OperationVO operation) {
        OrderVO newOrderVO = new OrderVO()
                .setOrderId(orderVO.getOrderId());
        TacheVO tacheVO = CustomConst.TACHE.TACHE_MAP.get(orderVO.getTacheId()).getNextTache();
        if (tacheVO != null && tacheVO.getTacheId() >= 0) {
            newOrderVO.setTacheId(tacheVO.getTacheId());
            newOrderVO.setOrderState(CustomConst.ORDER.STATE.RUNNING);
            orderDao.set(newOrderVO);
            if (!CollectionUtils.isEmpty(tacheVO.getTacheRelas())) {
                for (TacheVO.TacheRela tacheRela :
                        tacheVO.getTacheRelas()) {
                    OperationVO operationVO = new OperationVO()
                            .setOrderId(orderVO.getOrderId())
                            .setTacheId(tacheRela.getChildTaches().get(0).getTacheId());
                    switch (tacheRela.getRole()) {
                        case CustomConst.ROLE.CUSTOMER:
                            operationVO.setOprMan(orderVO.getCustId());
                            break;
                        case CustomConst.ROLE.DISTRIBUTOR:
                            operationVO.setOprMan(orderVO.getOrderAppointPerson());
                            break;
                        case CustomConst.ROLE.WOCKER:
                            operationVO.setOprMan(orderVO.getOrderControlPerson());
                            break;
                        default:
                            continue;
                    }
                    operationManager(CustomConst.ADD, operationVO);
                }
            } else if (operation != null) {
                OperationVO operationVO = new OperationVO()
                        .setTacheId(tacheVO.getTacheId())
                        .setOrderId(orderVO.getOrderId())
                        .setOprMan(operation.getOprMan())
                        .setInfo(operation.getInfo());
                if (!StringUtils.isEmpty(operationVO.getOprMan()))
                    operationManager(CustomConst.ADD, operationVO);
            } else {
                operationDao.set(new OperationVO()
                        .setActive(false)
                        .setFinishTime(new Timestamp(System.currentTimeMillis()))
                        .setOrderId(orderVO.getOrderId()));
            }
        } else {
            newOrderVO.setFinishDate(Timestamp.valueOf(LocalDateTime.now()));
            newOrderVO.setOrderState(CustomConst.ORDER.STATE.END);
            orderDao.set(newOrderVO);
        }
    }

    /*工单管理*/
    public Object orderManger(int mode, Map params) {
        Object resultObj = null;
        OrderVO orderVO;
        switch (mode) {
            case CustomConst.ADD:
                orderVO = mapToObject(params, OrderVO.class);
                /* orderVO.setTacheId(TACHE_VO_LIST.get(0).getTacheId());*/
                orderVO.setTacheId(CustomConst.TACHE.FIRST_TACHE.getTacheId());
                orderVO.setOrderState(CustomConst.ORDER.STATE.WAIT_NEXT);
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
            case CustomConst.LIST:
                orderVO = mapToObject(params, OrderVO.class);
                List list = orderDao.list(orderVO);
                resultObj = list;
                break;
            case CustomConst.GET:
                orderVO = mapToObject(params, OrderVO.class);
                resultObj = orderDao.get(orderVO);
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
            case CustomConst.ADD:
                break;
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
                operationVO = mapToObject(params, OperationVO.class);
                resultObj = operationManager(mode, operationVO);
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
                        .setActive(false)
                        .setFinishTime(new Timestamp(System.currentTimeMillis()))
                        .setOprMan(operationVO.getOprMan())
                        .setOrderId(operationVO.getOrderId()));
                resultObj = operationDao.add(operationVO.setActive(true));
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
                userVO.setRole(CustomConst.ROLE.CUSTOMER);
                userVO.setName("新用戶");
                jsonObject.put("user", JsonObject.mapFrom(userVO));
            } else {
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
        Object returnObj = null;
        Integer orderId = MapUtils.getInteger(map, "orderId", null);
        Integer tacheId = MapUtils.getInteger(map, "tacheId", null);
        String userId = MapUtils.getString(map, "userId", null);
        String info = MapUtils.getString(map, "info", null);
        /*下个环节还是自己操作时，能直接让工单执行*/
        boolean doNext = MapUtils.getBoolean(map, "doNext", false);
        if (StringUtils.isEmpty(orderId)
                || tacheId == null
                || StringUtils.isEmpty(userId)) {
            return returnObj;
        }

        UserVO userInfo = userDao.get(new UserVO().setId(userId));
        OrderVO orderVO = orderDao.get(new OrderVO().setOrderId(orderId));
        /*TacheVO queryTacheVo = new TacheVO().setTacheId(tacheId);
        TacheVO orderTache = null;
        Iterator<TacheVO> iterator = TACHE_VO_LIST.iterator();
        while (iterator.hasNext()) {
            TacheVO iteratorTempVo = iterator.next();
            if (iteratorTempVo.getTacheId() == orderVO.getTacheId()) {
                orderTache = iteratorTempVo;
                if (tacheId == iteratorTempVo.getTacheId()) {
                    queryTacheVo = iteratorTempVo;
                    if (queryTacheVo.getNextTache() == null) {
                        queryTacheVo.setNextTache(iterator.hasNext() ?
                                iterator.next() : new TacheVO().setTacheId(CustomConst.TACHE.STATE.END));
                    }
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
        }*/
        TacheVO orderTache = CustomConst.TACHE.TACHE_MAP.get(orderVO.getTacheId());
        TacheVO queryTacheVo = CustomConst.TACHE.TACHE_MAP.get(tacheId);

        if (queryTacheVo == null) return null;

        /*流程有权限限制*/

        boolean nextOrder = false;
        boolean nextOperation = false;
        /*为主环节，该环节没有关联环节,则让他往下走*/
        if ((orderTache == queryTacheVo && orderTache.getTacheRelas().size() <= 0)) {
            nextOrder = true;
        }
        /*不为主环节，且环节走到尽头*/
        else if ((orderTache != queryTacheVo && queryTacheVo.getNextTache() != null && queryTacheVo.getNextTache().getTacheId() < 0)) {
            //没有下一环节
            nextOrder = true;
        }
        /*不管是不是主环节 有子环节都要处理*/
        else {
            nextOperation = true;
        }
        /*走工单*/
        if (nextOrder) {
            if (doNext) {
                /*直接触发过单*/
                returnObj = new OperationVO()
                        .setOrderId(orderVO.getOrderId())
                        .setTacheId(CustomConst.TACHE.STATE.END)
                        .setOprMan(userId)
                        .setInfo(info);
                this.toBeContinue(orderVO, (OperationVO) returnObj);
            } else {
                orderDao.set(new OrderVO().setOrderId(orderVO.getOrderId()).setOrderState(CustomConst.ORDER.STATE.WAIT_NEXT));
                returnObj = new OperationVO().setOrderId(orderVO.getOrderId()).setTacheId(CustomConst.TACHE.STATE.WAIT);
            }
        } else if (nextOperation) {
            /*走工单子流程*/
            OperationVO operationVO = operationDao.get(new OperationVO()
                    .setOrderId(orderVO.getOrderId())
                    .setOprMan(userInfo.getId()).setTacheId(tacheId).setActive(true));
            if (operationVO != null) {
                /*没有下一环节了*/
                if (CustomConst.TACHE.STATE.END == queryTacheVo.getNextTache().getTacheId()) {
                    /*存在两个以上operationVO 证明有其他人也在处理*/
                    if (operationDao.num(new OperationVO().setOrderId(orderVO.getOrderId()).setActive(true)) > 1) {
                        /*让当前人员等待*/
                        operationVO.setTacheId(CustomConst.TACHE.STATE.WAIT);
                    } else if (doNext) {
                        /*直接触发过单*/
                        this.toBeContinue(orderVO, operationVO
                                .setInfo(info));
                    } else
                        /*提交到扫单触发过单*/
                        orderDao.set(new OrderVO()
                                .setOrderId(orderVO.getOrderId())
                                .setOrderState(CustomConst.ORDER.STATE.WAIT_NEXT));
                } else {
                    operationManager(CustomConst.ADD, operationVO.setTacheId(queryTacheVo.getNextTache().getTacheId()));
                }
                returnObj = operationVO;
            }
        }
        return returnObj;
    }

    /*private static TacheVO findTache(TacheVO queryTache, List<TacheVO> fullTaches, UserVO userinfo) {
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
                if (targetTacheVo.getNextTache() == null) {
                    targetTacheVo.setNextTache(tacheVOIterator.hasNext() ?
                            tacheVOIterator.next() : new TacheVO().setTacheId(CustomConst.TACHE.STATE.END));
                }
                break;
            }
            else if (!CollectionUtils.isEmpty(iteratorTempVo.getTacheRelas())) {
                *//*for (TacheVO.TacheRela tmepRela : tmepVo.getTacheRelas()) {
                    if (ROLE_MAP.get(tmepRela.getRole()) >= userinfo.getLevel()) {
                        tmepVo = findTache(queryTache, tmepRela.getChildTaches(), userinfo);
                        if (tmepVo != null) {
                            targetTacheVo = tmepVo;
                            break;
                        }
                    }
                }*//*
                targetTacheVo = findTache(queryTache, iteratorTempVo, userinfo);
                if (targetTacheVo != null) break;
            }
        }
        return targetTacheVo;
    }

    private static TacheVO findTache(TacheVO queryTache, TacheVO iteratorTempVo, UserVO userinfo) {
        TacheVO targetTacheVo = null;
        for (TacheVO.TacheRela tmepRela : iteratorTempVo.getTacheRelas()) {
            if (CustomConst.ROLE.ROLE_MAP.get(tmepRela.getRole()) >= userinfo.getLevel()) {
                iteratorTempVo = findTache(queryTache, tmepRela.getChildTaches(), userinfo);
                if (iteratorTempVo != null) {
                    targetTacheVo = iteratorTempVo;
                    break;
                }
            }
        }
        return targetTacheVo;
    }*/

    private static <T> T mapToObject(Map params, Class<T> type) {
        return JsonObject.mapFrom(params).mapTo(type);
    }
}

