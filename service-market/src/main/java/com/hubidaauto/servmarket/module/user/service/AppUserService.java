package com.hubidaauto.servmarket.module.user.service;

import com.baomidou.dynamic.datasource.annotation.DS;
import com.hubidaauto.servmarket.module.user.dao.AppUserDao;
import com.hubidaauto.servmarket.module.user.entity.AppUserVO;
import com.hubidaauto.servmarket.module.user.entity.UserCondition;
import io.vertx.core.json.JsonObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.welisdoon.web.entity.wechat.WeChatUser;
import org.welisdoon.web.service.wechat.intf.IWechatUserHandler;

import java.util.List;

/**
 * @Classname AppUserService
 * @Description TODO
 * @Author wang.zhidong
 * @Date 2021/8/11 21:09
 */
@Service
@Transactional
@DS("shop")
public class AppUserService implements IWechatUserHandler {
    AppUserDao appUserDao;

    @Autowired
    public void setAppUserDao(AppUserDao appUserDao) {
        this.appUserDao = appUserDao;
    }

    @Override
    public AppUserVO login(WeChatUser weChatUser) {
        List<AppUserVO> users = appUserDao.list(new UserCondition().setOpenId(weChatUser.getOpenId()));
        AppUserVO userVO;
        if (CollectionUtils.isEmpty(users)) {
            userVO = new AppUserVO(weChatUser).setName("新用户");
            appUserDao.add(userVO);
        } else {
            userVO = users.get(0);
            userVO.setSession(weChatUser.getSessionKey());
            appUserDao.put(userVO);
        }
        return userVO;
    }
}
