package org.welisdoon.webserver.entity.wechat.custom;

public class CustomMpNewsMessage extends CustomMessage {
    MpNews mpnews;

    public CustomMpNewsMessage() {
        this.msgtype = MsgType.MP_NEWS.value;
    }

    public static class MpNews {
        String media_id;

        public String getMedia_id() {
            return media_id;
        }

        public void setMedia_id(String media_id) {
            this.media_id = media_id;
        }
    }

    public MpNews getMpnews() {
        return mpnews;
    }

    public CustomMpNewsMessage setMpnews(MpNews mpnews) {
        this.mpnews = mpnews;
        return this;
    }

    public CustomMpNewsMessage mpnews(String mediaId) {
        if (this.mpnews == null) {
            this.mpnews = new MpNews();
        }
        this.mpnews.media_id = mediaId;
        return this;
    }
}
