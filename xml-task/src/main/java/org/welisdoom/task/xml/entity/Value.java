package org.welisdoom.task.xml.entity;

import org.apache.commons.collections4.MapUtils;
import org.xml.sax.Attributes;

import java.util.Objects;

/**
 * @Classname Values
 * @Description TODO
 * @Author Septem
 * @Date 19:24
 */
@Tag(value = "value", parentTag = Initialization.class)
public class Value extends Unit {

    @Override
    public Unit attr(Attributes attributes) {
        return super.attr(attributes);
    }

    public String getValue() {
        return MapUtils.getString(attributes, "value", this.content);
    }

    public static String getValue(Task task, String name) {
        return Initialization.getInstance(task)
                .getChildren(Value.class)
                .stream()
                .filter(value -> Objects.equals(name, value.getName()))
                .findFirst().orElse(new Value()).getValue();

    }
}
