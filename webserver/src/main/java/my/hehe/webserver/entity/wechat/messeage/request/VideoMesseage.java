package my.hehe.webserver.entity.wechat.messeage.request;


public interface VideoMesseage extends RequestMesseage {

    public String getMediaId();

    public String getThumbMediaId();

    public void setMediaId(String mediaId);

    public void setThumbMediaId(String thumbMediaId);




}
