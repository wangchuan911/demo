package pers.welisdoon.webserver.entity.wechat.messeage.request;




public interface ImageMesseage extends RequestMesseage {

	
	public String getPicUrl();

	public String getMediaId();
	

	public void setPicUrl(String picUrl);

	public void setMediaId(String mediaId);







}
