package com.hubidaauto.servmarket.module.order.service;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.dynamic.datasource.annotation.DS;
import com.hubidaauto.servmarket.module.flow.enums.OperationType;
import com.hubidaauto.servmarket.module.flow.enums.OrderStatus;
import com.hubidaauto.servmarket.module.flow.enums.ServiceContent;
import com.hubidaauto.servmarket.module.flow.enums.WorkOrderStatus;
import com.hubidaauto.servmarket.module.goods.dao.ItemDao;
import com.hubidaauto.servmarket.module.goods.dao.ItemTypeDao;
import com.hubidaauto.servmarket.module.order.annotation.OrderClass;
import com.hubidaauto.servmarket.module.order.dao.BaseOrderDao;
import com.hubidaauto.servmarket.module.order.dao.ServiceClassOrderDao;
import com.hubidaauto.servmarket.module.order.entity.*;
import com.hubidaauto.servmarket.module.workorder.dao.ServiceClassWorkOrderDao;
import com.hubidaauto.servmarket.module.workorder.entity.ServiceClassWorkOrderCondition;
import com.hubidaauto.servmarket.module.workorder.entity.ServiceClassWorkOrderVO;
import com.hubidaauto.servmarket.module.workorder.entity.WorkOrderVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.welisdoon.flow.module.flow.entity.Flow;
import org.welisdoon.flow.module.flow.entity.Stream;
import org.welisdoon.flow.module.flow.entity.StreamStatus;
import org.welisdoon.flow.module.flow.intf.FlowEvent;

import java.util.List;

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
public class ServiceClassOrderService implements FlowEvent, IOrderService<ServiceClassOrderCondition, ServiceClassWorkOrderCondition> {
    static long TEMPLATE_ID = 1L;
    FlowProxyService flowService;
    ServiceClassOrderDao orderDao;
    BaseOrderDao baseOrderDao;
    ItemDao itemDao;
    ItemTypeDao itemTypeDao;
    ServiceClassWorkOrderDao workOrderDao;

    @Autowired
    public void init(ServiceClassWorkOrderDao workOrderDao, FlowProxyService flowService, ServiceClassOrderDao orderDao, ItemDao itemDao, ItemTypeDao itemTypeDao, BaseOrderDao baseOrderDao) {
        this.workOrderDao = workOrderDao;
        this.orderDao = orderDao;
        this.flowService = flowService;
        this.baseOrderDao = baseOrderDao;
        this.itemDao = itemDao;
        this.itemTypeDao = itemTypeDao;
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
        this.flowService.stream(workOrderVO.getStreamId());
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
    public void onFinished(Flow flow) {
        System.out.println("onFinished");


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
                workOrderVO = new ServiceClassWorkOrderVO();
                ServiceClassOrderVO orderVO = orderDao.find((ServiceClassOrderCondition) new ServiceClassOrderCondition().setFlowId(flow.getId()));
                workOrderVO.setOrderId(orderVO.getId());
                JSONObject valueJson = stream.getValue() != null ? stream.getValue().jsonValue() : stream.getValueId() != null ? flowService.getValue(stream.getValueId()).jsonValue() : new JSONObject();
                if (stream.getNodeId() == 6L)
                    workOrderVO.setStaffId(valueJson.getLong("staffId"));
                workOrderVO.setStatusId(WorkOrderStatus.READY.statusId());
                workOrderVO.setStreamId(stream.getId());
                long functionId = valueJson.getLongValue("id");
                if (functionId < 0) {
                    OperationType operationType = OperationType.getInstance(functionId);
                    switch (operationType) {
                        case SERVICING:
                        case SIGN_UP:
                        case DISPATCH:
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
        return List.of(ServiceContent.HOME_CLEAN, ServiceContent.HOME_EQP_CLEAN);
    }


}
