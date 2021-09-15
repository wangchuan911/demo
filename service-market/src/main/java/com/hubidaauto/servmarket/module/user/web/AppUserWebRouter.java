package com.hubidaauto.servmarket.module.user.web;

import com.alibaba.fastjson.JSONObject;
import com.hubidaauto.carservice.wxapp.core.config.CustomWeChatAppConfiguration;
import com.hubidaauto.servmarket.common.utils.JsonUtils;
import com.hubidaauto.servmarket.module.staff.dao.StaffDao;
import com.hubidaauto.servmarket.module.staff.entity.StaffCondition;
import com.hubidaauto.servmarket.module.user.dao.AddressDao;
import com.hubidaauto.servmarket.module.user.dao.AppUserDao;
import com.hubidaauto.servmarket.module.user.entity.AddressVO;
import com.hubidaauto.servmarket.module.user.entity.AppUserVO;
import com.hubidaauto.servmarket.module.user.entity.UserCondition;
import com.hubidaauto.servmarket.module.user.service.AppUserService;
import io.vertx.core.json.Json;
import io.vertx.ext.web.handler.BodyHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.welisdoon.web.common.config.AbstractWechatConfiguration;
import org.welisdoon.web.vertx.annotation.VertxConfiguration;
import org.welisdoon.web.vertx.annotation.VertxRoutePath;
import org.welisdoon.web.vertx.annotation.VertxRouter;
import org.welisdoon.web.vertx.enums.VertxRouteType;
import org.welisdoon.web.vertx.utils.RoutingContextChain;

/**
 * @author Septem
 */
@Component
@VertxConfiguration
@ConditionalOnProperty(prefix = "wechat-app-hubida", name = "appID")
@VertxRoutePath("{wechat-app-hubida.path.app}/user")
public class AppUserWebRouter {
    AppUserDao appUserDao;
    AddressDao addressDao;
    AppUserService appUserService;
    AbstractWechatConfiguration abstractWechatConfiguration;
    StaffDao staffDao;

    @Autowired
    public void setAppUserDao(AppUserDao appUserDao) {
        this.appUserDao = appUserDao;
    }

    @Autowired
    public void setAddressDao(AddressDao addressDao) {
        this.addressDao = addressDao;
    }

    @Autowired
    public void setAppUserService(AppUserService appUserService) {
        this.appUserService = appUserService;
    }

    @Autowired
    public void setAbstractWechatConfiguration(CustomWeChatAppConfiguration abstractWechatConfiguration) {
        this.abstractWechatConfiguration = abstractWechatConfiguration;
    }

    @Autowired
    public void setStaffDao(StaffDao staffDao) {
        this.staffDao = staffDao;
    }

    @VertxRouter(path = "/*", order = -1)
    public void all(RoutingContextChain chain) {
        chain.handler(BodyHandler.create());
    }


    @VertxRouter(path = "\\/(?<id>\\d+)",
            method = "GET",
            mode = VertxRouteType.PathRegex)
    public void getUser(RoutingContextChain chain) {
        chain.blockingHandler(routingContext -> {
            AppUserVO appUser = appUserDao.get(Long.parseLong(routingContext.pathParam("id")));
            if (appUser == null) {
                routingContext.response().setStatusCode(404).end("没有数据");
                return;
            }
            routingContext.end(Json.encodeToBuffer(appUser));
        });
    }

    @VertxRouter(path = "\\/list(?:\\/(?<page>\\d+))?",
            method = "POST",
            mode = VertxRouteType.PathRegex)
    public void listUser(RoutingContextChain chain) {
        chain.blockingHandler(routingContext -> {
            UserCondition userCondition = JsonUtils.jsonToObject(routingContext.getBodyAsString(), UserCondition.class, () -> null);
            if (userCondition == null) {
                routingContext.response().setStatusCode(404).end("没有数据");
                return;
            }
            String page = routingContext.pathParam("page");
            if (!StringUtils.isEmpty(userCondition))
                userCondition.page(Integer.parseInt(page));
            routingContext.end(Json.encodeToBuffer(appUserDao.list(userCondition)));
        });
    }

    @VertxRouter(path = "\\/addr\\/(?<id>\\d+)",
            method = "GET",
            mode = VertxRouteType.PathRegex)
    public void getAddr(RoutingContextChain chain) {
        chain.blockingHandler(routingContext -> {
            AddressVO appUser = addressDao.get(Long.parseLong(routingContext.pathParam("id")));
            routingContext.end(Json.encodeToBuffer(appUser));
        });
    }

    @VertxRouter(path = "/addr/list",
            method = "POST")
    public void listAddr(RoutingContextChain chain) {
        chain.blockingHandler(routingContext -> {
            UserCondition userCondition = JsonUtils.jsonToObject(routingContext.getBodyAsString(), UserCondition.class, () -> null);
            if (userCondition == null) {
                routingContext.response().setStatusCode(404).end("没有数据");
                return;
            }
            routingContext.end(Json.encodeToBuffer(addressDao.list(userCondition)));
        });
    }

    @VertxRouter(path = "/addr",
            method = "PUT")
    public void updateAddr(RoutingContextChain chain) {
        chain.blockingHandler(routingContext -> {
            AddressVO addressVO = JsonUtils.jsonToObject(routingContext.getBodyAsString(), AddressVO.class, () -> null);
            if (addressVO == null || addressDao.put(addressVO) == 0) {
                routingContext.response().setStatusCode(404).end("没有数据");
                return;
            }
            routingContext.end(Json.encodeToBuffer(addressVO));
        });
    }

    @VertxRouter(path = "/addr",
            method = "POST")
    public void addAddr(RoutingContextChain chain) {
        chain.blockingHandler(routingContext -> {
            AddressVO addressVO = JsonUtils.jsonToObject(routingContext.getBodyAsString(), AddressVO.class, () -> null);
            if (addressVO == null || addressDao.add(addressVO) == 0) {
                routingContext.response().setStatusCode(404).end("没有数据");
                return;
            }
            routingContext.end(Json.encodeToBuffer(addressVO));
        });
    }

    @VertxRouter(path = "/wx",
            method = "POST")
    public void wxLogin(RoutingContextChain chain) {
        chain.blockingHandler(routingContext -> {
            String code = routingContext.getBodyAsJson().getString("code");
            abstractWechatConfiguration
                    .getWeChatCode2session(code, appUserService)
                    .onSuccess(entries -> {
                        routingContext.end(entries.toBuffer());
                    })
                    .onFailure(throwable -> {
                        routingContext.fail(throwable);
                    });
        });
    }

    @VertxRouter(path = "\\/addr\\/def(?<id>\\d+)",
            mode = VertxRouteType.PathRegex,
            method = "POST")
    public void addrDefault(RoutingContextChain chain) {
        chain.blockingHandler(routingContext -> {
            AddressVO addressVO = addressDao.get(Long.parseLong(routingContext.pathParam("id")));
            AppUserVO userVO = appUserDao.get(addressVO.getUserId());
            if (appUserDao.put(userVO.setDefAddrId(addressVO.getId())) == 0) {
                routingContext.response().setStatusCode(404).end("没有数据");
                return;
            }
            routingContext.end("ok");
        });
    }

    @VertxRouter(path = "\\/addr\\/(?<id>\\d+)",
            mode = VertxRouteType.PathRegex,
            method = "DELETE")
    public void delAddr(RoutingContextChain chain) {
        chain.blockingHandler(routingContext -> {
            if (addressDao.delete(Long.parseLong(routingContext.pathParam("id"))) == 0) {
                routingContext.response().setStatusCode(404).end("没有数据");
                return;
            }
            routingContext.end("ok");
        });
    }

    @VertxRouter(path = "/workers",
            method = "POST")
    public void workers(RoutingContextChain chain) {
        chain.blockingHandler(routingContext -> {
            try {
                StaffCondition staffCondition = JSONObject.parseObject(routingContext.getBodyAsString(), StaffCondition.class);
                routingContext.end(JSONObject.toJSONString(staffDao.list(staffCondition)));
            } catch (Throwable e) {
                routingContext.response().setStatusCode(404).end(e.getMessage());
            }
        });
    }


    /*@VertxRouter(path = "/",
            method = "PUT")
    public void put(RoutingContextChain chain) {
        chain.blockingHandler(routingContext -> {
            routingContext.end(Json.encodeToBuffer(itemDao.put(routingContext.getBodyAsJson().mapTo(ItemVO.class))));
        });
    }*/

}
