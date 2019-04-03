package my.hehe.webserver.entity.wechat.messeage.response;



import my.hehe.webserver.entity.wechat.messeage.MesseageTypeEnum;
import my.hehe.webserver.entity.wechat.messeage.request.RequestMesseage;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "xml")
@XmlAccessorType(XmlAccessType.FIELD)
public class VoiceMesseage extends ResponseMesseage {
	@XmlElement(name = "Voice")
	private final Voice voice = new Voice();

	public void setMediaId(String mediaId) {
		voice.setMediaId(mediaId);
	}

	public String getMediaId() {
		return voice.getMediaId();
	}

	public VoiceMesseage() {
		super();
		// TODO Auto-generated constructor stub
	}

	public VoiceMesseage(RequestMesseage msg, Long createTime) {
		super(msg, createTime, MesseageTypeEnum.VOICE.getValue());

		// TODO Auto-generated constructor stub
	}

	public VoiceMesseage(RequestMesseage msg) {
		super(msg, MesseageTypeEnum.VOICE.getValue());
		// TODO Auto-generated constructor stub
	}

	public VoiceMesseage(String toUserName, String fromUserName,
                         Long createTime) {
		super(toUserName, fromUserName, createTime, MesseageTypeEnum.VOICE.getValue());
		// TODO Auto-generated constructor stub
	}

	public VoiceMesseage(String toUserName, String fromUserName) {
		super(toUserName, fromUserName, MesseageTypeEnum.VOICE.getValue());
		// TODO Auto-generated constructor stub
	}

}

@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
class Voice {
	@XmlElement(name = "MediaId")
	private String mediaId;

	public String getMediaId() {
		return mediaId;
	}

	public void setMediaId(String mediaId) {
		this.mediaId = mediaId;
	}

}