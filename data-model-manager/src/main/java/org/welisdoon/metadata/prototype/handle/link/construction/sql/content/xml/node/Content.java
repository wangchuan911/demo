package org.welisdoon.metadata.prototype.handle.link.construction.sql.content.xml.node;

import org.welisdoom.task.xml.annotations.Attr;
import org.welisdoom.task.xml.annotations.Tag;
import org.welisdoom.task.xml.intf.type.BaseUnit;
import org.welisdoom.task.xml.intf.type.Script;
import org.welisdoon.metadata.prototype.handle.link.construction.sql.content.xml.entity.SqlParameter;

import java.util.Map;

/**
 * @Classname Content
 * @Description TODO
 * @Author Septem
 * @Date 15:07
 */

@Tag(value = "content", parentTagTypes = BaseUnit.class, desc = "单纯的文本内容")
@Attr(name = "content", desc = "文本内容")
public class Content extends LikeMyBatisSqlNode implements Script<SqlParameter> {
    String content;

    public Content(LikeMyBatisSqlNode parent, Map<String, String> attributes) {
        super(parent, attributes);
    }

    public Content setContent(String content) {
        this.content = content;
        return this;
    }

    public String getContent() {
        return content;
    }

    @Override
    public String getScript(SqlParameter request, String s) {
        return content + s;
    }

    @Override
    public LikeMyBatisSqlNode copyTo(LikeMyBatisSqlNode parent) {
        Content content = (Content) super.copyTo(parent);
        content.content = this.content;
        return content;
    }

    @Override
    public boolean isStaticContent() {
        return true;
    }
}
