package pers.welisdoon.webserver.service.common.impl;


import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import pers.welisdoon.webserver.common.JAXBUtils;
import pers.welisdoon.webserver.entity.wechat.messeage.request.RequestMesseageBody;
import pers.welisdoon.webserver.entity.wechat.messeage.response.ResponseMesseage;
import pers.welisdoon.webserver.service.WeChatService;
import pers.welisdoon.webserver.service.common.CommonAsynService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class CommonAsynServiceImpl implements CommonAsynService {
    @Autowired
    private WeChatService weChatService;

    private static final Logger logger = LoggerFactory.getLogger(CommonAsynServiceImpl.class);

    @Override
    public void wechatMsgReceive(String message, Handler<AsyncResult<String>> resultHandler) {
        Future future = Future.future();
        try {
            future.setHandler(resultHandler);
            //报文转对象
            RequestMesseageBody requestMesseageBody = JAXBUtils.fromXML(message, RequestMesseageBody.class);
            //处理数据
            ResponseMesseage responseMesseage = weChatService.receive(requestMesseageBody);
            //报文转对象;
            future.complete(JAXBUtils.toXML(responseMesseage));
        } catch (Exception e) {
            future.fail(e);
        }
    }
}
