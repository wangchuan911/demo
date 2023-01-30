package com.hubidaauto.carservice.officalaccount.service;


import com.hubidaauto.carservice.officalaccount.config.CustomWeChaConfiguration;
import com.hubidaauto.carservice.officalaccount.dao.OfficalAccoutUserDao;
import com.hubidaauto.carservice.officalaccount.entity.UserVO;
import com.hubidaauto.carservice.wxapp.core.config.CustomConst;
import com.hubidaauto.carservice.wxapp.core.config.CustomWeChatAppConfiguration;
import com.hubidaauto.carservice.wxapp.increment.entity.InviteCodeDto;
import com.hubidaauto.carservice.wxapp.increment.service.InviteCodeService;
import io.vertx.core.json.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.welisdoon.web.common.ApplicationContextProvider;
import org.welisdoon.web.common.CommonConst;
import org.welisdoon.web.common.config.AbstractWechatConfiguration;
import org.welisdoon.web.entity.wechat.custom.CustomTextMessage;
import org.welisdoon.web.entity.wechat.messeage.request.TextMesseage;
import org.welisdoon.web.entity.wechat.messeage.response.ResponseMesseage;
import org.welisdoon.web.service.wechat.service.AbstractWeChatOfficialAccountService;

import javax.annotation.PostConstruct;
import java.util.Map;

@Service
@ConditionalOnProperty(prefix = "wechat-public-hubida", name = "appID")
public class CustomWeChatOfficalAccountService extends AbstractWeChatOfficialAccountService<CustomWeChaConfiguration> {

    private static final Logger logger = LoggerFactory.getLogger(CustomWeChatOfficalAccountService.class);
    @Autowired
    OfficalAccoutUserDao userDao;

    InviteCodeService inviteCodeService;


    @PostConstruct
    void init() {
        inviteCodeService = ApplicationContextProvider.getBean(InviteCodeService.class);
    }

//    @Override
    @Deprecated
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
                            )
                            .onSuccess(bufferHttpResponse -> {
                                JsonObject jsonObject = bufferHttpResponse.bodyAsJsonObject();
                                userVO.setUnionid(jsonObject.getString("unionid"));
//										userVO.setName(jsonObject.getString("nickname"));
                                userDao.set(userVO.openData(false));
                            });
                    text = "同步成功";
                }
                to = new org.welisdoon.web.entity.wechat.messeage.response.TextMesseage(msg);
                ((org.welisdoon.web.entity.wechat.messeage.response.TextMesseage) to).setContent(text);
            }
            break;
            default: {
                String keyWord;
                if (text.indexOf(keyWord = "JOINUS@") == 0 && false) {
                    String inviteCode = text.substring(keyWord.length());
                    InviteCodeDto inviteCodeDto = (InviteCodeDto) inviteCodeService.handle(CustomConst.GET, Map.of("code", inviteCode, "type", CustomConst.INVITE_CODE.WORKER));
                    if (inviteCodeDto.getValid() > 0) {
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
                                                                    , String.format("%s?code=joinUs&my=%s&invite=%s",
                                                                            configuration.getPath().getOther().get("invite"),
                                                                            userVO.getId(),
                                                                            inviteCode)))
                                                    .setTouser(userVO.getId()));
                        }
                    }
                }
            }
            break;
        }
        return to;
    }

    public UserVO getOrAddUser(String userId) {
        UserVO userVO = userDao.get(new UserVO().setId(userId));
        if (userVO == null) {
            userDao.add(userVO = new UserVO().setId(userId));
        }
        return userVO;
    }

}
