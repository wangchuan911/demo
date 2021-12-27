package com.hubidaauto.servmarket.module.order.service;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.dynamic.datasource.annotation.DS;
import com.hubidaauto.servmarket.module.common.dao.AppConfigDao;
import com.hubidaauto.servmarket.module.common.entity.AppConfig;
import com.hubidaauto.servmarket.module.flow.enums.OperationType;
import com.hubidaauto.servmarket.module.flow.enums.OrderStatus;
import com.hubidaauto.servmarket.module.flow.enums.ServiceContent;
import com.hubidaauto.servmarket.module.flow.enums.WorkOrderStatus;
import com.hubidaauto.servmarket.module.goods.dao.ItemDao;
import com.hubidaauto.servmarket.module.goods.dao.ItemTypeDao;
import com.hubidaauto.servmarket.module.goods.entity.ItemCondition;
import com.hubidaauto.servmarket.module.goods.entity.ItemTypeVO;
import com.hubidaauto.servmarket.module.log.dao.OrderPayDaoLog;
import com.hubidaauto.servmarket.module.log.entity.OrderPayLogVO;
import com.hubidaauto.servmarket.module.order.annotation.OrderClass;
import com.hubidaauto.servmarket.module.order.consts.WorkLoadUnit;
import com.hubidaauto.servmarket.module.order.dao.BaseOrderDao;
import com.hubidaauto.servmarket.module.order.dao.ServiceClassOrderDao;
import com.hubidaauto.servmarket.module.order.entity.*;
import com.hubidaauto.servmarket.module.order.model.IOrderService;
import com.hubidaauto.servmarket.module.order.model.IOverTimeOperationable;
import com.hubidaauto.servmarket.module.popularize.model.IRebate;
import com.hubidaauto.servmarket.module.staff.dao.StaffTaskDao;
import com.hubidaauto.servmarket.module.staff.entity.StaffCondition;
import com.hubidaauto.servmarket.module.staff.entity.StaffTaskVO;
import com.hubidaauto.servmarket.module.user.dao.AddressDao;
import com.hubidaauto.servmarket.module.user.dao.AppUserDao;
import com.hubidaauto.servmarket.module.user.entity.AddressVO;
import com.hubidaauto.servmarket.module.user.entity.AppUserVO;
import com.hubidaauto.servmarket.module.user.entity.UserCondition;
import com.hubidaauto.servmarket.module.workorder.dao.ServiceClassWorkOrderDao;
import com.hubidaauto.servmarket.module.workorder.entity.ServiceClassWorkOrderCondition;
import com.hubidaauto.servmarket.module.workorder.entity.ServiceClassWorkOrderVO;
import com.hubidaauto.servmarket.module.workorder.entity.WorkOrderVO;
import com.hubidaauto.servmarket.weapp.ServiceMarketConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.welisdoon.flow.module.flow.entity.Flow;
import org.welisdoon.flow.module.flow.entity.Stream;
import org.welisdoon.flow.module.flow.entity.StreamStatus;
import org.welisdoon.flow.module.flow.intf.FlowEvent;
import org.welisdoon.web.common.ApplicationContextProvider;
import org.welisdoon.web.common.config.AbstractWechatConfiguration;
import org.welisdoon.web.entity.wechat.WeChatPayOrder;
import org.welisdoon.web.entity.wechat.WeChatRefundOrder;
import org.welisdoon.web.entity.wechat.payment.requset.PayBillRequsetMesseage;
import org.welisdoon.web.entity.wechat.payment.requset.PrePayRequsetMesseage;
import org.welisdoon.web.entity.wechat.payment.requset.RefundRequestMesseage;
import org.welisdoon.web.entity.wechat.payment.requset.RefundResultMesseage;
import org.welisdoon.web.entity.wechat.payment.response.PayBillResponseMesseage;
import org.welisdoon.web.entity.wechat.payment.response.RefundReplyMesseage;
import org.welisdoon.web.service.wechat.intf.IWechatPayHandler;
import org.welisdoon.web.vertx.verticle.WorkerVerticle;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @Classname ServMallOrderService
 * @Description TODO
 * @Author wang.zhidong
 * @Date 2021/8/13 12:13
 */
@Service
@DS("shop")
@Transactional(rollbackFor = Throwable.class)
@OrderClass(
        id = 1000L)
public class ServiceClassOrderService implements FlowEvent, IOrderService<ServiceClassOrderCondition, ServiceClassWorkOrderCondition>, IWechatPayHandler, IOverTimeOperationable, IRebate {
    private static final Logger logger = LoggerFactory.getLogger(ServiceClassOrderService.class);
    static long TEMPLATE_ID = 1L, SIMPLE_NODE_ID = 6L;
    FlowProxyService flowService;
    ServiceClassOrderDao orderDao;
    BaseOrderDao baseOrderDao;
    ItemDao itemDao;
    ItemTypeDao itemTypeDao;
    ServiceClassWorkOrderDao workOrderDao;
    StaffTaskDao staffTaskDao;
    AddressDao addressDao;
    AppUserDao appUserDao;
    OrderPayDaoLog orderPrePayDaoLog;
    AppConfigDao appConfigDao;

    @Autowired
    public void init(AppUserDao appUserDao, AddressDao addressDao, StaffTaskDao staffTaskDao, ServiceClassWorkOrderDao workOrderDao, FlowProxyService flowService, ServiceClassOrderDao orderDao, ItemDao itemDao, ItemTypeDao itemTypeDao, BaseOrderDao baseOrderDao, OrderPayDaoLog orderPrePayDaoLog,
                     AppConfigDao appConfigDao) {
        this.workOrderDao = workOrderDao;
        this.orderDao = orderDao;
        this.flowService = flowService;
        this.baseOrderDao = baseOrderDao;
        this.itemDao = itemDao;
        this.itemTypeDao = itemTypeDao;
        this.staffTaskDao = staffTaskDao;
        this.addressDao = addressDao;
        this.appUserDao = appUserDao;
        this.orderPrePayDaoLog = orderPrePayDaoLog;
        this.appConfigDao = appConfigDao;
    }

    @Override
    public ServiceClassOrderVO order(ServiceClassOrderCondition condition) {
        System.out.println(Thread.currentThread());
        ServiceClassOrderVO orderVO = new ServiceClassOrderVO(condition.getForm());
        orderVO.setClassId(condition.getClassId());
        orderVO.setCustId(condition.getCustId());
        ;
        baseOrderDao.add(orderVO);
        orderDao.add(orderVO);

        Flow flow = new Flow();
        flow.setTemplateId(TEMPLATE_ID);
        flow.setFunctionId(5L);
        this.flowService.flow(flow);

        orderVO.setFlowId(flow.getId());
        orderVO.setStatusId(OrderStatus.PRE_PAY.statusId());
        orderVO.setDesc(this.orderDetail(orderVO.getId()).stream().filter(detailVO -> detailVO.getValue() != null).map(detailVO -> detailVO.getValue().toString()).collect(Collectors.joining(" ")));
        orderVO.setCode(String.format("%6d%s%04d%s%09d", condition.getForm().getRegionId(), LocalDateTime.now().format(DateTimeFormatter.ofPattern("YYYYMMddHH")), orderVO.getCustId(), "SCO", orderVO.getId()));
        baseOrderDao.put(orderVO);
        return orderVO;
    }


    @Override
    public void start(ServiceClassOrderCondition condition) {
        ServiceClassOrderVO orderVO = orderDao.get(condition.getId());
        orderVO.setStatusId(OrderStatus.READY.statusId());
        baseOrderDao.put(orderVO);

        this.flowService.start(orderVO.getFlowId());
    }


    @Override
    public void workOrder(ServiceClassWorkOrderCondition workOrderCondition) {
        ServiceClassWorkOrderVO workOrderVO = workOrderDao.get(workOrderCondition.getId());
        /*if (!workOrderCondition.getOrderId().equals(workOrderVO.getOrderId()))
            throw new RuntimeException("工单数据异常");*/
        ServiceClassOrderVO orderVO = orderDao.get(workOrderVO.getOrderId());
        String operation = workOrderVO.getOperation();
        OperationType type;
        if (!StringUtils.isEmpty(operation) && (type = OperationType.getInstance(operation)) != null) {
            switch (type) {
                case SIGN_UP:
                    break;
                case SERVICING:
                    Stream superStream = this.flowService.getStream(this.flowService.getStream(workOrderVO.getStreamId()).getSuperId());
                    if (superStream.getValue() == null) break;
                    ServiceContent serviceContent = ServiceContent.getInstance(superStream.getValue().jsonValue().getLong("function"));
                    /*if (serviceContent.equals(ServiceContent.HOME_CLEAN)) {
                        logger.info(ServiceContent.HOME_CLEAN.toString());
                    } else if (serviceContent.equals(ServiceContent.HOME_EQP_CLEAN)) {
                        logger.info(ServiceContent.HOME_CLEAN.toString());
                    }*/
                    break;
                case DISPATCH:
                    List<Long> staffIds = workOrderCondition.getData().getJSONArray("workers").toJavaList(Long.class);
                    ApplicationContextProvider.getBean(ServiceClassOrderService.class).addStaffTask(staffIds, orderVO);
                    break;
                case CUST_COMFIRM:
                    break;
                default:
                    throw new RuntimeException("未知操作");
            }
        }
        try {
            this.flowService.stream(workOrderVO.getStreamId());
        } catch (Throwable e) {
            ApplicationContextProvider.getBean(ServiceClassOrderService.class).undo(workOrderVO);
            throw e;
        }
    }

    @Transactional(rollbackFor = Throwable.class, propagation = Propagation.REQUIRES_NEW)
    public void undo(ServiceClassWorkOrderVO workOrderVO) {
        workOrderVO.setStatusId(WorkOrderStatus.READY.statusId());
        workOrderDao.put(workOrderVO);
    }

    @Transactional(rollbackFor = Throwable.class, propagation = Propagation.REQUIRES_NEW)
    public void addStaffTask(List<Long> staffIds, ServiceClassOrderVO orderVO) {
        for (Long staffId : staffIds) {
            staffTaskDao.add(new StaffTaskVO().setOrderId(orderVO.getId()).setStaffId(staffId).setTaskTime(orderVO.getBookTime()));
        }
    }

    @Override
    public ServiceClassOrderVO getOrder(Long orderId) {
        ServiceClassOrderVO orderVO = orderDao.get(orderId);
        orderVO.setItemType(itemTypeDao.get(orderVO.getItemTypeId()));
        orderVO.setItem(itemDao.get(orderVO.getItemType().getItemId()));
        return orderVO;
    }

    @Override
    public WorkOrderVO getWorkOrder(ServiceClassWorkOrderCondition workOrderCondition) {
        ServiceClassWorkOrderVO workOrderVO = workOrderDao.find(workOrderCondition);
        workOrderVO.setStream(this.flowService.getStream(workOrderVO.getStreamId()));
        return workOrderVO;
    }

    @Override
    public List<WorkOrderVO> getWorkOrders(ServiceClassWorkOrderCondition workOrderCondition) {
        List<ServiceClassWorkOrderVO> list = workOrderDao.list(workOrderCondition);
        for (ServiceClassWorkOrderVO workOrderVO : list) {
            workOrderVO.setStream(this.flowService.getStream(workOrderVO.getStreamId()));
            switch (workOrderCondition.getQuery()) {
                case "doing":
                    Stream superStream = flowService.getStream(workOrderVO.getStream().getSuperId());
                    if (superStream.getValue() == null) continue;
                    ServiceContent serviceContent = ServiceContent.getInstance(superStream.getValue().jsonValue().getLong("function"));
                    if (serviceContent != null) {
                        workOrderVO.setOperation(serviceContent.getCode());
                        workOrderVO.getStream().setName(serviceContent.getDesc());
                    }
                    break;
            }
        }
        return (List) list;
    }


    @Override
    public void onPreCreate(Flow flow) {
        System.out.println("onPreCreate");
    }

    @Override
    public void onCreated(Flow flow, Stream stream) {
        System.out.println("onCreated");

    }

    @Override
    @Transactional(rollbackFor = Throwable.class, propagation = Propagation.REQUIRES_NEW)
    public void onFinished(Flow flow) {
        System.out.println("onFinished");
        ServiceClassOrderCondition condition = new ServiceClassOrderCondition();
        condition.setFlowId(flow.getId());
        ServiceClassOrderVO orderVO = orderDao.find(condition);
        orderVO.setStatusId(OrderStatus.COMPLETE.statusId());
        baseOrderDao.put(orderVO);
        AbstractWechatConfiguration configuration = AbstractWechatConfiguration.getConfig(ServiceMarketConfiguration.class);
        WorkerVerticle.pool().getOne().eventBus().send(String.format("app[%s]-%s", configuration.getAppID(), "orderFinished"), orderVO.getId());
    }

    @Override
    public void onPreStart(Stream stream) {
        System.out.println("onPreStart");

    }

    @Override
    public void onStarted(Stream stream) {
        System.out.println("onStarted");

    }

    @Override
    public void onPreFinish(Stream stream) {
        System.out.println("onPreFinish");

    }

    @Override
    public void onFinished(Stream stream) {
        System.out.println("onFinished");

    }

    @Override
    public void onError(Flow flow, Stream stream, Throwable e) {
        System.out.println("onError");
        e.printStackTrace();

    }

    @Override
    public void onFlowStatus(Flow flow) {
        System.out.println("onFlowStatus");

    }

    @Override
    @Transactional(rollbackFor = Throwable.class, propagation = Propagation.REQUIRES_NEW)
    public void onStreamStatus(Flow flow, Stream stream) {
        if (stream.getShowId() == null && stream.getNodeId() != SIMPLE_NODE_ID) {
            return;
        }
        System.out.println("onStreamStatus");
        ServiceClassWorkOrderCondition workOrderCondition;
        ServiceClassWorkOrderVO workOrderVO;
        switch (StreamStatus.getInstance(stream.getStatusId())) {
            case SKIP:
                workOrderCondition = new ServiceClassWorkOrderCondition();
                workOrderCondition.setStreamId(stream.getId());
                workOrderCondition.setId(workOrderDao.find(workOrderCondition).getId());
                workOrderCondition.setUpdate("skip");
                workOrderDao.update(workOrderCondition);
                break;
            case WAIT:
            case READY:
                workOrderCondition = new ServiceClassWorkOrderCondition();
                workOrderCondition.setStreamId(stream.getId());
                workOrderVO = workOrderDao.find(workOrderCondition);
                if (workOrderVO != null) break;

                workOrderVO = new ServiceClassWorkOrderVO();
                ServiceClassOrderVO orderVO = orderDao.find((ServiceClassOrderCondition) new ServiceClassOrderCondition().setFlowId(flow.getId()));
                workOrderVO.setOrderId(orderVO.getId());
                JSONObject valueJson = stream.getValue() != null ? stream.getValue().jsonValue() : stream.getValueId() != null ? flowService.getValue(stream.getValueId()).jsonValue() : new JSONObject();
                if (stream.getNodeId() == SIMPLE_NODE_ID)
                    workOrderVO.setStaffId(valueJson.getLong("staffId"));
                workOrderVO.setStatusId(WorkOrderStatus.READY.statusId());
                workOrderVO.setStreamId(stream.getId());
                long functionId = valueJson.getLongValue("id");
                if (functionId < 0) {
                    OperationType operationType = OperationType.getInstance(functionId);
                    switch (operationType) {
                        case SERVICING:
                            workOrderVO.setDeadLineTime(Timestamp.valueOf(this.computeWorkTime(orderVO.getBookTime().toLocalDateTime(), orderVO.getWorkLoadUnit(), orderVO.getTotalWorkLoad())));
                            workOrderVO.setOperation(operationType.name());
                            break;
                        case SIGN_UP:
                            workOrderVO.setDeadLineTime(Timestamp.valueOf(orderVO.getBookTime().toLocalDateTime()));
                            workOrderVO.setOperation(operationType.name());
                            break;
                        case DISPATCH:
                            workOrderVO.setDeadLineTime(Timestamp.valueOf(this.computeWorkTime(orderVO.getCreateTime().toLocalDateTime(), 0L, 2)));
                            workOrderVO.setOperation(operationType.name());
                            break;
                        case CUST_COMFIRM:
                            workOrderVO.setOperation(operationType.name());
                            break;
                        default:
                            break;
                    }
                }
                workOrderDao.add(workOrderVO);
                break;
            case COMPLETE:
//                workOrderCondition = new ServiceClassWorkOrderCondition();
                workOrderCondition = new ServiceClassWorkOrderCondition();
                workOrderCondition.setStreamId(stream.getId());
                workOrderCondition.setId(workOrderDao.find(workOrderCondition).getId());
                workOrderCondition.setUpdate("finish");
                workOrderDao.update(workOrderCondition);
                break;
            default:
                break;
        }
    }

    @Override
    public List<ServiceContent> getServices(OrderVO orderVO) {
        return List.of(ServiceContent.SIMPLE_CLEAM);
    }

    @Override
    public List<DetailVO> orderDetail(Long id) {
        ServiceClassOrderVO orderVO = orderDao.get(id);
        List<DetailVO> list = new LinkedList<>();
        list.add(new DetailVO("上门时间", orderVO.getBookTime().toLocalDateTime().format(DateTimeFormatter.ofPattern("yyyy年MM月dd日 HH点"))));
        switch (WorkLoadUnit.getInstance(orderVO.getWorkLoadUnit())) {
            case HOURS:
            case TIMES:
                list.add(new DetailVO("服务时长", String.format("%d%s", orderVO.getTotalWorkLoad(), WorkLoadUnit.HOURS.getDesc())));
                break;
            case SQUARE:
                list.add(new DetailVO("服务范围", String.format("%d%s", orderVO.getTotalWorkLoad(), WorkLoadUnit.HOURS.getDesc())));
                break;
        }

        AddressVO addressVO = addressDao.get(orderVO.getAddressId());
        list.add(new DetailVO("服务区域", addressVO.getRegion()));
        list.add(new DetailVO("需要人数", String.format("%s%s", orderVO.getWorkerNum(), "人")));
        list.add(new DetailVO("备注信息", orderVO.getRemark()));
        list.add(new DetailVO("支付方式", "微信"));
        if (!StringUtils.isEmpty(orderVO.getAddedValue()))
            list.add(new DetailVO("增值服务", orderVO.getAddedValue()));

        List<StaffTaskVO> staffTaskVOS = staffTaskDao.list(new StaffCondition().setOrderId(orderVO.getId()));
        if (!CollectionUtils.isEmpty(staffTaskVOS)) {
            staffTaskVOS.stream()
                    .map(staffTaskVO -> appUserDao.get(staffTaskVO.getStaffId()))
                    .map(appUserVO -> new DetailVO("服务人员", appUserVO.getName()))
                    .forEach(detailVO -> list.add(detailVO));
        }
        return list;
    }

    @Override
    public void dismiss(Long orderId) {
        Flow flow = new Flow();
        flow.setId(orderDao.get(orderId).getFlowId());
        orderDao.delete(orderId);
        baseOrderDao.delete(orderId);
        ServiceClassWorkOrderCondition workOrderCondition = new ServiceClassWorkOrderCondition();
        workOrderCondition.setOrderId(orderId);
        workOrderDao.clear(workOrderCondition);
        staffTaskDao.clear(new StaffCondition().setOrderId(orderId));
        flowService.dismiss(flow);
    }


    @Override
    public PayBillResponseMesseage payCallBack(PayBillRequsetMesseage payBillRequsetMesseage) {
        OrderVO orderVO;
        {
            OrderCondition<OrderVO> condition = new OrderCondition<>();
            condition.setCode(payBillRequsetMesseage.getOutTradeNo());
            AppUserVO userVO = appUserDao.find(new UserCondition().setOpenId(payBillRequsetMesseage.getOpenId()));
            condition.setCustId(userVO.getId());
            orderVO = baseOrderDao.find(condition);
        }

        PayBillResponseMesseage payBillResponseMesseage = new PayBillResponseMesseage();
        if (orderVO != null) {
            orderPrePayDaoLog.put(new OrderPayLogVO().setOrderId(orderVO.getId()).setTransactionId(payBillRequsetMesseage.getTransactionId()));
            payBillResponseMesseage.ok();
            this.start((ServiceClassOrderCondition) new ServiceClassOrderCondition().setId(orderVO.getId()));
        } else {
            payBillResponseMesseage.fail("定单不存在");
        }
        return payBillResponseMesseage;
    }

    @Override
    public PrePayRequsetMesseage payRequset(WeChatPayOrder weChatPayOrder) {
        OrderVO orderVO = baseOrderDao.get(Long.parseLong(weChatPayOrder.getId()));
        AbstractWechatConfiguration customWeChatAppConfiguration = AbstractWechatConfiguration.getConfig(ServiceMarketConfiguration.class);
        PrePayRequsetMesseage messeage = new PrePayRequsetMesseage()
                .setBody(String.format("%s-服务费用结算:\n定单编号：%s\n金额%s", customWeChatAppConfiguration.getAppName(), orderVO.getCode(), OrderUtils.priceFormat(orderVO.getPrice().intValue(), ' ', true, false)))
                .setOutTradeNo(orderVO.getCode())
                .setTotalFee(orderVO.getPrice().intValue())
                .setOpenid(weChatPayOrder.getUserId());
        return messeage;
    }

    @Override
    public RefundReplyMesseage refundCallBack(RefundResultMesseage refundResultMesseage) {
        RefundReplyMesseage messeage = new RefundReplyMesseage();
        OrderVO orderVO;
        OrderCondition<OrderVO> condition = new OrderCondition<>();
        condition.setCode(refundResultMesseage.getOutTradeNo());
        orderVO = baseOrderDao.find(condition);
        orderVO.setStatusId(OrderStatus.COMPLETE.statusId());
        baseOrderDao.put(orderVO);
        messeage.ok();
        return messeage;
    }

    @Override
    public RefundRequestMesseage refundRequset(WeChatRefundOrder weChatPayOrder) {
        OrderVO orderVO = baseOrderDao.get(Long.parseLong(weChatPayOrder.getId()));
        List<ServiceClassWorkOrderVO> doing = (List) this.getWorkOrders((ServiceClassWorkOrderCondition) new ServiceClassWorkOrderCondition().setQuery("all").setOrderId(orderVO.getId()));
        if (CollectionUtils.isEmpty(doing))
            throw new RuntimeException("无效的操作");
        WorkOrderVO workOrderVO = doing.stream()
                .sorted(Comparator.comparing(WorkOrderVO::getCreateTime))
                .filter(workOrderVO1 -> {
                    if (workOrderVO1.getStream().getNodeId() != SIMPLE_NODE_ID)
                        return false;
                    if (workOrderVO1.getStream().getValueId() == null)
                        return false;
                    Stream stream = workOrderVO1.getStream();
                    long functionId = ((stream.getValue() != null) ? stream.getValue() : flowService.getValue(stream.getValueId())).jsonValue().getLongValue("id");
                    if (functionId >= 0) return false;
                    OperationType operationType = OperationType.getInstance(functionId);
                    switch (operationType) {
                        case SIGN_UP:
                        case SERVICING:
                        case DISPATCH:
                        case CUST_COMFIRM:
                            return true;
                        default:
                            return false;
                    }
                })
                .findFirst().orElse(null);
        if (workOrderVO == null) {
            throw new RuntimeException("改环节不能退款");
        }
        switch (OperationType.getInstance(workOrderVO.getStream().getValue().
                jsonValue().getLongValue("id"))) {
            case SERVICING:
            case CUST_COMFIRM:
                throw new RuntimeException("该环节已不能进行此操作");
            case DISPATCH:
            case SIGN_UP:
            default:
                break;
        }
        OrderPayLogVO payLogVO = orderPrePayDaoLog.get(orderVO.getId());
        return new RefundRequestMesseage().setOutTradeNo(orderVO.getCode()).
                setOutRefundNo(orderVO.getCode()).setRefundFee(orderVO.getPrice().intValue()).
                setTotalFee(orderVO.getPrice().intValue()).setTransactionId(payLogVO.getTransactionId());
    }

    @Override
    public void overtime(OverTimeOrderVO overTimeOrder) {
        ServiceClassOrderVO orderVO = orderDao.get(overTimeOrder.getRelaOrderId());

        //orderVO.setWorkLoad(orderVO.getWorkLoad() + overTimeOrder.getWorkLoad());
        orderVO.setTotalWorkLoad(orderVO.getTotalWorkLoad() + overTimeOrder.getWorkLoad());
        orderDao.put(orderVO);

        List<ServiceClassWorkOrderVO> allWorkOrders = (List) this.getWorkOrders((ServiceClassWorkOrderCondition) new ServiceClassWorkOrderCondition().setQuery("all").setOrderId(orderVO.getId()));
        allWorkOrders = allWorkOrders.stream()
                .sorted(Comparator.comparing(WorkOrderVO::getCreateTime))
                .filter(workOrderVO1 -> {
                    if (workOrderVO1.getStream().getNodeId() != SIMPLE_NODE_ID)
                        return false;
                    if (workOrderVO1.getStream().getValueId() == null)
                        return false;
                    Stream stream = workOrderVO1.getStream();
                    long functionId = ((stream.getValue() != null) ? stream.getValue() : flowService.getValue(stream.getValueId())).jsonValue().getLongValue("id");
                    if (functionId >= 0) return false;
                    OperationType operationType = OperationType.getInstance(functionId);
                    switch (operationType) {
                        case SERVICING:
                            return true;
                        case CUST_COMFIRM:
                        case SIGN_UP:
                        case DISPATCH:
                        default:
                            return false;
                    }
                }).collect(Collectors.toList());
        if (!CollectionUtils.isEmpty(allWorkOrders)) {
            allWorkOrders.stream().forEach(workOrder -> {
                LocalDateTime time = workOrder.getDeadLineTime().toLocalDateTime();
                time = computeWorkTime(time, orderVO.getWorkLoadUnit(), overTimeOrder.getWorkLoad());
                workOrder.setDeadLineTime(Timestamp.valueOf(time));
            });
        }
    }

    LocalDateTime computeWorkTime(LocalDateTime time, long workLoadUnit, Integer workLoad) {
        switch (WorkLoadUnit.getInstance(workLoadUnit)) {
            case HOURS:
                return time.plusHours(workLoad);
            case TIMES:
                return time.plusHours(workLoad * 8);
            default:
                return time;
        }
    }

    @Override
    public Integer rebate(OrderVO orderVO) {
        ServiceClassOrderVO order = orderDao.get(orderVO.getId());
        ItemTypeVO itemTypeVO = itemTypeDao.get(order.getItemTypeId());
        AppConfig config = appConfigDao.find(new AppConfig().setName(String.format("%s_%d", IRebate.CONFIG_GROUP, Long.valueOf(itemTypeVO.getItemId()))).setGroup(IRebate.CONFIG_GROUP));
        if (config == null)
            return 0;
        StringBuilder sb = new StringBuilder(order.getTotalPrice() * Integer.valueOf(config.getValue()));
        return Integer.valueOf(sb.substring(0, sb.length() - 2));
    }
}
