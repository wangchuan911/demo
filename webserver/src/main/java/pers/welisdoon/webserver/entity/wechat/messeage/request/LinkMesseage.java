package pers.welisdoon.webserver.entity.wechat.messeage.request;


public interface LinkMesseage extends RequestMesseage {

    public String getTitle();

    public String getDescription();

    public String getUrl();

    public void setTitle(String title);

    public void setDescription(String description);

    public void setUrl(String url);


}
