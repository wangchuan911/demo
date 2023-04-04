package org.welisdoom.task.xml.entity;

import org.apache.commons.collections4.MapUtils;
import org.welisdoom.task.xml.annotations.Attr;
import org.welisdoom.task.xml.annotations.Tag;
import org.welisdoom.task.xml.intf.type.Initialize;
import org.xml.sax.Attributes;

import java.util.Objects;

/**
 * @Classname Values
 * @Description TODO
 * @Author Septem
 * @Date 19:24
 */
@Tag(value = "value", parentTagTypes = Initialize.class, desc = "初始化值")
@Attr(name = "id", desc = "唯一标识", require = true)
@Attr(name = "value", desc = "值", require = true)
public class Value extends Unit implements Initialize {

    @Override
    public Unit attr(Attributes attributes) {
        return super.attr(attributes);
    }

    public String getValue() {
        return MapUtils.getString(attributes, "value", getChild(Content.class).stream().findFirst().orElse(new Content().setContent("")).getContent());
    }

    public static String getValue(Task task, String name) {
        return Initialization.getInstance(task)
                .getChildren(Value.class)
                .stream()
                .filter(value -> Objects.equals(name, value.getId()))
                .findFirst().orElse(new Value()).getValue();

    }
}
