package org.welisdoom.task.xml.entity;

import org.welisdoom.task.xml.annotations.Attr;
import org.welisdoom.task.xml.annotations.Tag;
import org.welisdoom.task.xml.intf.Copyable;
import org.welisdoom.task.xml.intf.type.Script;
import org.welisdoom.task.xml.intf.type.UnitType;

/**
 * @Classname Content
 * @Description TODO
 * @Author Septem
 * @Date 15:07
 */

@Tag(value = "content", parentTagTypes = UnitType.class, desc = "单纯的文本内容")
@Attr(name = "content", desc = "文本内容")
public class Content extends Unit implements Script, Copyable {
    String content;

    public Content setContent(String content) {
        this.content = content;
        return this;
    }

    public String getContent() {
        return content;
    }

    @Override
    public String getScript(TaskInstance request, String s) {
        return content + s;
    }

    @Override
    public Copyable copy() {
        Content content = copyableUnit(this);
        content.content = this.content;
        return content;
    }

    @Override
    public boolean isStaticContent() {
        return true;
    }
}
