package org.welisdoon.web.entity.wechat.messeage.response;

import org.welisdoon.web.entity.wechat.messeage.MesseageTypeEnum;
import org.welisdoon.web.entity.wechat.messeage.MesseageTypeValue;
import org.welisdoon.web.entity.wechat.messeage.request.RequestMesseage;

import javax.xml.bind.JAXBException;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;


public class NoReplyMesseage extends ResponseMesseage {

	public NoReplyMesseage(RequestMesseage msg) {
		super(msg, MesseageTypeEnum.TEXT.getValue());
	}

	@Override
	public String toXMLString() {
		return MesseageTypeValue.MSG_REPLY;
	}
}
