package my.hehe.webserver.entity.wechat.messeage.response;

import my.hehe.webserver.entity.wechat.messeage.MesseageTypeEnum;
import my.hehe.webserver.entity.wechat.messeage.request.RequestMesseage;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "xml")
@XmlAccessorType(XmlAccessType.FIELD)
public class ImageMesseage extends ResponseMesseage {
	@XmlElement(name = "Image")
	private final Image image = new Image();

	public void setMediaId(String mediaId) {
		image.setMediaId(mediaId);
	}

	public String getMediaId() {
		return image.getMediaId();
	}

	public ImageMesseage() {
		super();
		// TODO Auto-generated constructor stub
	}

	public ImageMesseage(RequestMesseage msg, Long createTime) {
		super(msg, createTime, MesseageTypeEnum.IMAGE.getValue());

		// TODO Auto-generated constructor stub
	}

	public ImageMesseage(RequestMesseage msg) {
		super(msg,MesseageTypeEnum.IMAGE.getValue());
		// TODO Auto-generated constructor stub
	}

	public ImageMesseage(String toUserName, String fromUserName,
						 Long createTime) {
		super(toUserName, fromUserName, createTime, MesseageTypeEnum.IMAGE.getValue());
		// TODO Auto-generated constructor stub
	}

	public ImageMesseage(String toUserName, String fromUserName) {
		super(toUserName, fromUserName, MesseageTypeEnum.IMAGE.getValue());
		// TODO Auto-generated constructor stub
	}

	@Override
	public String toString() {
		return "ImageMesseage{" +
				"image=" + image +
				", toUserName='" + toUserName + '\'' +
				", fromUserName='" + fromUserName + '\'' +
				", createTime=" + createTime +
				", msgType='" + msgType + '\'' +
				'}';
	}
}

@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
class Image {
	@XmlElement(name = "MediaId")
	private String mediaId;

	public String getMediaId() {
		return mediaId;
	}

	public void setMediaId(String mediaId) {
		this.mediaId = mediaId;
	}

}