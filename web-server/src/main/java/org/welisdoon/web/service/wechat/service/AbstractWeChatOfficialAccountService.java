package org.welisdoon.web.service.wechat.service;

import io.vertx.core.CompositeFuture;
import io.vertx.core.Future;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.impl.NoStackTraceThrowable;
import io.vertx.ext.web.RoutingContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.welisdoon.common.LogUtils;
import org.welisdoon.common.ObjectUtils;
import org.welisdoon.web.common.ApplicationContextProvider;
import org.welisdoon.web.common.config.AbstractWechatOfficialAccountConfiguration;
import org.welisdoon.web.entity.wechat.messeage.MesseageTypeValue;
import org.welisdoon.web.entity.wechat.messeage.handler.MesseageHandler;
import org.welisdoon.web.entity.wechat.messeage.request.*;
import org.welisdoon.web.entity.wechat.messeage.response.NoReplyMesseage;
import org.welisdoon.web.entity.wechat.messeage.response.ResponseMesseage;

import javax.annotation.PostConstruct;
import java.util.*;
import java.util.stream.Collectors;

public abstract class AbstractWeChatOfficialAccountService<T extends AbstractWechatOfficialAccountConfiguration> {

    protected Class<T> configType() {
//        System.out.println(this.getClass().getGenericSuperclass());
        return (Class<T>) ObjectUtils.getGenericTypes(this.getClass(), AbstractWeChatOfficialAccountService.class, 0);
    }

    protected Logger logger = LoggerFactory.getLogger(this.getClass());

    protected static final Map<String, Class<? extends RequestMesseage>> mapper;

    final int part = 3;

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
        List<MesseageHandler> matcheds = Arrays
                .stream(messeageHandlers)
                .filter(entry -> {
                    try {
                        return mapper.get(message.getMsgType()).equals(ApplicationContextProvider.getRawType(entry, MesseageHandler.class)[0]);
                    } catch (Throwable e) {
                        logger.error(e.getMessage(), e);
                        return false;
                    }
                }).collect(Collectors.toList());

        List<MesseageHandler>[] tasks = this.splitTask(matcheds);


        return this.matched(this.splitTask(matcheds), 0, message).compose(entry -> matcheds.get(entry.getKey()).handle(message));



        /*Future<CompositeFuture> compositeFuture = CompositeFuture.all(matcheds.stream().map(messeageHandler -> messeageHandler.matched(message)).collect(Collectors.toList()));
        return compositeFuture.compose(compositeFuture1 -> {
            List<Boolean> list = compositeFuture1.list();
            for (int i = 0; i < list.size(); i++) {
                if (list.get(i)) {
                    return matcheds.get(i).handle(message);
                }
            }
            return Future.succeededFuture(new NoReplyMesseage(message));
        });*/
    }

    protected Future<Map.Entry<Integer, Object>> matched(List<MesseageHandler>[] tasks, int index, RequestMesseage message) {
        if (tasks.length >= index)
            return Future.failedFuture("解析失败");
        return CompositeFuture.all(tasks[index].stream().map(messeageHandler -> messeageHandler.matched(message)).collect(Collectors.toList()))
                .compose(compositeFuture -> {
                    List<Boolean> list = compositeFuture.list();
                    for (int i = 0; i < list.size(); i++) {
                        if (list.get(i)) {
                            return Future.succeededFuture(Map.entry(i + (this.part * index), null));
                        }
                    }
                    return matched(tasks, index + 1, message);
                });
    }

    protected List<MesseageHandler>[] splitTask(List<MesseageHandler> matcheds) {
        int size = matcheds.size(), index = 0;
        int start = 0, end = this.part;
        List<MesseageHandler>[] lists = new List[(int) Math.ceil((1.0F * size) / 3)];
        while (Math.min(end, size) < size) {
            lists[index++] = matcheds.subList(start, end);
            start = end;
            end = Math.min(end + 3, size);
        }
        return lists;
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

    public static AbstractWeChatOfficialAccountService findService(Class<? extends AbstractWechatOfficialAccountConfiguration> configClass) {
        return ApplicationContextProvider.getApplicationContext()
                .getBeansOfType(AbstractWeChatOfficialAccountService.class)
                .entrySet()
                .stream()
                .map(Map.Entry::getValue)
                .filter(service -> service.configType() == ApplicationContextProvider.getRealClass(configClass))
                .findFirst().get();
    }
}
