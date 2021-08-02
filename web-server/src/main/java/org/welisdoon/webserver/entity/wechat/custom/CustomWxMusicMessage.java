package org.welisdoon.webserver.entity.wechat.custom;

public class CustomWxMusicMessage extends CustomMessage {
    Music music;

    public CustomWxMusicMessage() {
        this.msgtype = MsgType.MUSIC.value;
    }

    public static class Music {
        String title, description, musicurl, hqmusicurl, thumb_media_id;

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

        public String getMusicurl() {
            return musicurl;
        }

        public void setMusicurl(String musicurl) {
            this.musicurl = musicurl;
        }

        public String getHqmusicurl() {
            return hqmusicurl;
        }

        public void setHqmusicurl(String hqmusicurl) {
            this.hqmusicurl = hqmusicurl;
        }

        public String getThumb_media_id() {
            return thumb_media_id;
        }

        public void setThumb_media_id(String thumb_media_id) {
            this.thumb_media_id = thumb_media_id;
        }
    }

    public Music getMusic() {
        return music;
    }

    public CustomWxMusicMessage setMusic(Music music) {
        this.music = music;
        return this;
    }

    public CustomWxMusicMessage music(String title, String description, String musicurl, String hqmusicurl, String thumb_media_id) {
        if (this.music == null) {
            this.music = new Music();
        }
        this.music.title = title;
        this.music.description = description;
        this.music.musicurl = musicurl;
        this.music.hqmusicurl = hqmusicurl;
        this.music.thumb_media_id = thumb_media_id;
        return this;
    }
}
