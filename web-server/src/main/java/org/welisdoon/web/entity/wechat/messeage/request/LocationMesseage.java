package org.welisdoon.web.entity.wechat.messeage.request;


public interface LocationMesseage extends RequestMesseage {

    public String getLocationX();

    public String getLocationY();

    public String getScale();

    public String getLabel();

    public void setLocationX(String locationX);

    public void setLocationY(String locationY);

    public void setScale(String scale);

    public void setLabel(String label);


}
