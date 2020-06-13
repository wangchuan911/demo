package org.welisdoon.webserver.entity.wechat.custom;

public class CustomWxCardMessage extends CustomMessage {
    WxCard wxcard;

    public CustomWxCardMessage() {
        this.msgtype = MsgType.WX_CARD.value;
    }

    public static class WxCard {
        String card_id;

        public String getCard_id() {
            return card_id;
        }

        public void setCard_id(String card_id) {
            this.card_id = card_id;
        }
    }

    public WxCard getWxcard() {
        return wxcard;
    }

    public CustomWxCardMessage setWxcard(WxCard wxcard) {
        this.wxcard = wxcard;
        return this;
    }

    public CustomWxCardMessage wxcard(String cardId) {
        if (this.wxcard == null) {
            this.wxcard = new WxCard();
        }
        this.wxcard.card_id = cardId;
        return this;
    }
}
