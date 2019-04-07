package pers.welisdoon.webserver.entity.wechat.messeage;

public enum MesseageTypeEnum {
    TEXT ( MesseageTypeValue.TEXT),
    IMAGE ( MesseageTypeValue.IMAGE),
    VOICE ( MesseageTypeValue.VOICE),
    VIDEO ( MesseageTypeValue.VIDEO),
    SHORT_VIDEO ( MesseageTypeValue.SHORT_VIDEO),
    LOCATION ( MesseageTypeValue.LOCATION),
    LINK ( MesseageTypeValue.LINK),
    MUSIC (MesseageTypeValue.MUSIC),
    ARTICLE ( MesseageTypeValue.ARTICLE),
    MSG_REPLY(MesseageTypeValue.MSG_REPLY),
    MSG_PARM_TIMESTAMP(MesseageTypeValue.MSG_PARM_TIMESTAMP),
    MSG_PARM_MSG_SIGNATURE(MesseageTypeValue.MSG_PARM_MSG_SIGNATURE),
    MSG_PARM_NONCE (MesseageTypeValue.MSG_PARM_NONCE),
    MSG_PARM_SIGNATURE(MesseageTypeValue.MSG_PARM_SIGNATURE);
    String value;
    MesseageTypeEnum(String value){
        this.value=value;
    }

    public String getValue() {
        return value;
    }
}
