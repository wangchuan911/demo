package com.hubidaauto.servmarket.module.user.service;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.dynamic.datasource.annotation.DS;
import com.hubidaauto.servmarket.module.user.dao.AppUserDao;
import com.hubidaauto.servmarket.module.user.entity.AppUserVO;
import com.hubidaauto.servmarket.module.user.entity.UserCondition;
import com.hubidaauto.servmarket.weapp.ServiceMarketConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.welisdoon.web.common.config.AbstractWechatConfiguration;
import org.welisdoon.web.entity.wechat.WeChatUser;
import org.welisdoon.web.service.wechat.intf.WechatLoginHandler;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @Classname AppUserService
 * @Description TODO
 * @Author wang.zhidong
 * @Date 2021/8/11 21:09
 */
@Service
@Transactional
@DS("shop")
public class AppUserService implements WechatLoginHandler<AppUserVO> {
    AppUserDao appUserDao;
    AbstractWechatConfiguration abstractWechatConfiguration;

    @Autowired
    public void setAppUserDao(AppUserDao appUserDao) {
        this.appUserDao = appUserDao;
    }

    @Autowired
    public void setAbstractWechatConfiguration(ServiceMarketConfiguration abstractWechatConfiguration) {
        this.abstractWechatConfiguration = abstractWechatConfiguration;
    }

    @Override
    public AppUserVO login(WeChatUser weChatUser) {
        List<AppUserVO> users = appUserDao.list(new UserCondition().setOpenId(weChatUser.getOpenId()));
        AppUserVO userVO;
        if (CollectionUtils.isEmpty(users)) {
            userVO = new AppUserVO(weChatUser).setName("新用户");
            appUserDao.add(userVO);
        } else {
            userVO = users.get(0).weChatUser(weChatUser);
            appUserDao.put(userVO);
        }
        return userVO;
    }

    public AppUserVO decrypt(UserCondition userCondition) throws Throwable {
        AppUserVO userVO = appUserDao.get(userCondition.getId());
        if (userCondition.getPhone() != null) {
            JSONObject jsonObject = JSONObject.parseObject(userCondition.getPhone().decrypt(userVO.getSession()));
            userVO.setPhone(jsonObject.getString("phoneNumber"));
            if (StringUtils.isEmpty(userVO.getPhone()))
                userVO.setPhone(jsonObject.getString("purePhoneNumber"));
        }
        if (userCondition.getUser() != null) {
            JSONObject jsonObject = JSONObject.parseObject(userCondition.getUser().decrypt(userVO.getSession()));
            userVO.setUnionId(jsonObject.getString("unionId"));
        }
        return userVO;
    }

    public void update(UserCondition userCondition) throws Throwable {
        AppUserVO userVO = this.decrypt(userCondition);
        if (userCondition.getData() != null) {
            AppUserVO userVO2 = userCondition.getData();
            JSONObject target = (JSONObject) JSONObject.toJSON(userVO), destinate = (JSONObject) JSONObject.toJSON(userVO2);
            for (String s : destinate.keySet()) {
                if (destinate.containsKey(s)) {
                    target.put(s, destinate.get(s));
                }
            }
            userVO = target.toJavaObject(AppUserVO.class);
        }
        appUserDao.put(userVO);
    }

    /* *//**
     * @author Septem
     *//*
    @Component
    @VertxConfiguration
    @ConditionalOnProperty(prefix = "wechat-app-hubida", name = "appID")
    @VertxRoutePath("{wechat-app-hubida.path.app}/user")
    public static class AppUserPage {
        AppUserDao appUserDao;
        AddressDao addressDao;
        AppUserService appUserService;
        AbstractWechatConfiguration abstractWechatConfiguration;

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

        @VertxRouter(path = "/*", order = -1)
        public void all(RoutingContextChain chain) {
            chain.handler(BodyHandler.create());
        }


        @VertxRouter(path = "\\/(?<id>\\d+)",
                method = "GET",
                mode = VertxRouteType.PathRegex)
        public void getUser(RoutingContextChain chain) {
            chain.handler(routingContext -> {
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
            chain.handler(routingContext -> {
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
            chain.handler(routingContext -> {
                AddressVO appUser = addressDao.get(Long.parseLong(routingContext.pathParam("id")));
                routingContext.end(Json.encodeToBuffer(appUser));
            });
        }

        @VertxRouter(path = "/addr/list",
                method = "POST")
        public void listAddr(RoutingContextChain chain) {
            chain.handler(routingContext -> {
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
            chain.handler(routingContext -> {
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
            chain.handler(routingContext -> {
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
            chain.handler(routingContext -> {
                String code = routingContext.getBodyAsJson().getString("code");
                this.abstractWechatConfiguration
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
            chain.handler(routingContext -> {
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
            chain.handler(routingContext -> {
                if (addressDao.delete(Long.parseLong(routingContext.pathParam("id"))) == 0) {
                    routingContext.response().setStatusCode(404).end("没有数据");
                    return;
                }
                routingContext.end("ok");
            });
        }


        *//*@VertxRouter(path = "/",
                method = "PUT")
        public void put(RoutingContextChain chain) {
            chain.handler(routingContext -> {
                routingContext.end(Json.encodeToBuffer(itemDao.put(routingContext.getBodyAsJson().mapTo(ItemVO.class))));
            });
        }*//*

    }*/
}
