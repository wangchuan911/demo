package org.welisdoom.task.xml.entity;

import org.welisdoom.task.xml.annotations.Attr;
import org.welisdoom.task.xml.annotations.Tag;
import org.welisdoom.task.xml.intf.type.Stream;
import org.xml.sax.Attributes;

/**
 * @Classname Col
 * @Description TODO
 * @Author Septem
 * @Date 12:17
 */

@Tag(value = "col", parentTagTypes = {Stream.class, Stream.Writer.class}, desc = "将数据整合成为一个map,code为键，value为值")
@Attr(name = "name", desc = "字段信息")
@Attr(name = "value", desc = "字段的值")
@Attr(name = "code", require = true, desc = "字段别名")
public class Col extends Unit {
    String name, value, code;

    @Override
    public Unit attr(Attributes attributes) {
        super.attr(attributes);
        setName(this.attributes.get("name"));
        setCode(this.attributes.get("code"));
        setValue(this.attributes.get("value"));
        return this;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }
}
