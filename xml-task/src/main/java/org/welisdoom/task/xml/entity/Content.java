package org.welisdoom.task.xml.entity;

import org.welisdoom.task.xml.annotations.Attr;
import org.welisdoom.task.xml.annotations.Tag;
import org.welisdoom.task.xml.intf.type.Script;
import org.welisdoom.task.xml.intf.type.Stream;
import org.welisdoom.task.xml.intf.type.UnitType;

import java.util.Map;

/**
 * @Classname Content
 * @Description TODO
 * @Author Septem
 * @Date 15:07
 */

@Tag(value = "content", parentTagTypes = UnitType.class,desc = "单纯的文本内容")
@Attr(name = "content", desc = "文本内容")
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
    public String getScript(TaskRequest request, String s) {
        return content + s;
    }
}
