package org.welisdoon.web.entity.wechat.push;

public class PublicTamplateMessage extends PushMessage {

    /*
    参数	是否必填	说明
    touser	是	接收者openid
    template_id	是	模板ID
    url	否	模板跳转链接（海外帐号没有跳转能力）
    miniprogram	否	跳小程序所需数据，不需跳小程序可不用传该数据
    appid	是	所需跳转到的小程序appid（该小程序appid必须与发模板消息的公众号是绑定关联关系，暂不支持小游戏）
    pagepath	否	所需跳转到小程序的具体页面路径，支持带参数,（示例index?foo=bar），要求该小程序已发布，暂不支持小游戏
    data	是	模板数据
    color	否	模板内容字体颜色，不填默认为黑色
    * */

    MiniProgram miniprogram;

    public static class MiniProgram {
        String appid;
        String pagepath;

        public String getAppid() {
            return appid;
        }

        public MiniProgram setAppid(String appid) {
            this.appid = appid;
            return this;
        }

        public String getPagepath() {
            return pagepath;
        }

        public MiniProgram setPagepath(String pagepath) {
            this.pagepath = pagepath;
            return this;
        }
    }

    public MiniProgram getMiniprogram() {
        return miniprogram;
    }

    public PublicTamplateMessage setMiniprogram(MiniProgram miniprogram) {
        this.miniprogram = miniprogram;
        return this;
    }

    @Override
    public String toString() {
        return super.toString();
    }
}
