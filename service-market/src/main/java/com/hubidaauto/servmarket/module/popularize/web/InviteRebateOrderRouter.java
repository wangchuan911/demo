package com.hubidaauto.servmarket.module.popularize.web;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.hubidaauto.servmarket.module.message.entity.WorkOrderReadyEvent;
import com.hubidaauto.servmarket.module.order.dao.BaseOrderDao;
import com.hubidaauto.servmarket.module.order.entity.OrderVO;
import com.hubidaauto.servmarket.module.order.service.BaseOrderService;
import com.hubidaauto.servmarket.module.order.service.FlowProxyService;
import com.hubidaauto.servmarket.module.popularize.dao.InviteRebateCountDao;
import com.hubidaauto.servmarket.module.popularize.dao.InviteRebateOrderDao;
import com.hubidaauto.servmarket.module.popularize.entity.InviteRebateCountVO;
import com.hubidaauto.servmarket.module.popularize.entity.InviteRebateOrderCondition;
import com.hubidaauto.servmarket.module.popularize.entity.InviteRebateOrderVO;
import com.hubidaauto.servmarket.module.popularize.service.InviteRebateOrderService;
import com.hubidaauto.servmarket.module.staff.dao.StaffJobDao;
import com.hubidaauto.servmarket.module.staff.entity.StaffCondition;
import com.hubidaauto.servmarket.module.staff.entity.StaffJob;
import com.hubidaauto.servmarket.module.user.entity.UserCondition;
import com.hubidaauto.servmarket.module.workorder.dao.ServiceClassWorkOrderDao;
import com.hubidaauto.servmarket.module.workorder.entity.ServiceClassWorkOrderVO;
import com.hubidaauto.servmarket.weapp.ServiceMarketConfiguration;
import io.vertx.ext.web.handler.BodyHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.welisdoon.flow.module.flow.entity.Stream;
import org.welisdoon.web.common.ApplicationContextProvider;
import org.welisdoon.web.common.CommonConst;
import org.welisdoon.web.common.config.AbstractWechatConfiguration;
import org.welisdoon.web.entity.wechat.WeChatMarketTransferOrder;
import org.welisdoon.web.vertx.annotation.VertxConfiguration;
import org.welisdoon.web.vertx.annotation.VertxRoutePath;
import org.welisdoon.web.vertx.annotation.VertxRouter;
import org.welisdoon.web.vertx.enums.VertxRouteType;
import org.welisdoon.web.vertx.utils.RoutingContextChain;
import org.welisdoon.web.vertx.verticle.WorkerVerticle;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author Septem
 */
@Component
@VertxConfiguration
//@ConditionalOnProperty(prefix = "wechat-app-hubida", name = "appID")
@VertxRoutePath("{wechat-app-hubida.path.app}/invite")
public class InviteRebateOrderRouter {

    InviteRebateOrderDao inviteOrderDao;
    BaseOrderDao baseOrderDao;
    InviteRebateCountDao inviteRebateCountDao;
    AbstractWechatConfiguration wechatConfiguration;
    StaffJobDao staffJobDao;
    InviteRebateOrderService inviteRebateOrderService;

    @Autowired
    public void setInviteRebateOrderService(InviteRebateOrderService inviteRebateOrderService) {
        this.inviteRebateOrderService = inviteRebateOrderService;
    }

    @Autowired
    public void setStaffJobDao(StaffJobDao staffJobDao) {
        this.staffJobDao = staffJobDao;
    }

    @Autowired
    public void setBaseOrderDao(BaseOrderDao baseOrderDao) {
        this.baseOrderDao = baseOrderDao;
    }

    @Autowired
    public void setInviteOrderDao(InviteRebateOrderDao inviteOrderDao) {
        this.inviteOrderDao = inviteOrderDao;
    }

    @Autowired
    public void setInviteRebateCountDao(InviteRebateCountDao inviteRebateCountDao) {
        this.inviteRebateCountDao = inviteRebateCountDao;
    }

    @Autowired
    public void setWechatConfiguration(ServiceMarketConfiguration wechatConfiguration) {
        this.wechatConfiguration = wechatConfiguration;
    }

    @VertxRouter(path = "/*", order = -1)
    public void all(RoutingContextChain chain) {
        chain.handler(BodyHandler.create());
    }


    @VertxRouter(path = "\\/orders\\/(?<userId>\\d+)\\/(?<page>\\d+)",
            method = "GET",
            mode = VertxRouteType.PathRegex)
    public void listUser(RoutingContextChain chain) {
        chain.blockingHandler(routingContext -> {
            InviteRebateOrderCondition condition = new InviteRebateOrderCondition();
            condition.setInviteMan(Long.valueOf(routingContext.pathParam("userId")));
            String page = routingContext.pathParam("page");
            condition.page(Integer.parseInt(page));
            try {

                List<InviteRebateOrderVO> inviteOrders = inviteOrderDao.list(condition);
                routingContext.end(JSON.toJSONString(inviteOrders));
            } catch (Throwable e) {
                e.printStackTrace();
                routingContext.response().setStatusCode(404).end("[]");
            }

        });
    }

    @VertxRouter(path = "\\/order\\/(?<orderId>\\d+)",
            method = "DELETE",
            mode = VertxRouteType.PathRegex)
    public void order(RoutingContextChain chain) {
        chain.blockingHandler(routingContext -> {
            AbstractWechatConfiguration configuration = AbstractWechatConfiguration.getConfig(ServiceMarketConfiguration.class);
            WorkerVerticle.pool().getOne().eventBus().send(String.format("app[%s]-%s", configuration.getAppID(), "orderFinished"), Long.valueOf(routingContext.pathParam("orderId")));
            routingContext.end();
        });
    }

    @VertxRouter(path = "\\/workorder\\/(?<workorderId>\\d+)",
            method = "DELETE",
            mode = VertxRouteType.PathRegex)
    public void workorder(RoutingContextChain chain) {
        chain.blockingHandler(routingContext -> {
            try {

                AbstractWechatConfiguration configuration = AbstractWechatConfiguration.getConfig(ServiceMarketConfiguration.class);

                ServiceClassWorkOrderVO workOrderVO = ApplicationContextProvider.getBean(ServiceClassWorkOrderDao.class).get(Long.valueOf(routingContext.pathParam("workorderId")));
                Stream stream = ApplicationContextProvider.getBean(FlowProxyService.class).getStream(workOrderVO.getStreamId());
                workOrderVO.setStream(stream);
                if (stream == null || stream.getValueId() == null || stream.getValue() == null)
                    throw new RuntimeException();
                ;
                JSONObject valueJson = stream.getValue().jsonValue();
                if (valueJson == null || valueJson.size() == 0 || !valueJson.containsKey("tplt"))
                    throw new RuntimeException();
                ;
                valueJson = valueJson.getJSONObject("tplt");
                String url = valueJson.getString("url");
                if (StringUtils.isEmpty(url)) throw new RuntimeException();
                OrderVO orderVO = ApplicationContextProvider.getBean(BaseOrderService.class).get(workOrderVO.getOrderId());

                WorkerVerticle.pool().getOne().eventBus()
                        .send(String.format("app[%s]-%s", configuration.getAppID(), "workorder_ready"),
                                JSONObject.toJSONString(new WorkOrderReadyEvent(orderVO, workOrderVO, valueJson), SerializerFeature.WriteClassName));
                routingContext.end();
            } catch (Throwable e) {
                e.printStackTrace();
                routingContext.response().setStatusCode(500).end(e.getMessage());
            }
        });
    }

    @VertxRouter(path = "\\/count\\/(?<userId>\\d+)",
            method = "GET",
            mode = VertxRouteType.PathRegex)
    public void count(RoutingContextChain chain) {
        chain.blockingHandler(routingContext -> {
            routingContext.end(JSON.toJSONString(inviteRebateCountDao.get(Long.valueOf(routingContext.pathParam("userId")))));
        });
    }

    @VertxRouter(path = "\\/rebate\\/(?<userId>\\d+)",
            method = "GET",
            mode = VertxRouteType.PathRegex)
    public void rebate(RoutingContextChain chain) {
        chain.blockingHandler(routingContext -> {
            String path = routingContext.request().path();
            Long userId = Long.valueOf(routingContext.pathParam("userId"));
            this.wechatConfiguration
                    .wechatMarketTransfersRequest((WeChatMarketTransferOrder)
                            new WeChatMarketTransferOrder()
                                    .setUserId(userId.toString())
                                    .setPayClass(InviteRebateOrderService.class.getName()))
                    .onSuccess(jsonObject -> {
                        InviteRebateCountVO countVO = inviteRebateCountDao.get(userId);
                        countVO.setPaidRebate(countVO.getTotalRebate());
                        inviteRebateCountDao.put(countVO);
                        inviteOrderDao.update(new InviteRebateOrderCondition().setInviteMan(userId).setPayState(10051L));
                        routingContext.end("成功");
                    })
                    .onFailure(throwable -> {
                        routingContext.response().setStatusCode(500).end(throwable.getMessage());
                    });
        });
    }

    @VertxRouter(path = "\\/(?<del>(un)?)promote",
            mode = VertxRouteType.PathRegex,
            method = "PUT")
    public void promote(RoutingContextChain chain) {
        chain.blockingHandler(routingContext -> {
            boolean delete = "un".equals(routingContext.pathParam("del"));
            try {
                StaffCondition condition = JSONObject.parseObject(routingContext.getBodyAsString(), StaffCondition.class);
                condition.setRegionId(450000L);
                condition.setRoleId(3L);
                Set<Long> staffIds = Arrays.stream(condition.getStaffIds()).collect(Collectors.toSet());
                List<StaffJob> jobs = this.staffJobDao.list(condition);
                jobs.stream().forEach(staffJob -> {
                    if (delete)
                        staffJobDao.delete(staffJob.getId());
                    staffIds.remove(staffJob.getStaffId());
                });
                if (!delete)
                    staffIds.stream().forEach(staffId -> {
                        staffJobDao.add(new StaffJob().setName("推广人员").setRegionId(condition.getRegionId()).setRoleId(condition.getRoleId()).setStaffId(staffId));
                    });
                routingContext.response().end(JSONObject.toJSONString(jobs));
            } catch (Throwable e) {
                e.printStackTrace();
                routingContext.response().setStatusCode(500).end(e.getMessage());
            }
        });
    }

    @VertxRouter(path = "\\/join",
            mode = VertxRouteType.PathRegex,
            method = "PUT")
    public void promoteJoin(RoutingContextChain chain) {
        chain.blockingHandler(routingContext -> {
            try {
                inviteRebateOrderService.promoteJoin(JSONObject.parseObject(routingContext.getBodyAsString()).toJavaObject(UserCondition.class));
                routingContext.end("成功");
            } catch (Throwable e) {
                e.printStackTrace();
                routingContext.response().setStatusCode(500).end(e.getMessage());
            }
        });
    }

}
