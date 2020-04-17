package org.welisdoon.webserver.service.wechat.service;


import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

@Service
@ConditionalOnProperty(prefix = "wechat", name = "appID")
public class WeChatService extends AbstractWeChatService {

}
