package com.hubidaauto.carservice.service.service;

import com.hubidaauto.carservice.service.config.CustomConst;
import com.hubidaauto.carservice.service.dao.*;
import com.hubidaauto.carservice.service.entity.*;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.welisdoon.webserver.common.web.AbstractBaseService;
import org.welisdoon.webserver.vertx.annotation.VertxWebApi;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

@Service
@Transactional(rollbackFor = Throwable.class)
public class OperationService extends AbstractBaseService<OperationVO> {
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
    PictureDao pictureDao;

    @Override
    @VertxWebApi
    public Object handle(int exeCode, Map params) {
        Object resultObj = null;
        OperationVO operationVO;
        switch (exeCode) {
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
                resultObj = operationManager(exeCode, operationVO);
                break;
        }
        return resultObj;
    }

    /*环节 操作 内部 管理*/
    public Object operationManager(int mode, OperationVO operationVO) {
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

    @VertxWebApi
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
        TacheVO orderTache = CustomConst.TACHE.TACHE_MAP.get(orderVO.getTacheId());
        TacheVO queryTacheVo = CustomConst.TACHE.TACHE_MAP.get(tacheId);
        OperationVO operationVO = operationDao.get(new OperationVO()
                .setOrderId(orderVO.getOrderId())
                .setOprMan(userInfo.getId()).setTacheId(tacheId).setActive(true));

        /*走工单子流程*/
        if (queryTacheVo == null) return null;

        if (!StringUtils.isEmpty(info)) {
            this.operationInfoDeal(map);
        }

        Integer nextTache = queryTacheVo.getNextTache();
        int num = operationDao.num(new OperationVO().setOrderId(orderVO.getOrderId()).setActive(true));
        int operationMode = -1;
        /*为主环节，该环节没有关联环节,则让他往下走*/
        if ((orderTache.getTacheId().equals(queryTacheVo.getTacheId()) && orderTache.getTacheRelas().size() <= 0)) {
            operationMode = doNext ? 1 : 0;
        } else if (!orderTache.getTacheId().equals(queryTacheVo.getTacheId())
                && (orderTache.getTacheId().equals(queryTacheVo.getSuperTache()))) {
            ///*不为主环节，且环节走到尽头*/
            ///*不为主环节，但一定是当前环节的子环节*/
            if (queryTacheVo.getNextTache() != null
                    && queryTacheVo.getNextTache() < 0
                    && num <= 1) {
                //没有下一环节了，且没其他人也完成了
                operationMode = doNext ? 1 : 0;
            } else if (operationVO != null) {
                //当前环节有操作记录
                if (CustomConst.TACHE.STATE.END == nextTache
                        || CustomConst.TACHE.STATE.END == (nextTache = CustomConst.TACHE.getOptionTache(Arrays.asList(orderVO.getPassTache().split(",")), CustomConst.TACHE.TACHE_MAP.get(nextTache)))) {
                    //下一环节没有了
                    //或者因为有跳过的环节下一环没有了
                    if (num > 1) {
                        //还有其他操作数
                        operationMode = 2;
                    } else if (doNext) {
                        //快速过单
                        operationMode = 3;
                    } else {
                        //直接增加操作
                        operationMode = 4;
                    }
                } else {
                    operationMode = 5;
                }
            }
        }
        /*不管是不是主环节 有子环节都要处理*/
        /*走工单*/
        switch (operationMode) {
            case 0:
                orderDao.set(new OrderVO().setOrderId(orderVO.getOrderId()).setOrderState(CustomConst.ORDER.STATE.WAIT_NEXT));
                returnObj = new OperationVO().setOrderId(orderVO.getOrderId()).setTacheId(CustomConst.TACHE.STATE.WAIT);
                break;
            case 1:
                returnObj = new OperationVO()
                        .setOrderId(orderVO.getOrderId())
                        .setTacheId(CustomConst.TACHE.STATE.END)
                        .setOprMan(userId);
                this.toBeContinue(orderVO);
                break;
            case 2:
                operationDao.set(new OperationVO()
                        .setActive(false)
                        .setFinishTime(new Timestamp(System.currentTimeMillis()))
                        .setOprMan(operationVO.getOprMan())
                        .setOrderId(operationVO.getOrderId())
                        .setTacheId(operationVO.getTacheId()));
                /*让当前人员等待*/
                operationVO.setTacheId(CustomConst.TACHE.STATE.WAIT);
                returnObj = operationVO;
                break;
            case 3:
                /*直接触发过单*/
                this.toBeContinue(orderVO);
                returnObj = operationVO;
                break;
            case 4:
                /*提交到扫单触发过单*/
                orderDao.set(new OrderVO()
                        .setOrderId(orderVO.getOrderId())
                        .setOrderState(CustomConst.ORDER.STATE.WAIT_NEXT));
                returnObj = operationVO;
                break;
            case 5:
                operationManager(CustomConst.ADD, operationVO.setTacheId(nextTache));
                returnObj = operationVO;
                break;

        }
        return returnObj;
    }

    private void operationInfoDeal(Map map) {
        Integer orderId = MapUtils.getInteger(map, "orderId", null);
        Integer tacheId = MapUtils.getInteger(map, "tacheId", null);
        String userId = MapUtils.getString(map, "userId", null);
        try {
            JsonObject infoJson = JsonObject.mapFrom(map.get("info"));
            if (infoJson.containsKey("pictureIds")) {
                JsonArray jsonArray = infoJson.getJsonArray("pictureIds");
                if (jsonArray != null && jsonArray.size() > 0) {
                    for (int i = 0; i < jsonArray.size(); i++) {
                        pictureDao.set(new PictureVO()
                                .setPictrueId(jsonArray.getInteger(i))
                                .setOrderId(orderId)
                                .setTacheId(tacheId));
                    }
                }
            }
            if (infoJson.containsKey("setWorker")) {
                String workerId = infoJson.getString("setWorker");
                OrderVO orderVO;
                UserVO userVO;
                synchronized (this) {
                    if (!StringUtils.isEmpty(workerId)
                            && (orderVO = orderDao.get(new OrderVO().setOrderId(orderId))) != null
                            && StringUtils.isEmpty(orderVO.getOrderAppointPerson())
                            && (userVO = userDao.get(new UserVO().setId(workerId))) != null) {
                        orderDao.set(new OrderVO()
                                .setOrderId(orderVO.getOrderId())
                                .setOrderAppointPerson(userVO.getId())
                                .setOrderAppointPersonName(userVO.getName())
                                .setOrderAppointPhone(userVO.getPhone())
                                .setOrderControlPerson(userId));
                    }
                }
            }

            OperationVO operationVO = operationDao.get(new OperationVO()
                    .setOrderId(orderId)
                    .setOprMan(userId).setTacheId(tacheId).setActive(true));
            if (operationVO != null) {
                operationDao.set(operationVO.setInfo(infoJson.toString()));
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    private boolean waitOtherOperation(List<OperationVO> operationVOList, OperationVO operationVO) {
        Stream<OperationVO> stream = operationVOList.stream();
        long all = operationVOList.size();
        long orther = stream.filter(operationVO1 -> !operationVO1.getOprMan().equals(operationVO.getOprMan())).count();
        return orther > 1 || (all - orther > 0);
    }

    void toBeContinue(OrderVO orderVO/*, OperationVO operation*/) {
        OrderVO newOrderVO = new OrderVO()
                .setOrderId(orderVO.getOrderId());
        Integer tacheId = CustomConst.TACHE.TACHE_MAP.get(orderVO.getTacheId()).getNextTache();
        /*//测试用 目前自己授权给自己
        {
            test(newOrderVO, orderVO, tacheId);
        }*/
        if (tacheId != null && tacheId >= 0) {
            TacheVO tacheVO = CustomConst.TACHE.TACHE_MAP.get(tacheId);
            newOrderVO.setTacheId(tacheVO.getTacheId());
            newOrderVO.setOrderState(CustomConst.ORDER.STATE.RUNNING);

            orderDao.set(newOrderVO);
            if (!CollectionUtils.isEmpty(tacheVO.getTacheRelas())) {
                {
                    operationDao.set(new OperationVO()
                            .setActive(false)
                            .setFinishTime(new Timestamp(System.currentTimeMillis()))
                            .setOrderId(orderVO.getOrderId()));
                }
                for (TacheVO.TacheRela tacheRela :
                        tacheVO.getTacheRelas()) {
                    Integer newTacheId = CustomConst.TACHE.getOptionTache(Arrays.asList(orderVO.getPassTache().split(",")), tacheRela.getChildTaches().get(0));
                    if (newTacheId < 0) {
                        continue;
                    }
                    OperationVO operationVO = new OperationVO()
                            .setOrderId(orderVO.getOrderId())
                            .setTacheId(newTacheId);
                    switch (tacheRela.getRole()) {
                        case CustomConst.ROLE.CUSTOMER:
                            operationVO.setOprMan(orderVO.getCustId());
                            break;
                        case CustomConst.ROLE.DISTRIBUTOR:
                            operationVO.setOprMan(orderVO.getOrderControlPerson());
                            break;
                        case CustomConst.ROLE.WOCKER:
                            operationVO.setOprMan(orderVO.getOrderAppointPerson());
                            break;
                        default:
                            continue;
                    }
//                    operationManager(CustomConst.ADD, operationVO);
                    operationDao.add(operationVO.setActive(true));
                }
            } /*else if (operation != null && !StringUtils.isEmpty(operation.getOprMan())) {
                OperationVO operationVO = new OperationVO()
                        .setTacheId(tacheVO.getTacheId())
                        .setOrderId(orderVO.getOrderId())
                        .setOprMan(operation.getOprMan())
                        .setInfo(operation.getInfo());
                operationManager(CustomConst.ADD, operationVO);
            }*/ else {
                operationDao.set(new OperationVO()
                        .setActive(false)
                        .setFinishTime(new Timestamp(System.currentTimeMillis()))
                        .setOrderId(orderVO.getOrderId()));
            }
        }
        if (tacheId == null
                || tacheId < 0
                || "finish".equals(CustomConst.TACHE.TACHE_MAP.get(tacheId).getCode())) {
            newOrderVO.setFinishDate(Timestamp.valueOf(LocalDateTime.now()));
            newOrderVO.setOrderState(CustomConst.ORDER.STATE.END);
            orderDao.set(newOrderVO);
        }
    }

    public void toBeContinue() {
        final OrderVO nextOrderVO = new OrderVO().setOrderState(CustomConst.ORDER.STATE.WAIT_NEXT);
        orderDao.list(nextOrderVO).stream().forEach(orderVO -> {
            this.toBeContinue(orderVO);
        });
    }
}