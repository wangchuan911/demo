package org.welisdoon.webserver.entity.wechat.custom;

public abstract class CustomMessage {
    public static enum MsgType {
        TEXT("text"),
        IMAGE("image"),
        VOICE("voice"),
        MUSIC("music"),
        NEWS("news"),
        MP_NEWS("mpnews"),
        MSG_MENU("msgmenu"),
        WX_CARD("wxcard"),
        MINI_PROGRAM_PAGE("miniprogrampage");

        String value;

        MsgType(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }
    }

    String touser,
            msgtype;

    public String getTouser() {
        return touser;
    }

    public void setTouser(String touser) {
        this.touser = touser;
    }

    public String getMsgtype() {
        return msgtype;
    }

    public void setMsgtype(String msgtype) {
        this.msgtype = this.msgtype != null ? this.msgtype : msgtype;
    }
}
