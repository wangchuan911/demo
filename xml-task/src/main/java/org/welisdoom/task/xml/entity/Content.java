package org.welisdoom.task.xml.entity;

import org.welisdoom.task.xml.intf.type.Script;

import java.util.Map;

/**
 * @Classname Content
 * @Description TODO
 * @Author Septem
 * @Date 15:07
 */

public class Content extends Unit implements Script {
    String content;

    public Content setContent(String content) {
        this.content = content;
        return this;
    }

    public String getContent() {
        return content;
    }

    @Override
    public String getScript(Map<String, Object> data, String s) {
        return content + s;
    }
}
