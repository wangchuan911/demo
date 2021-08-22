package org.welisdoon.flow.module.flow.router;

import com.alibaba.fastjson.JSONObject;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.welisdoon.flow.module.flow.dao.FlowDao;
import org.welisdoon.flow.module.flow.dao.StreamDao;
import org.welisdoon.flow.module.flow.entity.Flow;
import org.welisdoon.flow.module.flow.entity.Stream;
import org.welisdoon.flow.module.flow.service.FlowService;
import org.welisdoon.web.vertx.annotation.VertxConfiguration;
import org.welisdoon.web.vertx.annotation.VertxRoutePath;
import org.welisdoon.web.vertx.annotation.VertxRouter;
import org.welisdoon.web.vertx.enums.VertxRouteType;
import org.welisdoon.web.vertx.utils.RoutingContextChain;

/**
 * @Classname FlowWebRouter
 * @Description TODO
 * @Author wang.zhidong
 * @Date 2021/8/21 16:58
 */
@Component
@ConditionalOnProperty(prefix = "demo", name = "router")
@VertxRoutePath("/demo/flow")
@VertxConfiguration
public class FlowWebRouter {
    FlowService flowService;
    StreamDao streamDao;
    FlowDao flowDao;

    @Autowired
    public void setStreamDao(StreamDao streamDao) {
        this.streamDao = streamDao;
    }

    @Autowired
    public void setFlowDao(FlowDao flowDao) {
        this.flowDao = flowDao;
    }

    @Autowired
    public void setFlowService(FlowService flowService) {
        this.flowService = flowService;
    }

    @VertxRouter(path = "*", method = "GET")
    public void all(RoutingContextChain context) {
        context.handler(BodyHandler.create());
    }

    @VertxRouter(path = "", method = "GET")
    public void flow(RoutingContextChain context) {
        context.handler(routingContext -> {
            Flow flow = new Flow();
            flow.setTemplateId(1L);
            flow.setFunctionId(1L);
            flowService.flow(flow);
            routingContext.end(JSONObject.toJSONString(flow));
        });
    }

    @VertxRouter(path = "\\/stream\\/(?<id>\\d+)", method = "GET", mode = VertxRouteType.PathRegex)
    public void stream(RoutingContextChain context) {
        context.handler(routingContext -> {
            try {
                Stream stream = streamDao.get(Long.parseLong(routingContext.pathParam("id")));
                stream = flowService.stream(stream);
                routingContext.end(JSONObject.toJSONString(stream));
            } catch (Throwable e) {
                e.printStackTrace();
                routingContext.fail(e.getCause());
            }
        });
    }

    @VertxRouter(path = "\\/stream\\/start\\/(?<id>\\d+)", method = "GET", mode = VertxRouteType.PathRegex)
    public void start(RoutingContextChain context) {

        context.handler(routingContext -> {
            try {
                Flow flow = flowDao.get(Long.parseLong(routingContext.pathParam("id")));
                flowService.start(flow);
                routingContext.end(JSONObject.toJSONString(flow));
            } catch (Throwable e) {
                e.printStackTrace();
                routingContext.fail(e.getCause());
            }
        });
    }
}
