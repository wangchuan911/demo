package org.welisdoon.web.entity.wechat.messeage.response;

import org.welisdoon.common.JAXBUtils;
import org.welisdoon.web.entity.wechat.messeage.request.RequestMesseage;


import javax.xml.bind.JAXBException;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.Date;

@XmlRootElement(name = "xml")
@XmlAccessorType(XmlAccessType.FIELD)
public abstract class ResponseMesseage implements JAXBUtils.XmlSerializable {//http://blog.csdn.net/howingtian/article/details/49026907
    @XmlElement(name = "ToUserName")
    protected String toUserName;
    @XmlElement(name = "FromUserName")
    protected String fromUserName;
    @XmlElement(name = "CreateTime")
    protected Long createTime;
    @XmlElement(name = "MsgType")
    protected String msgType;


    public String getToUserName() {
        return toUserName;
    }


    public String getFromUserName() {
        return fromUserName;
    }


    public Long getCreateTime() {
        return createTime;
    }


    public String getMsgType() {
        return msgType;
    }

    public void setToUserName(String toUserName) {
        toUserName = toUserName;
    }

    public void setFromUserName(String fromUserName) {
        fromUserName = fromUserName;
    }

    public void setCreateTime(Long createTime) {
        createTime = createTime;
    }

    public void setMsgType(String msgType) {
        this.msgType = msgType;
    }

    public ResponseMesseage() {
    }

    public ResponseMesseage(String toUserName, String fromUserName, Long createTime,
                            String msgType) {
        this.toUserName = toUserName;
        this.fromUserName = fromUserName;
        this.createTime = createTime;
        this.msgType = msgType;
    }

    public ResponseMesseage(String toUserName, String fromUserName, String msgType) {
        this(toUserName, fromUserName, System.currentTimeMillis(), msgType);
    }

    public ResponseMesseage(RequestMesseage msg, Long createTime, String msgType) {
        getMessageBaseInfo(msg);
        this.createTime = createTime;
        this.msgType = msgType;
    }

    public ResponseMesseage(RequestMesseage msg, String msgType) {
        this(msg, System.currentTimeMillis(), msgType);
    }

    @Override
    public String toString() {
        // TODO Auto-generated method stub
        return fromUserName + "," + toUserName + "," + msgType + ","
                + createTime;
    }

    public void getMessageBaseInfo(RequestMesseage msg) {
        this.fromUserName = msg.getToUserName();
        this.toUserName = msg.getFromUserName();
    }
}
