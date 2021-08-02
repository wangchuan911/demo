package org.welisdoon.web.entity.wechat.messeage.response;

import org.welisdoon.web.entity.wechat.messeage.request.RequestMesseage;
import org.welisdoon.web.entity.wechat.messeage.MesseageTypeEnum;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "xml")
@XmlAccessorType(XmlAccessType.FIELD)
public class VideoMesseage extends ResponseMesseage {
	@XmlElement(name = "Video")
	private final Video Video = new Video();

	public void setMediaId(String mediaId) {
		Video.setMediaId(mediaId);
	}

	public String getMediaId() {
		return Video.getMediaId();
	}

	public String getTitle() {
		return Video.getTitle();
	}

	public String getDescription() {
		return Video.getDescription();
	}

	public void setTitle(String title) {
		Video.setTitle(title);
	}

	public void setDescription(String description) {
		Video.setDescription(description);
	}
	
	public void setVideoInfo(String MediaId,String Title,String Description){
		Video.setTitle(Title);
		Video.setDescription(Description);
		Video.setMediaId(MediaId);
	}

	public VideoMesseage() {
		super();
		// TODO Auto-generated constructor stub
	}

	public VideoMesseage(RequestMesseage msg, Long createTime) {
		super(msg, createTime, MesseageTypeEnum.VIDEO.getValue());

		// TODO Auto-generated constructor stub
	}

	public VideoMesseage(RequestMesseage msg) {
		super(msg, MesseageTypeEnum.VIDEO.getValue());
		// TODO Auto-generated constructor stub
	}

	public VideoMesseage(String toUserName, String fromUserName,
						 Long createTime) {
		super(toUserName, fromUserName, createTime, MesseageTypeEnum.VIDEO.getValue());
		// TODO Auto-generated constructor stub
	}

	public VideoMesseage(String toUserName, String fromUserName) {
		super(toUserName, fromUserName, MesseageTypeEnum.VIDEO.getValue());
		// TODO Auto-generated constructor stub
	}

}

@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
class Video {
	@XmlElement(name = "MediaId")
	private String MediaId;
	@XmlElement(name = "Title")
	private String Title;
	@XmlElement(name = "Description")
	private String Description;

	public String getMediaId() {
		return MediaId;
	}

	public String getTitle() {
		return Title;
	}

	public String getDescription() {
		return Description;
	}

	public void setMediaId(String mediaId) {
		MediaId = mediaId;
	}

	public void setTitle(String title) {
		Title = title;
	}

	public void setDescription(String description) {
		Description = description;
	}

}