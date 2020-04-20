package com.hubidaauto.carservice.officalaccount.service;


import com.hubidaauto.carservice.officalaccount.config.CustomWeChaConfiguration;
import com.hubidaauto.carservice.officalaccount.dao.OfficalAccoutUserDao;
import com.hubidaauto.carservice.officalaccount.entity.UserVO;
import io.vertx.core.json.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.welisdoon.webserver.common.CommonConst;
import org.welisdoon.webserver.common.WechatAsyncMeassger;
import org.welisdoon.webserver.entity.wechat.messeage.request.TextMesseage;
import org.welisdoon.webserver.entity.wechat.messeage.response.ResponseMesseage;
import org.welisdoon.webserver.entity.wechat.push.PublicTamplateMessage;
import org.welisdoon.webserver.service.wechat.service.AbstractWeChatService;

import javax.annotation.PostConstruct;
import java.util.Map;

@Service
@ConditionalOnProperty(prefix = "wechat-public-hubida", name = "appID")
public class CustomWeChatOfficalAccountService extends AbstractWeChatService {

    private static final Logger logger = LoggerFactory.getLogger(CustomWeChatOfficalAccountService.class);
    @Autowired
    OfficalAccoutUserDao userDao;


    @PostConstruct
    void init() {
    }

    public ResponseMesseage textProcess(TextMesseage msg) {
        // TODO Auto-generated method stub
        ResponseMesseage to = null;
        String text = msg.getContent();
        if ("同步".equals(msg.getContent())) {
            UserVO userVO = userDao.get(new UserVO().setId(msg.getFromUserName()));
            if (userVO == null) {
                userDao.add(userVO = new UserVO().setId(msg.getFromUserName()));
            }
            if (StringUtils.isEmpty(userVO.getUnionid())) {
                final UserVO userVO1 = userVO;
                text = "同步中";
                WechatAsyncMeassger
                        .get(CustomWeChaConfiguration.class)
                        .get(CommonConst.WecharUrlKeys.USER_INFO, Map.of("OPEN_ID", userVO.getId()))
                        .setSuccess(bufferHttpResponse -> {
                            JsonObject jsonObject = bufferHttpResponse.bodyAsJsonObject();
                            String unionid;
                            userVO1.setUnionid(unionid = jsonObject.getString("unionid"));
                            userVO1.setName(jsonObject.getString("nickname"));
                            userDao.set(userVO1);
                            logger.info(String.format("unionid:%s", unionid));
                        });
            }
        }
        to = new org.welisdoon.webserver.entity.wechat.messeage.response.TextMesseage(msg);
        ((org.welisdoon.webserver.entity.wechat.messeage.response.TextMesseage) to).setContent(text);
        return to;
    }
}
