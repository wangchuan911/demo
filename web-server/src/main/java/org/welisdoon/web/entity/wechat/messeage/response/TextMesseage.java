package org.welisdoon.web.entity.wechat.messeage.response;

import org.welisdoon.web.entity.wechat.messeage.request.RequestMesseage;
import org.welisdoon.web.entity.wechat.messeage.MesseageTypeEnum;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "xml")
@XmlAccessorType(XmlAccessType.FIELD)
public class TextMesseage extends ResponseMesseage {
	@XmlElement(name = "Content")
	private String content;

	
	public String getContent() {
		return this.content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public TextMesseage() {
		this.setMsgType(MesseageTypeEnum.TEXT.getValue());
	}

	public TextMesseage(String toUserName, String fromUserName,
						Long createTime) {
		super(toUserName, fromUserName, createTime, MesseageTypeEnum.TEXT.getValue());
	}

	public TextMesseage(String toUserName, String fromUserName) {
		super(toUserName, fromUserName, MesseageTypeEnum.TEXT.getValue());
	}

	public TextMesseage(RequestMesseage msg, Long createTime) {
		super(msg, createTime, MesseageTypeEnum.TEXT.getValue());
	}

	public TextMesseage(RequestMesseage msg) {
		super(msg, MesseageTypeEnum.TEXT.getValue());
	}


	@Override
	public String toString() {
		return "TextMesseage{" +
				"content='" + content + '\'' +
				", toUserName='" + toUserName + '\'' +
				", fromUserName='" + fromUserName + '\'' +
				", createTime=" + createTime +
				", msgType='" + msgType + '\'' +
				'}';
	}
}
