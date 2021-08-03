package org.welisdoon.web.service.wechat.service;

import io.vertx.core.Future;
import org.welisdoon.web.entity.wechat.messeage.MesseageTypeValue;
import org.welisdoon.web.entity.wechat.messeage.request.*;
import org.welisdoon.web.entity.wechat.messeage.response.ArticleMesseage;
import org.welisdoon.web.entity.wechat.messeage.response.ResponseMesseage;

public abstract class AbstractVertxWeChatService {

    public Future<ResponseMesseage> receive(RequestMesseage message) {
        switch (message.getMsgType()) {
            case MesseageTypeValue.TEXT:
                return this.textProcess((TextMesseage) message);
            case MesseageTypeValue.IMAGE:
                return  this.imageProcess((ImageMesseage) message);

            case MesseageTypeValue.VIDEO:
                return  this.videoProcess((VideoMesseage) message);

            case MesseageTypeValue.VOICE:
                return  this.voiceProcess((VoiceMesseage) message);

            case MesseageTypeValue.LINK:
                return  this.linkProcess((LinkMesseage) message);

            case MesseageTypeValue.LOCATION:
                return  this.locationProcess(((LocationMesseage) message));

            case MesseageTypeValue.SHORT_VIDEO:
                return  this.shortVideoProcess(((ShortVideoMesseage) message));

            default:
                ResponseMesseage msg= new org.welisdoon.web.entity.wechat.messeage.response.TextMesseage();
                ((org.welisdoon.web.entity.wechat.messeage.response.TextMesseage) msg).setContent("你发的是什么鬼啊") ;
                return Future.succeededFuture(msg);
        }
    }

    public Future<ResponseMesseage> textProcess(TextMesseage msg) {
        // TODO Auto-generated method stub
        ResponseMesseage to = new org.welisdoon.web.entity.wechat.messeage.response.TextMesseage(msg);
        ((org.welisdoon.web.entity.wechat.messeage.response.TextMesseage) to).setContent(msg.getContent());
        return Future.succeededFuture(to);
    }

    public Future<ResponseMesseage> imageProcess(ImageMesseage msg) {
        ResponseMesseage to = new org.welisdoon.web.entity.wechat.messeage.response.ImageMesseage(msg);
        ((org.welisdoon.web.entity.wechat.messeage.response.ImageMesseage) to).setMediaId(msg.getMediaId());
        return Future.succeededFuture(to);
    }

    private Future<ResponseMesseage> linkProcess(LinkMesseage msg) {
        ResponseMesseage to = new ArticleMesseage(msg);
        ((ArticleMesseage) to).setArticleInfo(msg.getTitle(), msg.getDescription(), "", msg.getUrl());
        return Future.succeededFuture(to);
    }

    public Future<ResponseMesseage> locationProcess(LocationMesseage msg) {
        ResponseMesseage to = new org.welisdoon.web.entity.wechat.messeage.response.TextMesseage(msg);
        ((org.welisdoon.web.entity.wechat.messeage.response.TextMesseage) to).setContent(msg.getLocation_X() + ":" + msg.getLocation_Y());
        return Future.succeededFuture(to);
    }

    public Future<ResponseMesseage> shortVideoProcess(ShortVideoMesseage msg) {
        ResponseMesseage to = new org.welisdoon.web.entity.wechat.messeage.response.VideoMesseage(msg);
        ((org.welisdoon.web.entity.wechat.messeage.response.VideoMesseage) to).setVideoInfo(msg.getMediaId(), "", "");
        return Future.succeededFuture(to);
    }

    public Future<ResponseMesseage> videoProcess(VideoMesseage msg) {
        ResponseMesseage to = new org.welisdoon.web.entity.wechat.messeage.response.VideoMesseage(msg);
        ((org.welisdoon.web.entity.wechat.messeage.response.VideoMesseage) to).setVideoInfo(msg.getMediaId(), "", "");
        return Future.succeededFuture(to);
    }

    public Future<ResponseMesseage> voiceProcess(VoiceMesseage msg) {
        ResponseMesseage to = new org.welisdoon.web.entity.wechat.messeage.response.VoiceMesseage(msg);
        ((org.welisdoon.web.entity.wechat.messeage.response.VoiceMesseage) to).setMediaId(msg.getMediaId());
        return Future.succeededFuture(to);
    }


}
