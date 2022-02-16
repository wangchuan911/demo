package org.welisdoon.web.service.wechat.service;


import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.welisdoon.web.config.WeChatServiceConfiguration;

@Service
@ConditionalOnProperty(prefix = "wechat", name = "appID")
public class WeChatService extends AbstractWeChatService<WeChatServiceConfiguration> {

}
