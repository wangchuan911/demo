package org.welisdoon.web.entity.wechat.custom;

public class CustomImageMessage extends CustomMessage {
    Image image;

    public CustomImageMessage() {
        this.msgtype = MsgType.IMAGE.value;
    }

    public static class Image {
        String media_id;

        public String getMedia_id() {
            return media_id;
        }

        public void setMedia_id(String media_id) {
            this.media_id = media_id;
        }
    }

    public Image getImage() {
        return image;
    }

    public CustomImageMessage setImage(Image image) {
        this.image = image;
        return this;
    }

    public CustomImageMessage image(String imageId) {
        if (this.image == null) {
            this.image = new Image();
        }
        this.image.media_id = imageId;
        return this;
    }
}
