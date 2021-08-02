package org.welisdoon.web.service.wechat.service;

import org.welisdoon.web.entity.wechat.messeage.MesseageTypeValue;
import org.welisdoon.web.entity.wechat.messeage.request.*;
import org.welisdoon.web.entity.wechat.messeage.response.ArticleMesseage;
import org.welisdoon.web.entity.wechat.messeage.response.ResponseMesseage;

public abstract class AbstractWeChatService {

    public ResponseMesseage receive(RequestMesseage message) {
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
    }

    public ResponseMesseage textProcess(TextMesseage msg) {
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
    }


}
