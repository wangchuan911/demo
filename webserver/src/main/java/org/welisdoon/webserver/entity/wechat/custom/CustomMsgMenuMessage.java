package org.welisdoon.webserver.entity.wechat.custom;

import java.util.LinkedList;
import java.util.List;

public class CustomMsgMenuMessage extends CustomMessage {
    MsgMenu msgmenu;

    public CustomMsgMenuMessage() {
        this.msgtype = MsgType.MSG_MENU.value;
    }

    public static class MsgMenu {
        String head_content;
        List<Menu> list;

        public static class Menu {
            String id, content;

            public String getId() {
                return id;
            }

            public void setId(String id) {
                this.id = id;
            }

            public String getContent() {
                return content;
            }

            public void setContent(String content) {
                this.content = content;
            }
        }
    }

    private void initParameter() {
        if (this.msgmenu == null) {
            this.msgmenu = new MsgMenu();
            if (this.msgmenu.list == null) {
                this.msgmenu.list = new LinkedList<>();
            }
        }
    }

    public CustomMsgMenuMessage addMenu(String id, String content) {
        this.initParameter();
        MsgMenu.Menu n = new MsgMenu.Menu();
        n.id = id;
        n.content = content;
        this.msgmenu.list.add(n);
        return this;
    }

    public CustomMsgMenuMessage headContent(String headContent) {
        this.initParameter();
        this.msgmenu.head_content = headContent;
        return this;
    }

}
