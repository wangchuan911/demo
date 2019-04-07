package pers.welisdoon.webserver.service.wechat.impl;


import io.vertx.codegen.annotations.VertxGen;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.JsonObject;
import pers.welisdoon.webserver.common.JAXBUtils;
import pers.welisdoon.webserver.entity.wechat.messeage.MesseageTypeValue;
import pers.welisdoon.webserver.entity.wechat.messeage.request.RequestMesseage;
import pers.welisdoon.webserver.entity.wechat.messeage.request.RequestMesseageBody;
import pers.welisdoon.webserver.entity.wechat.messeage.response.ResponseMesseage;
import pers.welisdoon.webserver.service.WeChatService;
import pers.welisdoon.webserver.service.wechat.WeChatAsynService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import pers.welisdoon.webserver.common.JAXBUtils;
import pers.welisdoon.webserver.service.wechat.WeChatAsynService;

@Component
public class WeChatAsynServiceImpl implements WeChatAsynService {
    @Autowired
    private WeChatService weChatService;

    private static final Logger logger = LoggerFactory.getLogger(WeChatAsynServiceImpl.class);

    @Override
    public void receive(String message, Handler<AsyncResult<String>> resultHandler) {
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
