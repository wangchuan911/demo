package org.welisdoon.web.service.wechat.service;

import io.vertx.core.Future;
import io.vertx.core.buffer.Buffer;
import io.vertx.ext.web.RoutingContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Assert;
import org.welisdoon.common.JAXBUtils;
import org.welisdoon.common.LogUtils;
import org.welisdoon.common.ObjectUtils;
import org.welisdoon.web.common.ApplicationContextProvider;
import org.welisdoon.web.common.config.AbstractWechatConfiguration;
import org.welisdoon.web.entity.wechat.messeage.MesseageTypeValue;
import org.welisdoon.web.entity.wechat.messeage.handler.MesseageHandler;
import org.welisdoon.web.entity.wechat.messeage.request.*;
import org.welisdoon.web.entity.wechat.messeage.response.NoReplyMesseage;
import org.welisdoon.web.entity.wechat.messeage.response.ResponseMesseage;

import javax.annotation.PostConstruct;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.*;
import java.util.stream.Collectors;

public abstract class AbstractWeChatService<T extends AbstractWechatConfiguration> {

    public Class<T> configType() {
//        System.out.println(this.getClass().getGenericSuperclass());
        return (Class<T>) ObjectUtils.getGenericTypes(this.getClass(), AbstractWeChatService.class, 0);
    }

    protected Logger logger = LoggerFactory.getLogger(this.getClass());

    protected static final Map<String, Class<? extends RequestMesseage>> mapper;

    static {
        mapper = new HashMap<>(8, 1.0f);
        mapper.put(MesseageTypeValue.TEXT, TextMesseage.class);
        mapper.put(MesseageTypeValue.IMAGE, ImageMesseage.class);
        mapper.put(MesseageTypeValue.VIDEO, VideoMesseage.class);
        mapper.put(MesseageTypeValue.VOICE, VoiceMesseage.class);
        mapper.put(MesseageTypeValue.LINK, LinkMesseage.class);
        mapper.put(MesseageTypeValue.LOCATION, LocationMesseage.class);
        mapper.put(MesseageTypeValue.SHORT_VIDEO, ShortVideoMesseage.class);
    }

    /*public ResponseMesseage receive(RequestMesseage message) {
        ResponseMesseage msgBody = null;
        String type = message.getMsgType();
        switch (type) {
            case MesseageTypeValue.TEXT:
                msgBody = this.textProcess((TextMesseage) message);
                break;
            case MesseageTypeValue.IMAGE:
                msgBody = this.imageProcess((ImageMesseage) message);
                break;
            case MesseageTypeValue.VIDEO:
                msgBody = this.videoProcess((VideoMesseage) message);
                break;
            case MesseageTypeValue.VOICE:
                msgBody = this.voiceProcess((VoiceMesseage) message);
                break;
            case MesseageTypeValue.LINK:
                msgBody = this.linkProcess((LinkMesseage) message);
                break;
            case MesseageTypeValue.LOCATION:
                msgBody = this.locationProcess(((LocationMesseage) message));
                break;
            case MesseageTypeValue.SHORT_VIDEO:
                msgBody = this.shortVideoProcess(((ShortVideoMesseage) message));
                break;
            default:
                msgBody = new org.welisdoon.web.entity.wechat.messeage.response.TextMesseage();
                ((org.welisdoon.web.entity.wechat.messeage.response.TextMesseage) msgBody).setContent("你发的是什么鬼啊");
        }
        return msgBody;
    }*/

    protected MesseageHandler[] messeageHandlers;

    @PostConstruct
    void catchHandler() {
        messeageHandlers = ApplicationContextProvider.getApplicationContext()
                .getBeansOfType(MesseageHandler.class)
                .entrySet()
                .stream()
                .map(Map.Entry::getValue)
                .filter(messeageHandler -> this.priority(messeageHandler) != null)
                .sorted(Comparator.comparingInt(messeageHandler -> this.priority(messeageHandler).value()))
                .toArray(MesseageHandler[]::new);
        logger.warn(LogUtils.format(String.format("wechat offical account [%s] handlers list:", this.getClass().getName()), Arrays.stream(messeageHandlers).map(MesseageHandler::toString).toArray(CharSequence[]::new)));
    }

    protected MesseageHandler.Priority priority(MesseageHandler messeageHandler) {
        Class<?> clz = ApplicationContextProvider
                .getRealClass(messeageHandler.getClass());
        MesseageHandler.Prioritys prioritys = clz
                .getAnnotation(MesseageHandler.Prioritys.class);
        return (Arrays
                .stream(prioritys != null ? prioritys
                        .value() : new MesseageHandler.Priority[]{clz.getAnnotation(MesseageHandler.Priority.class)})
                .filter(priority -> {
                    if (priority == null) return false;
                    return priority.config().isAssignableFrom(this.configType());
                })

                .findFirst()
                .orElse(null));
    }

    public Future<ResponseMesseage> receive(RequestMesseage message) {
        Future<ResponseMesseage> future = Arrays
                .stream(messeageHandlers)
                .filter(entry -> {
                    try {
                        return mapper.get(message.getMsgType()).equals(ApplicationContextProvider.getRawType(entry, MesseageHandler.class)[0])
                                && entry.matched(message);
                    } catch (Throwable e) {
                        logger.error(e.getMessage(), e);
                        return false;
                    }
                })
                .findFirst()
                .get()
                .handle(message);
        return future;
    }


    /*public ResponseMesseage textProcess(TextMesseage msg) {
        // TODO Auto-generated method stub
        ResponseMesseage to = null;
        to = new org.welisdoon.web.entity.wechat.messeage.response.TextMesseage(msg);
        ((org.welisdoon.web.entity.wechat.messeage.response.TextMesseage) to).setContent(msg.getContent());
        return to;
    }

    public ResponseMesseage imageProcess(ImageMesseage msg) {
        ResponseMesseage to = new org.welisdoon.web.entity.wechat.messeage.response.ImageMesseage(msg);
        ((org.welisdoon.web.entity.wechat.messeage.response.ImageMesseage) to).setMediaId(msg.getMediaId());
        return to;
    }

    private ResponseMesseage linkProcess(LinkMesseage msg) {
        ResponseMesseage to = new ArticleMesseage(msg);
        ((ArticleMesseage) to).setArticleInfo(msg.getTitle(), msg.getDescription(), "", msg.getUrl());
        return to;
    }

    public ResponseMesseage locationProcess(LocationMesseage msg) {
        ResponseMesseage to = new org.welisdoon.web.entity.wechat.messeage.response.TextMesseage(msg);
        ((org.welisdoon.web.entity.wechat.messeage.response.TextMesseage) to).setContent(msg.getLocation_X() + ":" + msg.getLocation_Y());
        return to;
    }

    public ResponseMesseage shortVideoProcess(ShortVideoMesseage msg) {
        ResponseMesseage to = new org.welisdoon.web.entity.wechat.messeage.response.VideoMesseage(msg);
        ((org.welisdoon.web.entity.wechat.messeage.response.VideoMesseage) to).setVideoInfo(msg.getMediaId(), "", "");
        return to;
    }

    public ResponseMesseage videoProcess(VideoMesseage msg) {
        ResponseMesseage to = new org.welisdoon.web.entity.wechat.messeage.response.VideoMesseage(msg);
        ((org.welisdoon.web.entity.wechat.messeage.response.VideoMesseage) to).setVideoInfo(msg.getMediaId(), "", "");
        return to;
    }

    public ResponseMesseage voiceProcess(VoiceMesseage msg) {
        ResponseMesseage to = new org.welisdoon.web.entity.wechat.messeage.response.VoiceMesseage(msg);
        ((org.welisdoon.web.entity.wechat.messeage.response.VoiceMesseage) to).setMediaId(msg.getMediaId());
        return to;
    }*/

    public void receive(RoutingContext routingContext) {
        try {
            this.receive(RequestMesseageBody.toInstance(routingContext.getBodyAsString()))
                    .onSuccess(responseMesseage -> {
                        try {
                            if (responseMesseage instanceof NoReplyMesseage) {
                                routingContext.response().end(Buffer.buffer(responseMesseage.toXMLString()));
                                return;
                            }
                            routingContext.response().setChunked(true);
                            routingContext.setBody(Buffer.buffer(responseMesseage.toXMLString()));
                            routingContext.next();
                        } catch (Throwable e) {
                            logger.error(e.getMessage(), e);
                            routingContext.response().setStatusCode(500).end();
                        }
                    })
                    .onFailure(e -> {
                        logger.error(e.getMessage(), e);
                        routingContext.response().setStatusCode(500).end();
                    });
        } catch (Throwable e) {
            logger.error(e.getMessage(), e);
            routingContext.response().setStatusCode(500).end();
        }
    }

    public static AbstractWeChatService findService(Class<? extends AbstractWechatConfiguration> configClass) {
        return ApplicationContextProvider.getApplicationContext()
                .getBeansOfType(AbstractWeChatService.class)
                .entrySet()
                .stream()
                .map(Map.Entry::getValue)
                .filter(service -> service.configType() == ApplicationContextProvider.getRealClass(configClass))
                .findFirst().get();
    }
}
