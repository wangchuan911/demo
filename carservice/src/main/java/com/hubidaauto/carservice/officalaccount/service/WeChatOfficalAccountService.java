package com.hubidaauto.carservice.officalaccount.service;


import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.welisdoon.webserver.entity.wechat.messeage.request.TextMesseage;
import org.welisdoon.webserver.entity.wechat.messeage.response.ResponseMesseage;
import org.welisdoon.webserver.service.wechat.service.AbstractWeChatService;

@Service
@ConditionalOnProperty(prefix = "wechat-public-hubida", name = "appID")
public class WeChatOfficalAccountService extends AbstractWeChatService {
    public ResponseMesseage textProcess(TextMesseage msg) {
        // TODO Auto-generated method stub
        ResponseMesseage to = null;
        to = new org.welisdoon.webserver.entity.wechat.messeage.response.TextMesseage(msg);
        ((org.welisdoon.webserver.entity.wechat.messeage.response.TextMesseage) to).setContent("复读机：" + msg.getContent());
        return to;
    }
}
