package com.hubidaauto.carservice.officalaccount.service.handler;

import com.hubidaauto.carservice.officalaccount.config.CustomWeChaConfiguration;
import com.hubidaauto.carservice.officalaccount.dao.OfficalAccoutUserDao;
import com.hubidaauto.carservice.officalaccount.entity.UserVO;
import com.hubidaauto.carservice.officalaccount.service.CustomWeChatOfficalAccountService;
import com.hubidaauto.carservice.wxapp.core.config.CustomWeChatAppConfiguration;
import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.welisdoon.web.common.CommonConst;
import org.welisdoon.web.common.config.AbstractWechatConfiguration;
import org.welisdoon.web.entity.wechat.messeage.handler.MesseageHandler;
import org.welisdoon.web.entity.wechat.messeage.request.TextMesseage;

import java.util.Map;

/**
 * @Classname DefaultTextHandler
 * @Description TODO
 * @Author wang.zhidong
 * @Date 2022/2/15 15:13
 */
@Component
@MesseageHandler.Priority(config = CustomWeChatAppConfiguration.class, value = 0)
public class UpdateUserInfoTextHandler implements MesseageHandler<TextMesseage, org.welisdoon.web.entity.wechat.messeage.response.TextMesseage> {
    @Autowired
    OfficalAccoutUserDao userDao;

    @Override
    public Future<org.welisdoon.web.entity.wechat.messeage.response.TextMesseage> handle(TextMesseage msg) {
        final UserVO userVO = getOrAddUser(msg.getFromUserName());
        org.welisdoon.web.entity.wechat.messeage.response.TextMesseage to = new org.welisdoon.web.entity.wechat.messeage.response.TextMesseage(msg);
        Future<org.welisdoon.web.entity.wechat.messeage.response.TextMesseage> future;
        if (StringUtils.isEmpty(userVO.getUnionid())) {
            future = Future.future(textMesseagePromise -> {
                AbstractWechatConfiguration
                        .getConfig(CustomWeChaConfiguration.class)
                        .getWechatAsyncMeassger()
                        .get(CommonConst.WecharUrlKeys.USER_INFO
                                , Map.of("OPEN_ID", userVO.getId())
                        )
                        .onSuccess(bufferHttpResponse -> {
                            JsonObject jsonObject = bufferHttpResponse.bodyAsJsonObject();
                            userVO.setUnionid(jsonObject.getString("unionid"));
//										userVO.setName(jsonObject.getString("nickname"));
                            userDao.set(userVO.openData(false));
                            to.setContent("同步成功");
                            textMesseagePromise.complete(to);
                        })
                        .onFailure(throwable -> {
                            to.setContent(throwable.getMessage());
                            textMesseagePromise.complete(to);
                        });
            });
        } else {
            to.setContent("同步成功");
            future = Future.succeededFuture(to);
        }
        return future;
    }

    @Override
    public boolean matched(TextMesseage msg) {
        return "同步".equals(StringUtils.isEmpty(msg.getContent()) ? "" : msg.getContent());
    }

    UserVO getOrAddUser(String userId) {
        UserVO userVO = userDao.get(new UserVO().setId(userId));
        if (userVO == null) {
            userDao.add(userVO = new UserVO().setId(userId));
        }
        return userVO;
    }
}
