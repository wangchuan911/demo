package com.hubidaauto.carservice.officalaccount.service;


import com.hubidaauto.carservice.officalaccount.config.CustomWeChaConfiguration;
import com.hubidaauto.carservice.officalaccount.dao.OfficalAccoutUserDao;
import com.hubidaauto.carservice.officalaccount.entity.UserVO;
import com.hubidaauto.carservice.wxapp.config.CustomWeChatAppConfiguration;
import io.vertx.core.json.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.welisdoon.webserver.common.CommonConst;
import org.welisdoon.webserver.common.WechatAsyncMeassger;
import org.welisdoon.webserver.common.config.AbstractWechatConfiguration;
import org.welisdoon.webserver.entity.wechat.custom.CustomMiniProgramPageMessage;
import org.welisdoon.webserver.entity.wechat.custom.CustomTextMessage;
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
        String text = StringUtils.isEmpty(msg.getContent()) ? "" : msg.getContent();
        switch (text) {
            case "同步": {
                final UserVO userVO = getOrAddUser(msg.getFromUserName());
                if (StringUtils.isEmpty(userVO.getUnionid())) {
                    AbstractWechatConfiguration
                            .getConfig(CustomWeChaConfiguration.class)
                            .getWechatAsyncMeassger()
                            .get(CommonConst.WecharUrlKeys.USER_INFO
                                    , Map.of("OPEN_ID", userVO.getId())
                                    , bufferHttpResponse -> {
                                        JsonObject jsonObject = bufferHttpResponse.bodyAsJsonObject();
                                        userVO.setUnionid(jsonObject.getString("unionid"));
                                        userVO.setName(jsonObject.getString("nickname"));
                                        userDao.set(userVO.openData(false));
                                    });
                    text = "同步成功";
                }
                to = new org.welisdoon.webserver.entity.wechat.messeage.response.TextMesseage(msg);
                ((org.welisdoon.webserver.entity.wechat.messeage.response.TextMesseage) to).setContent(text);
            }
            break;
            case "申请加入": {
                final UserVO userVO = getOrAddUser(msg.getFromUserName());
                if (StringUtils.isEmpty(userVO.getUnionid())) {
                    AbstractWechatConfiguration configuration = AbstractWechatConfiguration
                            .getConfig(CustomWeChatAppConfiguration.class);
                    AbstractWechatConfiguration
                            .getConfig(CustomWeChaConfiguration.class)
                            .getWechatAsyncMeassger()
                            .post(CommonConst.WecharUrlKeys.CUSTOM_SEND
                                    , new CustomTextMessage()
                                            .text(
                                                    String.format("<a  data-miniprogram-appid=\"%s\" data-miniprogram-path=\"%s\">加入我们</a>"
                                                            , configuration.getAppID()
                                                            , String.format("%s?code=joinUs&my=%s", configuration.getPath().getIndex(), userVO.getId())))
                                            .setTouser(userVO.getId()));
                }
            }
            break;
            default:
                break;
        }
        return to;
    }

    UserVO getOrAddUser(String userId) {
        UserVO userVO = userDao.get(new UserVO().setId(userId));
        if (userVO == null) {
            userDao.add(userVO = new UserVO().setId(userId));
        }
        return userVO;
    }
}
