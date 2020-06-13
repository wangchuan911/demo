package org.welisdoon.webserver.entity.wechat.custom;

public class CustomTextMessage extends CustomMessage {
    Text text;

    public CustomTextMessage() {
        this.msgtype = MsgType.TEXT.value;
    }

    public static class Text {
        String content;

        public String getContent() {
            return content;
        }

        public void setContent(String content) {
            this.content = content;
        }
    }

    public Text getText() {
        return text;
    }

    public void setText(Text text) {
        this.text = text;
    }

    public CustomTextMessage text(String text) {
        if (this.text == null) {
            this.text = new Text();
        }
        this.text.content = text;
        return this;
    }
}
