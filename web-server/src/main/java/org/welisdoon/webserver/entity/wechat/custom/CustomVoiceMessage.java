package org.welisdoon.webserver.entity.wechat.custom;

public class CustomVoiceMessage extends CustomMessage {
    Voice voice;

    public CustomVoiceMessage() {
        this.msgtype = MsgType.VOICE.value;
    }

    public static class Voice {
        String media_id, thumb_media_id, title, description;

        public String getMedia_id() {
            return media_id;
        }

        public void setMedia_id(String media_id) {
            this.media_id = media_id;
        }

        public String getThumb_media_id() {
            return thumb_media_id;
        }

        public void setThumb_media_id(String thumb_media_id) {
            this.thumb_media_id = thumb_media_id;
        }

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }
    }

    public Voice getVoice() {
        return voice;
    }

    public CustomVoiceMessage setVoice(Voice voice) {
        this.voice = voice;
        return this;
    }

    public CustomVoiceMessage voice(String voiceId) {
        if (this.voice == null) {
            this.voice = new Voice();
        }
        this.voice.media_id = voiceId;
        return this;
    }

    public CustomVoiceMessage voice(String voiceId, String thumb_media_id, String title, String description) {
        if (this.voice == null) {
            this.voice = new Voice();
        }
        this.voice.media_id = voiceId;
        this.voice.thumb_media_id = thumb_media_id;
        this.voice.title = title;
        this.voice.description = description;
        return this;
    }
}
