package com.hubidaauto.servmarket.module.popularize.web;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.JSONPObject;
import com.hubidaauto.carservice.wxapp.core.config.CustomWeChatAppConfiguration;
import com.hubidaauto.servmarket.common.utils.JsonUtils;
import com.hubidaauto.servmarket.module.common.dao.ImageContentDao;
import com.hubidaauto.servmarket.module.common.entity.ImageContentVO;
import com.hubidaauto.servmarket.module.common.web.HtmlTemplateWebRouter;
import com.hubidaauto.servmarket.module.order.dao.BaseOrderDao;
import com.hubidaauto.servmarket.module.popularize.dao.InviteOrderDao;
import com.hubidaauto.servmarket.module.popularize.entity.InviteOrderCondition;
import com.hubidaauto.servmarket.module.popularize.entity.InviteOrderVO;
import com.hubidaauto.servmarket.module.staff.dao.StaffDao;
import com.hubidaauto.servmarket.module.staff.dao.StaffJobDao;
import com.hubidaauto.servmarket.module.staff.entity.StaffCondition;
import com.hubidaauto.servmarket.module.staff.entity.StaffJob;
import com.hubidaauto.servmarket.module.user.dao.AddressDao;
import com.hubidaauto.servmarket.module.user.dao.AppUserDao;
import com.hubidaauto.servmarket.module.user.entity.AddressVO;
import com.hubidaauto.servmarket.module.user.entity.AppUserVO;
import com.hubidaauto.servmarket.module.user.entity.UserCondition;
import com.hubidaauto.servmarket.module.user.service.AppUserService;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.handler.StaticHandler;
import io.vertx.ext.web.impl.Utils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.welisdoon.web.common.ApplicationContextProvider;
import org.welisdoon.web.common.config.AbstractWechatConfiguration;
import org.welisdoon.web.vertx.annotation.VertxConfiguration;
import org.welisdoon.web.vertx.annotation.VertxRoutePath;
import org.welisdoon.web.vertx.annotation.VertxRouter;
import org.welisdoon.web.vertx.enums.VertxRouteType;
import org.welisdoon.web.vertx.utils.RoutingContextChain;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author Septem
 */
@Component
@VertxConfiguration
@ConditionalOnProperty(prefix = "wechat-app-hubida", name = "appID")
@VertxRoutePath("{wechat-app-hubida.path.app}/invite")
public class InviteOrderRouter {

    InviteOrderDao inviteOrderDao;
    BaseOrderDao baseOrderDao;

    @Autowired
    public void setBaseOrderDao(BaseOrderDao baseOrderDao) {
        this.baseOrderDao = baseOrderDao;
    }

    @Autowired
    public void setInviteOrderDao(InviteOrderDao inviteOrderDao) {
        this.inviteOrderDao = inviteOrderDao;
    }

    @VertxRouter(path = "/*", order = -1)
    public void all(RoutingContextChain chain) {
        chain.handler(BodyHandler.create());
    }


    @VertxRouter(path = "\\/orders(?:\\/(?<page>\\d+))?",
            method = "POST",
            mode = VertxRouteType.PathRegex)
    public void listUser(RoutingContextChain chain) {
        chain.blockingHandler(routingContext -> {
            InviteOrderCondition condition = JsonUtils.jsonToObject(routingContext.getBodyAsString(), InviteOrderCondition.class, () -> null);
            if (condition == null) {
                routingContext.response().setStatusCode(404).end("没有数据");
                return;
            }
            String page = routingContext.pathParam("page");
            condition.page(Integer.parseInt(page));
            List<InviteOrderVO> inviteOrders = inviteOrderDao.list(condition);
            routingContext.end(JSON.toJSONString(inviteOrders));
        });
    }


}
