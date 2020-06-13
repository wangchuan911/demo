package org.welisdoon.webserver.entity.wechat.custom;

public class CustomMiniProgramPageMessage extends CustomMessage {
    MiniProgramPage miniprogrampage;

    public CustomMiniProgramPageMessage() {
        this.msgtype = MsgType.MINI_PROGRAM_PAGE.value;
    }

    public static class MiniProgramPage {
        String title, appid, pagepath, thumb_media_id;

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public String getAppid() {
            return appid;
        }

        public void setAppid(String appid) {
            this.appid = appid;
        }

        public String getPagepath() {
            return pagepath;
        }

        public void setPagepath(String pagepath) {
            this.pagepath = pagepath;
        }

        public String getThumb_media_id() {
            return thumb_media_id;
        }

        public void setThumb_media_id(String thumb_media_id) {
            this.thumb_media_id = thumb_media_id;
        }
    }

    public MiniProgramPage getMiniprogrampage() {
        return miniprogrampage;
    }

    public void setMiniprogrampage(MiniProgramPage miniprogrampage) {
        this.miniprogrampage = miniprogrampage;
    }

    public CustomMiniProgramPageMessage miniprogrampage(String title, String appid, String pagepath, String thumb_media_id) {
        if (this.miniprogrampage == null) {
            this.miniprogrampage = new MiniProgramPage();
        }
        this.miniprogrampage.title = title;
        this.miniprogrampage.appid = appid;
        this.miniprogrampage.pagepath = pagepath;
        this.miniprogrampage.thumb_media_id = thumb_media_id;
        return this;
    }
}
