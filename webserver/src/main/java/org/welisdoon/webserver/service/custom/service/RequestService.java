package org.welisdoon.webserver.service.custom.service;

import org.welisdoon.webserver.common.ApplicationContextProvider;

import javax.annotation.PostConstruct;

//@Service
//@VertxConfiguration
public class RequestService {

//    @Autowired
//    OrderDao orderDao;
//    @Autowired
//    TacheDao tacheDao;
//    @Autowired
//    UserDao userDao;
//    @Autowired
//    CarDao carDao;
//    @Autowired
//    OperationDao operationDao;
//    @Autowired
//    PictureDao pictureDao;
//    @Autowired
//    SqlSessionTemplate sqlSessionTemplate;
//    private static final Logger logger = LoggerFactory.getLogger(RequestService.class);
//
//    /*private static List<TacheVO> TACHE_VO_LIST;
//    private static Map<Integer, Integer> ROLE_MAP;*/
//
//    @PostConstruct
//    void init() {
//        /*TACHE_VO_LIST = tacheDao.listAll(new TacheVO().setTampalateId(1));
//        ROLE_MAP = userDao.listRoles()
//                .stream()
//                .collect(Collectors
//                        .toMap(UserVO.RoleConfig::getRoleId
//                                , UserVO.RoleConfig::getLevel));*/
//
//    }
//
//    /*扫单调用*/
//    @VertxWebApi
//    public void toBeContinue() {
//        final OrderVO nextOrderVO = new OrderVO().setOrderState(CustomConst.ORDER.STATE.WAIT_NEXT);
//        orderDao.list(nextOrderVO).stream().forEach(orderVO -> {
//            /*OrderVO newOrderVO = new OrderVO()
//                    .setOrderId(orderVO.getOrderId());
//            TacheVO tacheVO = null;
//            *//*Iterator<TacheVO> iterator = TACHE_VO_LIST.iterator();
//            while (iterator.hasNext()) {
//                if ((tacheVO = iterator.next()).getTacheId() == orderVO.getTacheId()) {
//                    if (tacheVO.getNextTache() == null) {
//                        tacheVO.setNextTache(iterator.hasNext() ?
//                                iterator.next() : new TacheVO().setTacheId(CustomConst.TACHE.STATE.END));
//                    }
//                    tacheVO = tacheVO.getNextTache();
//                    break;
//                }
//                else {
//                    tacheVO = null;
//                    continue;
//                }
//            }*//*
//            tacheVO = CustomConst.TACHE.TACHE_MAP.get(orderVO.getTacheId()).getNextTache();
//            if (tacheVO != null && tacheVO.getTacheId() >= 0) {
//                newOrderVO.setTacheId(tacheVO.getTacheId());
//                newOrderVO.setOrderState(CustomConst.ORDER.STATE.RUNNING);
//                orderDao.set(newOrderVO);
//                if (!CollectionUtils.isEmpty(tacheVO.getTacheRelas())) {
//                    tacheVO.getTacheRelas().stream().forEach(tacheRela -> {
//                        OperationVO operationVO = new OperationVO()
//                                .setOrderId(orderVO.getOrderId())
//                                .setTacheId(tacheRela.getChildTaches().get(0).getTacheId());
//                        switch (tacheRela.getRole()) {
//                            case CustomConst.ROLE.CUSTOMER:
//                                operationVO.setOprMan(orderVO.getCustId());
//                                break;
//                            case CustomConst.ROLE.DISTRIBUTOR:
//                                operationVO.setOprMan(orderVO.getOrderAppointPerson());
//                                break;
//                            case CustomConst.ROLE.WOCKER:
//                                operationVO.setOprMan(orderVO.getOrderControlPerson());
//                                break;
//                            default:
//                                return;
//                        }
//                        operationManager(CustomConst.ADD, operationVO);
//                    });
//                }
//            } else {
//                newOrderVO.setFinishDate(Timestamp.valueOf(LocalDateTime.now()));
//                newOrderVO.setOrderState(CustomConst.ORDER.STATE.END);
//                orderDao.set(newOrderVO);
//            }*/
//            this.toBeContinue(orderVO);
//        });
//    }
//
//    /*void test(OrderVO newOrderVO, OrderVO orderVO, Integer tacheId) {
//        switch (tacheId) {
//            case 2:
//                newOrderVO.setOrderControlPerson(orderVO.getCustId());
//                userDao.set(new UserVO().setRole(CustomConst.ROLE.DISTRIBUTOR).setId(orderVO.getCustId()));
//                orderVO.setOrderControlPerson(orderVO.getCustId());
//                break;
//            case 3:
//                newOrderVO.setOrderAppointPerson(orderVO.getCustId());
//                userDao.set(new UserVO().setRole(CustomConst.ROLE.WOCKER).setId(orderVO.getCustId()));
//                orderVO.setOrderAppointPerson(orderVO.getCustId());
//                break;
//            case CustomConst.TACHE.STATE.END:
//                userDao.set(new UserVO().setRole(CustomConst.ROLE.CUSTOMER).setId(orderVO.getCustId()));
//                break;
//            default:
//                return;
//        }
//    }*/
//
//    void toBeContinue(OrderVO orderVO/*, OperationVO operation*/) {
//        OrderVO newOrderVO = new OrderVO()
//                .setOrderId(orderVO.getOrderId());
//        Integer tacheId = CustomConst.TACHE.TACHE_MAP.get(orderVO.getTacheId()).getNextTache();
//        /*//测试用 目前自己授权给自己
//        {
//            test(newOrderVO, orderVO, tacheId);
//        }*/
//        if (tacheId != null && tacheId >= 0) {
//            TacheVO tacheVO = CustomConst.TACHE.TACHE_MAP.get(tacheId);
//            newOrderVO.setTacheId(tacheVO.getTacheId());
//            newOrderVO.setOrderState(CustomConst.ORDER.STATE.RUNNING);
//
//            orderDao.set(newOrderVO);
//            if (!CollectionUtils.isEmpty(tacheVO.getTacheRelas())) {
//                {
//                    operationDao.set(new OperationVO()
//                            .setActive(false)
//                            .setFinishTime(new Timestamp(System.currentTimeMillis()))
//                            .setOrderId(orderVO.getOrderId()));
//                }
//                for (TacheVO.TacheRela tacheRela :
//                        tacheVO.getTacheRelas()) {
//                    Integer newTacheId = getOptionTache(Arrays.asList(orderVO.getPassTache().split(",")), tacheRela.getChildTaches().get(0));
//                    if (newTacheId < 0) {
//                        continue;
//                    }
//                    OperationVO operationVO = new OperationVO()
//                            .setOrderId(orderVO.getOrderId())
//                            .setTacheId(newTacheId);
//                    switch (tacheRela.getRole()) {
//                        case CustomConst.ROLE.CUSTOMER:
//                            operationVO.setOprMan(orderVO.getCustId());
//                            break;
//                        case CustomConst.ROLE.DISTRIBUTOR:
//                            operationVO.setOprMan(orderVO.getOrderControlPerson());
//                            break;
//                        case CustomConst.ROLE.WOCKER:
//                            operationVO.setOprMan(orderVO.getOrderAppointPerson());
//                            break;
//                        default:
//                            continue;
//                    }
////                    operationManager(CustomConst.ADD, operationVO);
//                    operationDao.add(operationVO.setActive(true));
//                }
//            } /*else if (operation != null && !StringUtils.isEmpty(operation.getOprMan())) {
//                OperationVO operationVO = new OperationVO()
//                        .setTacheId(tacheVO.getTacheId())
//                        .setOrderId(orderVO.getOrderId())
//                        .setOprMan(operation.getOprMan())
//                        .setInfo(operation.getInfo());
//                operationManager(CustomConst.ADD, operationVO);
//            }*/ else {
//                operationDao.set(new OperationVO()
//                        .setActive(false)
//                        .setFinishTime(new Timestamp(System.currentTimeMillis()))
//                        .setOrderId(orderVO.getOrderId()));
//            }
//        } else {
//            newOrderVO.setFinishDate(Timestamp.valueOf(LocalDateTime.now()));
//            newOrderVO.setOrderState(CustomConst.ORDER.STATE.END);
//            orderDao.set(newOrderVO);
//        }
//    }
//
//    Integer getOptionTache(List<String> tacheIds, final TacheVO newTacheVo) {
//        if (tacheIds.stream().anyMatch(s ->
//                s != null ?
//                        s.trim().equals(newTacheVo.getTacheId().toString().trim())
//                        : false
//        )) {
//            if (newTacheVo.getNextTache() >= 0)
//                return getOptionTache(tacheIds, CustomConst.TACHE.TACHE_MAP.get(newTacheVo.getNextTache()));
//            else
//                return newTacheVo.getNextTache();
//        } else {
//            return newTacheVo.getTacheId();
//        }
//    }
//
//    /*工单管理*/
//    @VertxWebApi
//    public Object orderManger(int mode, Map params) {
//        Object resultObj = null;
//        OrderVO orderVO;
//        switch (mode) {
//            case CustomConst.ADD:
//                JsonObject orderVoJson = JsonObject.mapFrom(params);
//                List<Integer> jsonArray = (List<Integer>) orderVoJson.remove("pictureIds");
//
//                orderVO = orderVoJson.mapTo(OrderVO.class);
//                orderVO.setTacheId(CustomConst.TACHE.FIRST_TACHE.getTacheId())
//                        .setOrderState(CustomConst.ORDER.STATE.WAIT_NEXT);
//                UserVO userVO = userDao.get(new UserVO().setId(orderVO.getCustId()));
//                orderVO.setCustName(userVO.getName())
//                        .setCustPhone(StringUtils.isEmpty(orderVO.getCustPhone())
//                                ? userVO.getPhone()
//                                : orderVO.getCustPhone());
//                orderDao.add(orderVO);
//
//                if (jsonArray != null && jsonArray.size() > 0) {
//                    for (int i = 0; i < jsonArray.size(); i++) {
//                        pictureDao.set(new PictureVO()
//                                .setPictrueId(jsonArray.get(i))
//                                .setOrderId(orderVO.getOrderId())
//                                .setTacheId(orderVO.getTacheId()));
//                    }
//                }
//                resultObj = this.tacheSerivce(CustomConst.TACHE.GET_WORK_NUMBER, Map.of("userId", orderVO.getCustId()));
//                break;
//            case CustomConst.DELETE:
//                break;
//            /*case CustomConst.MODIFY:
//                orderVO = mapToObject(params, OrderVO.class);
//                resultObj = orderDao.set(orderVO);
//                break;*/
//            case CustomConst.LIST:
//                orderVO = mapToObject(params, OrderVO.class);
//                List list = orderDao.listAll(orderVO);
//                resultObj = list;
//                break;
//            case CustomConst.GET:
//                orderVO = mapToObject(params, OrderVO.class);
//                resultObj = orderDao.get(orderVO);
//                break;
//            case CustomConst.ORDER.GET_WORK_NUMBER:
//                orderVO = mapToObject(params, OrderVO.class);
//                resultObj = orderDao.getWorkIngOrderNum(orderVO);
//                break;
//            case CustomConst.ORDER.APPIONT_WORKER:
//                orderVO = new OrderVO()
////                        .setOrderAppointPerson(MapUtils.getString(params, "orderControlPerson"))
//                        .setOrderId(MapUtils.getInteger(params, "orderId"));
//                orderVO = orderDao.get(orderVO);
//                if (orderVO != null) {
//                    resultObj = orderDao.set(new OrderVO()
//                            .setOrderId(orderVO.getOrderId())
//                            .setOrderAppointPerson(MapUtils.getString(params, "orderAppointPerson")));
//                }
//            default:
//                break;
//
//        }
//        return resultObj;
//    }
//
//    /*车辆管理*/
//    @VertxWebApi
//    public Object carManger(int mode, Map params) {
//        Object resultObj = null;
//        CarVO carVO = null;
//        switch (mode) {
//            case CustomConst.LIST:
//                carVO = mapToObject(params, CarVO.class);
//                resultObj = carDao.list(carVO);
//                break;
//            case CustomConst.DELETE:
//                carVO = mapToObject(params, CarVO.class);
//                resultObj = carDao.del(carVO);
//                break;
//            case CustomConst.ADD:
//                Object phone = params.remove("phone");
//                carVO = mapToObject(params, CarVO.class);
//                carDao.add(carVO);
//                if (!StringUtils.isEmpty(phone)) {
//                    userDao.set(new UserVO().setId(carVO.getUserId()).setPhone(phone.toString()));
//                }
//                resultObj = carVO;
//                break;
//            case CustomConst.MODIFY:
//                carVO = mapToObject(params, CarVO.class);
//                resultObj = carDao.set(carVO);
//                break;
//            default:
//                break;
//        }
//        return resultObj;
//    }
//
//    /*用户管理*/
//    @VertxWebApi
//    public Object userManger(int mode, Map params) {
//        Object resultObj = null;
//        UserVO userVO = null;
//        switch (mode) {
//            case CustomConst.GET:
//                userVO = mapToObject(params, UserVO.class);
//                resultObj = userDao.get(userVO);
//                break;
//            case CustomConst.LIST:
//                userVO = mapToObject(params, UserVO.class);
//                resultObj = userDao.list(userVO);
//                break;
//            case CustomConst.USER.GET_WORKERS:
//                userVO = mapToObject(params, UserVO.class);
//                List<UserVO> userVOS = userDao.list(new UserVO().setRole(CustomConst.ROLE.WOCKER));
//                if (!StringUtils.isEmpty(userVO.getId())) {
//                    userVOS.add(0, userDao.get(new UserVO().setId(userVO.getId())));
//                }
//                resultObj = userVOS;
//                break;
//            case CustomConst.ADD:
//                userVO = mapToObject(params, UserVO.class);
//                userVO.setRole(CustomConst.ROLE.CUSTOMER);
//                resultObj = userDao.add(userVO);
//            case CustomConst.MODIFY:
//                userVO = mapToObject(params, UserVO.class);
//                userDao.set(new UserVO().setRole(userVO.getRole()).setId(userVO.getId()));
//                resultObj = userVO;
//            default:
//                break;
//        }
//        return resultObj;
//    }
//
//    /*环节管理*/
//    @VertxWebApi
//    public Object tacheSerivce(int mode, Map params) {
//        Object resultObj = null;
//        TacheVO tacheVO;
//        switch (mode) {
//            case CustomConst.GET:
//                tacheVO = mapToObject(params, TacheVO.class);
//                resultObj = tacheDao.list(tacheVO);
//                break;
//            case CustomConst.LIST:
//                tacheVO = mapToObject(params, TacheVO.class);
//                List list = tacheDao.listAll(tacheVO);
//                resultObj = list;
//                break;
//            case CustomConst.TACHE.GET_WORK_NUMBER:
//                resultObj = tacheDao.getProccess(params);
//                break;
//            default:
//                break;
//        }
//        return resultObj;
//    }
//
//    /*环节 操作 管理*/
//    @VertxWebApi
//    public Object operationManager(int mode, Map params) {
//        Object resultObj = null;
//        OperationVO operationVO;
//        switch (mode) {
//            case CustomConst.ADD:
//                break;
//            case CustomConst.GET:
//                operationVO = mapToObject(params, OperationVO.class);
//                resultObj = operationDao.list(operationVO);
//                break;
//            case CustomConst.LIST:
//                operationVO = mapToObject(params, OperationVO.class);
//                List list = operationDao.listAll(operationVO);
//                resultObj = list;
//                break;
//            default:
//                operationVO = mapToObject(params, OperationVO.class);
//                resultObj = operationManager(mode, operationVO);
//                break;
//        }
//        return resultObj;
//    }
//
//    /*环节 操作 内部 管理*/
//    Object operationManager(int mode, OperationVO operationVO) {
//        Object resultObj = null;
//        switch (mode) {
//            case CustomConst.ADD:
//                operationDao.set(new OperationVO()
//                        .setActive(false)
//                        .setFinishTime(new Timestamp(System.currentTimeMillis()))
//                        .setOprMan(operationVO.getOprMan())
//                        .setOrderId(operationVO.getOrderId()));
//                resultObj = operationDao.add(operationVO.setActive(true));
//                break;
//            case CustomConst.GET:
//                List list = operationDao.list(operationVO);
//                resultObj = list;
//                break;
//            default:
//                break;
//        }
//        return resultObj;
//    }
//
//    /*登陆初始化*/
//    public Object login(String userId) {
//        JsonObject jsonObject = new JsonObject();
//        if (!StringUtils.isEmpty(userId)) {
//            Object o = userManger(CustomConst.GET, Map.of("id", userId));
//            UserVO userVO;
//            if (o == null) {
//                userVO = new UserVO().setId(userId);
//                userVO.setRole(CustomConst.ROLE.GUEST);
//                userVO.setName("新用戶");
//                jsonObject.put("user", JsonObject.mapFrom(userVO));
//            } else {
//                userVO = (UserVO) o;
//                jsonObject.put("user", JsonObject.mapFrom(o));
//                switch (userVO.getRole()) {
//                    case CustomConst.ROLE.CUSTOMER:
//                        o = tacheSerivce(CustomConst.TACHE.GET_WORK_NUMBER, Map.of("userId", userVO.getId()));
//                        break;
//                    case CustomConst.ROLE.DISTRIBUTOR:
//                        o = orderManger(CustomConst.ORDER.GET_WORK_NUMBER, Map.of("orderControlPerson", userVO.getId()));
//                        break;
//                    case CustomConst.ROLE.WOCKER:
//                        o = orderManger(CustomConst.ORDER.GET_WORK_NUMBER, Map.of("orderAppointPerson", userVO.getId()));
//                        break;
//
//                }
//                o = o != null ? JsonObject.mapFrom(o) : Map.of("all_nums", 0, "nums", 0);
//                jsonObject.put("work", o);
//                jsonObject.put("cars", carDao.list(new CarVO().setUserId(userId).setDefaultSelected(1)));
//            }
//        }
//        return jsonObject;
//    }
//
//    /*下一步*/
//    @VertxWebApi
//    public Object toBeContinue(Map map) {
//        Object returnObj = null;
//        Integer orderId = MapUtils.getInteger(map, "orderId", null);
//        Integer tacheId = MapUtils.getInteger(map, "tacheId", null);
//        String userId = MapUtils.getString(map, "userId", null);
//        String info = MapUtils.getString(map, "info", null);
//        /*下个环节还是自己操作时，能直接让工单执行*/
//        boolean doNext = MapUtils.getBoolean(map, "doNext", false);
//        if (StringUtils.isEmpty(orderId)
//                || tacheId == null
//                || StringUtils.isEmpty(userId)) {
//            return returnObj;
//        }
//
//        UserVO userInfo = userDao.get(new UserVO().setId(userId));
//        OrderVO orderVO = orderDao.get(new OrderVO().setOrderId(orderId));
//        TacheVO orderTache = CustomConst.TACHE.TACHE_MAP.get(orderVO.getTacheId());
//        TacheVO queryTacheVo = CustomConst.TACHE.TACHE_MAP.get(tacheId);
//        OperationVO operationVO = operationDao.get(new OperationVO()
//                .setOrderId(orderVO.getOrderId())
//                .setOprMan(userInfo.getId()).setTacheId(tacheId).setActive(true));
//
//        /*走工单子流程*/
//        if (queryTacheVo == null) return null;
//
//        if (!StringUtils.isEmpty(info)) {
//            this.operationInfoDeal(map);
//        }
//
//        /*流程有权限限制*/
//
////        boolean nextOrder = false;
////        boolean nextOperation = false;
////        /*为主环节，该环节没有关联环节,则让他往下走*/
////        if ((orderTache == queryTacheVo && orderTache.getTacheRelas().size() <= 0)) {
////            nextOrder = true;
////        }
////        /*不为主环节，且环节走到尽头*/
////        /*当前可操作操作数不大于1*/
////        else if ((orderTache != queryTacheVo && queryTacheVo.getNextTache() != null
////                && queryTacheVo.getNextTache()/*.getTacheId()*/ < 0)
////                && operationDao.num(new OperationVO().setOrderId(orderVO.getOrderId()).setActive(true)) <= 1) {
////            //没有下一环节
////            nextOrder = true;
////        }
////        /*不管是不是主环节 有子环节都要处理*/
////        else {
////            nextOperation = true;
////        }
////        /*走工单*/
////        if (nextOrder) {
////            if (doNext) {
////                /*直接触发过单*/
////                returnObj = new OperationVO()
////                        .setOrderId(orderVO.getOrderId())
////                        .setTacheId(CustomConst.TACHE.STATE.END)
////                        .setOprMan(userId)
////                /*.setInfo(info)*/;
////                this.toBeContinue(orderVO/*, (OperationVO) returnObj*/);
////            } else {
////                orderDao.set(new OrderVO().setOrderId(orderVO.getOrderId()).setOrderState(CustomConst.ORDER.STATE.WAIT_NEXT));
////                returnObj = new OperationVO().setOrderId(orderVO.getOrderId()).setTacheId(CustomConst.TACHE.STATE.WAIT);
////            }
////        } else if (nextOperation) {
////            /*走工单子流程*/
////            OperationVO operationVO = operationDao.get(new OperationVO()
////                    .setOrderId(orderVO.getOrderId())
////                    .setOprMan(userInfo.getId()).setTacheId(tacheId).setActive(true));
////            if (operationVO != null) {
//////                operationDao.set(operationVO.setInfo(info));
////                /*没有下一环节了*/
////                /*或者因为跳过没有下一环节了*/
////                Integer nextTache = queryTacheVo.getNextTache();
////                if (CustomConst.TACHE.STATE.END == nextTache
////                        || CustomConst.TACHE.STATE.END == (nextTache = getOptionTache(Arrays.asList(orderVO.getPassTache().split(",")), CustomConst.TACHE.TACHE_MAP.get(nextTache)))) {
////                    /*存在非用户operationVO 证明有其他人也在处理*/
////                    if (this.waitOtherOperation(operationDao.list(new OperationVO().setOrderId(orderVO.getOrderId()).setActive(true)), operationVO)) {
////                        /*结束自己的操作*/
////                        operationDao.set(new OperationVO()
////                                .setActive(false)
////                                .setFinishTime(new Timestamp(System.currentTimeMillis()))
////                                .setOprMan(operationVO.getOprMan())
////                                .setOrderId(operationVO.getOrderId())
////                                .setTacheId(operationVO.getTacheId()));
////                        /*让当前人员等待*/
////                        operationVO.setTacheId(CustomConst.TACHE.STATE.WAIT);
////                    } else if (doNext) {
////                        /*直接触发过单*/
////                        this.toBeContinue(orderVO/*, operationVO
////                                .setInfo(info)*/);
////                    } else
////                        /*提交到扫单触发过单*/
////                        orderDao.set(new OrderVO()
////                                .setOrderId(orderVO.getOrderId())
////                                .setOrderState(CustomConst.ORDER.STATE.WAIT_NEXT));
////                } else {
////                    operationManager(CustomConst.ADD, operationVO.setTacheId(nextTache));
////                }
////                returnObj = operationVO;
////            }
////        }
////        return returnObj;
//
//        Integer nextTache = queryTacheVo.getNextTache();
//        int num = operationDao.num(new OperationVO().setOrderId(orderVO.getOrderId()).setActive(true));
//        int operationMode = -1;
//        /*为主环节，该环节没有关联环节,则让他往下走*/
//        if ((orderTache.getTacheId().equals(queryTacheVo.getTacheId()) && orderTache.getTacheRelas().size() <= 0)) {
//            operationMode = doNext ? 1 : 0;
//        } else if (!orderTache.getTacheId().equals(queryTacheVo.getTacheId())
//                && (orderTache.getTacheId().equals(queryTacheVo.getSuperTache()))) {
//            ///*不为主环节，且环节走到尽头*/
//            ///*不为主环节，但一定是当前环节的子环节*/
//            if (queryTacheVo.getNextTache() != null
//                    && queryTacheVo.getNextTache() < 0
//                    && num <= 1) {
//                //没有下一环节了，且没其他人也完成了
//                operationMode = doNext ? 1 : 0;
//            } else if (operationVO != null) {
//                //当前环节有操作记录
//                if (CustomConst.TACHE.STATE.END == nextTache
//                        || CustomConst.TACHE.STATE.END == (nextTache = getOptionTache(Arrays.asList(orderVO.getPassTache().split(",")), CustomConst.TACHE.TACHE_MAP.get(nextTache)))) {
//                    //下一环节没有了
//                    //或者因为有跳过的环节下一环没有了
//                    if (num > 1) {
//                        //还有其他操作数
//                        operationMode = 2;
//                    } else if (doNext) {
//                        //快速过单
//                        operationMode = 3;
//                    } else {
//                        //直接增加操作
//                        operationMode = 4;
//                    }
//                } else {
//                    operationMode = 5;
//                }
//            }
//        }
//        /*不管是不是主环节 有子环节都要处理*/
//        /*走工单*/
//        switch (operationMode) {
//            case 0:
//                orderDao.set(new OrderVO().setOrderId(orderVO.getOrderId()).setOrderState(CustomConst.ORDER.STATE.WAIT_NEXT));
//                returnObj = new OperationVO().setOrderId(orderVO.getOrderId()).setTacheId(CustomConst.TACHE.STATE.WAIT);
//                break;
//            case 1:
//                returnObj = new OperationVO()
//                        .setOrderId(orderVO.getOrderId())
//                        .setTacheId(CustomConst.TACHE.STATE.END)
//                        .setOprMan(userId);
//                this.toBeContinue(orderVO);
//                break;
//            case 2:
//                operationDao.set(new OperationVO()
//                        .setActive(false)
//                        .setFinishTime(new Timestamp(System.currentTimeMillis()))
//                        .setOprMan(operationVO.getOprMan())
//                        .setOrderId(operationVO.getOrderId())
//                        .setTacheId(operationVO.getTacheId()));
//                /*让当前人员等待*/
//                operationVO.setTacheId(CustomConst.TACHE.STATE.WAIT);
//                returnObj = operationVO;
//                break;
//            case 3:
//                /*直接触发过单*/
//                this.toBeContinue(orderVO);
//                returnObj = operationVO;
//                break;
//            case 4:
//                /*提交到扫单触发过单*/
//                orderDao.set(new OrderVO()
//                        .setOrderId(orderVO.getOrderId())
//                        .setOrderState(CustomConst.ORDER.STATE.WAIT_NEXT));
//                returnObj = operationVO;
//                break;
//            case 5:
//                operationManager(CustomConst.ADD, operationVO.setTacheId(nextTache));
//                returnObj = operationVO;
//                break;
//
//        }
//        return returnObj;
//    }
//
//    private void operationInfoDeal(Map map) {
//        Integer orderId = MapUtils.getInteger(map, "orderId", null);
//        Integer tacheId = MapUtils.getInteger(map, "tacheId", null);
//        String userId = MapUtils.getString(map, "userId", null);
//        try {
//            JsonObject infoJson = JsonObject.mapFrom(map.get("info"));
//            if (infoJson.containsKey("pictureIds")) {
//                JsonArray jsonArray = infoJson.getJsonArray("pictureIds");
//                if (jsonArray != null && jsonArray.size() > 0) {
//                    for (int i = 0; i < jsonArray.size(); i++) {
//                        pictureDao.set(new PictureVO()
//                                .setPictrueId(jsonArray.getInteger(i))
//                                .setOrderId(orderId)
//                                .setTacheId(tacheId));
//                    }
//                }
//            }
//            if (infoJson.containsKey("setWorker")) {
//                String workerId = infoJson.getString("setWorker");
//                OrderVO orderVO;
//                UserVO userVO;
//                synchronized (this) {
//                    if (!StringUtils.isEmpty(workerId)
//                            && (orderVO = orderDao.get(new OrderVO().setOrderId(orderId))) != null
//                            && StringUtils.isEmpty(orderVO.getOrderAppointPerson())
//                            && (userVO = userDao.get(new UserVO().setId(workerId))) != null) {
//                        orderDao.set(new OrderVO()
//                                .setOrderId(orderVO.getOrderId())
//                                .setOrderAppointPerson(userVO.getId())
//                                .setOrderAppointPersonName(userVO.getName())
//                                .setOrderAppointPhone(userVO.getPhone())
//                                .setOrderControlPerson(userId));
//                    }
//                }
//            }
//
//            OperationVO operationVO = operationDao.get(new OperationVO()
//                    .setOrderId(orderId)
//                    .setOprMan(userId).setTacheId(tacheId).setActive(true));
//            if (operationVO != null) {
//                operationDao.set(operationVO.setInfo(infoJson.toString()));
//            }
//        } catch (Throwable e) {
//            e.printStackTrace();
//        }
//    }
//
//    private boolean waitOtherOperation(List<OperationVO> operationVOList, OperationVO operationVO) {
//        Stream<OperationVO> stream = operationVOList.stream();
//        long all = operationVOList.size();
//        long orther = stream.filter(operationVO1 -> !operationVO1.getOprMan().equals(operationVO.getOprMan())).count();
//        return orther > 1 || (all - orther > 0);
//    }
//
//    public Object uploadFile(Map fileUpload, Map map) {
//        InputStream inputStream = null;
//        ByteArrayOutputStream outputStream = null;
//        Object returnObj = null;
//        try {
//            inputStream = new FileInputStream(new File(MapUtils.getString(fileUpload, "uploadedFileName")));
//            outputStream = new ByteArrayOutputStream();
//            StreamUtils.writeStream(inputStream, outputStream);
//            byte[] bytes = outputStream.toByteArray();
//            PictureVO pictureVO = new PictureVO()
//                    .setData(bytes)
//                    .setName(MapUtils.getString(fileUpload, "fileName"))
//                    .setOrderId(MapUtils.getInteger(map, "orderId"))
//                    .setTacheId(MapUtils.getInteger(map, "tacheId"));
//            pictureDao.add(pictureVO);
//            returnObj = pictureVO.setData(null);
//        } catch (Throwable e) {
//            e.printStackTrace();
//        } finally {
//            StreamUtils.close(inputStream);
//            StreamUtils.close(outputStream);
//        }
//        return returnObj;
//    }
//
//    @VertxWebApi
//    public Object pictureManager(int mode, Map map) {
//        Object returnObj = null;
//        PictureVO pictureVO;
//        switch (mode) {
//            case CustomConst.GET:
//                pictureVO = mapToObject(map, PictureVO.class);
//                returnObj = pictureDao.get(pictureVO);
//                break;
//            case CustomConst.DELETE:
//                pictureVO = mapToObject(map, PictureVO.class);
//                pictureDao.del(pictureVO);
//                break;
//        }
//        return returnObj;
//    }
//
//    /*private static TacheVO findTache(TacheVO queryTache, List<TacheVO> fullTaches, UserVO userinfo) {
//        if (queryTache == null
//                || queryTache.getTacheId() == null
//                || queryTache.getTacheId() < 0) {
//            return null;
//        }
//
//        Iterator<TacheVO> tacheVOIterator = fullTaches.iterator();
//        TacheVO targetTacheVo = null;
//        TacheVO iteratorTempVo = null;
//        while (tacheVOIterator.hasNext()) {
//            iteratorTempVo = tacheVOIterator.next();
//            if (queryTache.getTacheId() == iteratorTempVo.getTacheId()) {
//                targetTacheVo = iteratorTempVo;
//                if (targetTacheVo.getNextTache() == null) {
//                    targetTacheVo.setNextTache(tacheVOIterator.hasNext() ?
//                            tacheVOIterator.next() : new TacheVO().setTacheId(CustomConst.TACHE.STATE.END));
//                }
//                break;
//            }
//            else if (!CollectionUtils.isEmpty(iteratorTempVo.getTacheRelas())) {
//                *//*for (TacheVO.TacheRela tmepRela : tmepVo.getTacheRelas()) {
//                    if (ROLE_MAP.get(tmepRela.getRole()) >= userinfo.getLevel()) {
//                        tmepVo = findTache(queryTache, tmepRela.getChildTaches(), userinfo);
//                        if (tmepVo != null) {
//                            targetTacheVo = tmepVo;
//                            break;
//                        }
//                    }
//                }*//*
//                targetTacheVo = findTache(queryTache, iteratorTempVo, userinfo);
//                if (targetTacheVo != null) break;
//            }
//        }
//        return targetTacheVo;
//    }
//
//    private static TacheVO findTache(TacheVO queryTache, TacheVO iteratorTempVo, UserVO userinfo) {
//        TacheVO targetTacheVo = null;
//        for (TacheVO.TacheRela tmepRela : iteratorTempVo.getTacheRelas()) {
//            if (CustomConst.ROLE.ROLE_MAP.get(tmepRela.getRole()) >= userinfo.getLevel()) {
//                iteratorTempVo = findTache(queryTache, tmepRela.getChildTaches(), userinfo);
//                if (iteratorTempVo != null) {
//                    targetTacheVo = iteratorTempVo;
//                    break;
//                }
//            }
//        }
//        return targetTacheVo;
//    }*/
//
//    private static <T> T mapToObject(Map params, Class<T> type) {
//        return JsonObject.mapFrom(params).mapTo(type);
//    }


    TacheService tacheService;
    OrderService orderService;
    UserService userService;
    OperationService operationService;
    CarService carService;
    PictureService pictureService;
    EvaluateService evaluateSerivce;

    @PostConstruct
    void init() {
        tacheService = ApplicationContextProvider.getBean(TacheService.class);
        orderService = ApplicationContextProvider.getBean(OrderService.class);
        userService = ApplicationContextProvider.getBean(UserService.class);
        operationService = ApplicationContextProvider.getBean(OperationService.class);
        carService = ApplicationContextProvider.getBean(CarService.class);
        pictureService = ApplicationContextProvider.getBean(PictureService.class);
        evaluateSerivce = ApplicationContextProvider.getBean(EvaluateService.class);
    }

//    public void toBeContinue() {
//        operationSerivce.toBeContinue();
//    }

//    @VertxWebApi
//    public Object toBeContinue(Map orderMap) {
//        return operationSerivce.toBeContinue(orderMap);
//    }

    /*工单管理*/
//    @VertxWebApi
//    public Object orderManger(int mode, Map params) {
//        return orderService.handle(mode, params);
//    }

//    /*车辆管理*/
//    @VertxWebApi
//    public Object carManger(int mode, Map params) {
//        return carSerivce.handle(mode, params);
//    }
//
//    /*用户管理*/
//    @VertxWebApi
//    public Object userManger(int mode, Map params) {
//        return userSerivce.handle(mode, params);
//    }
//
//    /*环节管理*/
//    @VertxWebApi
//    public Object tacheManager(int mode, Map params) {
//        return tacheSerivce.handle(mode, params);
//    }
//
//    /*环节 操作 管理*/
//    @VertxWebApi
//    public Object operationManager(int mode, Map params) {
//        return operationSerivce.handle(mode, params);
//    }
//
//    /*图片 管理*/
//    @VertxWebApi
//    public Object pictureManager(int mode, Map params) {
//        return pictureSerivce.handle(mode, params);
//    }
//
//    /*支付 管理*/
//    @VertxWebApi
//    public Object payManger(int mode, Map params) {
//        return pictureSerivce.handle(mode, params);
//    }
//    /*评价 操作 管理*/
//    @VertxWebApi
//    public Object evaluateManager(int mode, Map params) {
//        return evaluateSerivce.handle(mode, params);
//    }

//    /*登陆初始化*/
//    @VertxWebApi
//    public Object login(String userId) {
//        return userSerivce.login(userId);
//    }

//    @VertxWebApi
//    public Object uploadFile(Map fileUpload, Map map) {
//        return pictureSerivce.uploadFile(fileUpload, map);
//    }


}

