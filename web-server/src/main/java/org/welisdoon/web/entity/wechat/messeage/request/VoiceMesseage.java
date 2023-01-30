package org.welisdoon.web.entity.wechat.messeage.request;


public interface VoiceMesseage extends RequestMesseage {




	public String getMediaId() ;

	public String getFormat() ;

	public String getRecognition() ;

	public void setMediaId(String mediaId) ;

	public void setFormat(String format) ;

	public void setRecognition(String recognition);




}
